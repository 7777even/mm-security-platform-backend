# 提案：系统管理域鉴权粒度细化（role=ADMIN → perm=system:*）

> **状态：`archived` — ADR-5 第二步，已于 2026-09-10 实施并推送 `main@c9d74b5`（mvn test 436 全绿 / 契约 0 漂移 / RBAC 冒烟 54/54）。**
> 本 Change 仅执行注解细化，**不新增表结构、不改端点路径、不改契约 schema**。

## 1. 背景与目标

前序 Change 已实现系统管理域（用户/角色/菜单/字典）全栈，并在 `V33` 种子里落好了**全部 `system:*` 权限码**（view + 各动作码），且 `ADMIN` 角色已授权全部 `status=1` 的菜单节点（含所有按钮级权限码，`V33` 第 92–95 行授权条件不看 `visible`）。

ADR-5 规划的两步走：
- **第一步**（前序 Change 已完成）：端点先统一 `role="ADMIN"` 粗粒度，权限码同批登记，保证可运行。
- **第二步（本 Change）**：把端点注解从 `role="ADMIN"` 逐一切到 `perm="system:*"`，实现细粒度权限判定，使「给某非 ADMIN 角色授予 `system:user:create` 即可让该角色创建用户」成为可能。

## 2. 变更范围

| 项 | 本次是否变更 |
|----|----|
| 数据库迁移（V32/V33） | 否（权限码与 ADMIN 授权已就绪） |
| 端点路径 / 请求响应结构 | 否 |
| 前端契约 `system.openapi.json` / `auth.openapi.json` | 否（仅鉴权粒度变化，无 schema 变化） |
| `@RequireAuth` 注解（已有 `perm` 属性） | 否（能力前序已落地） |
| 四个 System*Controller 的方法级 `@RequireAuth(perm=...)` | **是** |
| 单测 | 是（补鉴权粒度用例） |

## 3. 权限边界矩阵（端点 → perm 映射，权威真源）

映射严格对齐 `V33` 种子 `sys_menu.perm_code` 与前端 `views/system/*.vue` 的 `v-permission` 用法，三方一致。

### SystemUserController（类级保留 `@RequireAuth(role="ADMIN")` 作兜底）
| 方法 | 路径 | perm |
|----|----|----|
| GET | `/system/users` | `system:user:view` |
| GET | `/system/users/{id}` | `system:user:view` |
| POST | `/system/users` | `system:user:create` |
| PUT | `/system/users/{id}` | `system:user:edit` |
| DELETE | `/system/users/{id}` | `system:user:delete` |
| PUT | `/system/users/{id}/status` | `system:user:edit` |
| PUT | `/system/users/{id}/role` | `system:user:assign-role` |
| POST | `/system/users/{id}/password/reset` | `system:user:reset-pwd` |

### SystemRoleController
| 方法 | 路径 | perm |
|----|----|----|
| GET | `/system/roles` | `system:role:view` |
| GET | `/system/roles/{id}` | `system:role:view` |
| POST | `/system/roles` | `system:role:create` |
| PUT | `/system/roles/{id}` | `system:role:edit` |
| DELETE | `/system/roles/{id}` | `system:role:delete` |
| PUT | `/system/roles/{id}/status` | `system:role:edit` |
| GET | `/system/roles/{id}/menus` | `system:role:view` |
| PUT | `/system/roles/{id}/menus` | `system:role:grant` |

### SystemMenuController
| 方法 | 路径 | perm |
|----|----|----|
| GET | `/system/menus` | `system:menu:view` |
| POST | `/system/menus` | `system:menu:create` |
| PUT | `/system/menus/{id}` | `system:menu:edit` |
| DELETE | `/system/menus/{id}` | `system:menu:delete` |
| GET | `/system/permissions` | `system:menu:view` |

### SystemDictController（仅类级细化；`options()` 方法级 `@RequireAuth` 保持「仅登录」不动）
| 方法 | 路径 | perm |
|----|----|----|
| GET | `/system/dict-types` | `system:dict:view` |
| POST | `/system/dict-types` | `system:dict:create` |
| PUT | `/system/dict-types/{id}` | `system:dict:edit` |
| DELETE | `/system/dict-types/{id}` | `system:dict:delete` |
| GET | `/system/dict-items` | `system:dict:view` |
| POST | `/system/dict-items` | `system:dict:create` |
| PUT | `/system/dict-items/{id}` | `system:dict:edit` |
| DELETE | `/system/dict-items/{id}` | `system:dict:delete` |

## 4. 安全评估（关键）

- **不会锁 ADMIN**：`V33` 第 92–95 行 ADMIN 授权 = 全部 `status=1 AND deleted=0` 节点（不看 `visible`），即 ADMIN 同时持有全部 `system:*:view` 与所有动作码。切 perm 后 ADMIN 仍全部通过。
- **漏标不降级**：类级保留 `role="ADMIN"` 兜底；拦截器 `RequireAuthInterceptor` 为「方法级整体覆盖类级」语义，若某方法漏标 `perm`，将落到类级 `role="ADMIN"` 仍受保护，不会意外开放给任意登录用户。
- **当前行为不变**：非 ADMIN 角色（五类岗位）在 `V33` 仅授权 `fm-*` 导航，无 `system:*` 授权，切 perm 后依旧 403——与切前一致。细粒度分权需后续给具体角色授予 `system:*` 才生效（属运维配置，非代码变更）。
- **前端口径统一**：前端 `v-permission` 早已按 `system:*` 控制按钮显隐；后端切 perm 后前后端判定口径一致（此前后端粗粒度 `role=ADMIN`、前端细粒度 `perm`，两套口径漂移）。

## 5. 验证

- `mvn test` 全绿（含新增鉴权粒度单测：ADMIN 全过 + 无授权角色 403）。
- `node scripts/check-api-contract.mjs --strict` 路由 0 差异 / schema 0 漂移（契约未变）。
- `node scripts/check-openspec-hygiene.mjs` OK。
- 端到端冒烟脚本 `smoke_system_rbac.py` 仍应 54/54（admin 持全部 perm 不受影响）。
