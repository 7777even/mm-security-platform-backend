# Design: 安防设备台账 CRUD——道闸与防恐柱

## 端点与权限矩阵

| 方法 | 路径 | 权限码 | 广播域 | 返回 |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/security/gate-controls` | `security:gate-write` | `security.gate-control` | `GateControlItem` |
| PUT | `/api/v1/security/gate-controls/{id}` | `security:gate-write` | `security.gate-control` | `GateControlItem` |
| DELETE | `/api/v1/security/gate-controls/{id}` | `security:gate-write` | `security.gate-control` | `null` |
| POST | `/api/v1/security/bollards` | `security:bollard-write` | `security.bollard` | `BollardItem` |
| PUT | `/api/v1/security/bollards/{id}` | `security:bollard-write` | `security.bollard` | `BollardItem` |
| DELETE | `/api/v1/security/bollards/{id}` | `security:bollard-write` | `security.bollard` | `null` |

权限由 `@RequireAuth(perm=...)` 切面拦截，未授权返回 403；V101 三方言种子已把两个按钮级菜单（`fm-security-gate-write` / `fm-security-bollard-write`，sort_order 135/136）挂在 `fm-security` 父菜单下并授权 ADMIN、COMMANDER、SCHEDULER、TEAM_LEADER、INNER_OPER、OUTER_OPER。

## 写请求 DTO 与校验（零下行控制）

- `GateControlWriteRequest`（`name` `@NotBlank(message="道闸名称不能为空")`；`location` String 可空；`longitude`/`latitude` Double 可空）。
- `BollardWriteRequest`（`name` `@NotBlank(message="防恐柱名称不能为空")`；`zone` String 可空；`longitude`/`latitude` Double 可空）。
- **两个 DTO 均不含 `status` 字段**：`status` 是设备实时状态，仅读不写，写请求体不携带该字段，从白名单层面杜绝下行控制。
- Controller 侧 `@Valid` 触发校验，失败由全局异常处理器转 B3 `code=400`，不进入 Service。

## 写语义

- **新增**：DTO → 实体全字段落库（不含 status），`version` 由 `@Version` 置 0；返回落库后的 `GateControlItem` / `BollardItem`（含服务端生成主键），供前端免重拉回填。
- **更新**：read-modify-write——先按 `id` 查出实体，再把 DTO 字段覆盖（status 不在白名单，永远不覆盖设备实时状态），`@Version` 冲突抛乐观锁异常转 409。
- **删除**：真删除（物理删除），不存在按 404 处理。

## 实时广播

写方法统一标注 `@RealtimeSync(domain=...)`：道闸三写 `security.gate-control`、防恐柱三写 `security.bollard`。void 写方法（delete）也必须标注——否则 WS 不广播、前端不刷新（沿用 `FireFacilityServiceTest` 的「void 写方法仍广播」断言范式）。

## 契约真源与守门

契约机器可读真源在前端 `docs/api/security.openapi.json`，后端不复制第二份 OpenAPI。同 path 多 method 合并到同一 path key；新增 `GateControlWriteRequest` / `BollardWriteRequest` 两个 schema 与后端 DTO 同名同字段（`status` 不在 schema 内）。守门：`node scripts/check-api-contract.mjs --strict` 比对「Controller (method, path)」与「具名 DTO 字段 + 类型族」，目标路由差异 0 / schema 漂移 0。

## 迁移与多方言

`V101__security_device_ledger_perm.sql` 在 h2 / postgresql / dameng 三方言同名同号：按钮菜单 upsert（按 `code` 幂等）+ `ALTER TABLE fac_gate_control / fac_bollard ADD COLUMN version BIGINT DEFAULT 0`。达梦方言 `version` 用 `NUMBER(19)`。
