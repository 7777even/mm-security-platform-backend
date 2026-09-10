# 设计：系统管理域鉴权粒度细化（role=ADMIN → perm=system:*）

> 配套 `proposal.md`（含端点→perm 映射矩阵）与 `tasks.md`。本文件只讲机制与判定语义。

## 1. 拦截器判定语义（不可错读）

`RequireAuthInterceptor.preHandle` 的注解解析：

```
methodAnn = 方法上的 @RequireAuth
classAnn  = 类上的 @RequireAuth
ann       = methodAnn != null ? methodAnn : classAnn   // 方法级整体覆盖类级
if (ann == null || !ann.value()) return true            // 未注解 / value=false → 放行
if (user == null) throw 401
if (!ann.role().isEmpty() && !ann.role().equalsIgnoreCase(role)) throw 403
if (!ann.perm().isEmpty() && !roleAuthority.hasPerm(role, ann.perm())) throw 403
return true
```

**关键推论**：凡方法上已标 `@RequireAuth(perm=...)` 的端点，其 `role` 为空，
故**只校验 perm、不再校验 ADMIN**；类级 `@RequireAuth(role="ADMIN")` 仅对
**未标方法级注解的方法**生效（兜底防漏标开放）。

## 2. 四条控制器的落地形态

- 类级统一保留 `@RequireAuth(role = "ADMIN")` —— 纯兜底，非主判定。
- 每个业务方法标 `@RequireAuth(perm = "system:{domain}:{action}")`，
  映射见 `proposal.md §3`。
- `SystemDictController.options()`：方法级裸 `@RequireAuth`（无 role/perm）
  覆盖类级 ADMIN，实现「仅登录即可读字典下拉」的业务语义。

## 3. 为什么「切 perm」不会锁 ADMIN / 不会漏标降级

- **不锁 ADMIN**：`V33` 种子把 ADMIN 授权到全部 `status=1 AND deleted=0` 的
  `sys_menu` 节点（授权条件不看 `visible`），故 ADMIN 同时持有全部
  `system:*:view` 与所有动作码，`roleAuthority.hasPerm("ADMIN", ...)` 恒 true。
- **漏标不降级**：一旦某方法忘了标 `perm`，它会落到类级 `role="ADMIN"`，
  非 ADMIN 角色仍被 403 挡住，绝不会变成「任意登录用户可访问」。
- **当前行为零变化**：五类岗位角色在 `V33` 仅授权 `fm-*` 导航、无 `system:*`，
  切 perm 前/后对非 ADMIN 一律 403（与现状一致）。细粒度分权是后续运维动作
  （给具体角色授予 `system:*`），不属于本 Change。

## 4. 测试策略

- 既有 `RequireAuthInterceptorTest`：覆盖 value/role/perm 三级门禁通用语义。
- 新增 `SystemAuthGranularityTest`：用**真实四个控制器**的 `HandlerMethod`
  驱动拦截器，断言
  - ADMIN（stub `hasPerm→true`）访问全部受保护端点均放行；
  - 无 `system:*` 授权的角色（stub `hasPerm→false`）访问全部受保护端点 → 403；
  - `options()` 仅登录即放行（覆盖类级 ADMIN 的例外）。
  直接锁定真实注解，防止 perm 码或方法签名漂移。

## 5. 不在本 Change（已登记独立 Change / 决议不做）

- `sys_role.data_scope`（ALL/DEPT/SELF）行级数据过滤 —— ABAC，多角色未实际启用，超前。
- 账号登录失败锁定 —— 设计决议不做（耦合登录链路、当前无公网暴露收益）。
