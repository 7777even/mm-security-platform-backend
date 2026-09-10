# Spec Delta：系统管理域鉴权粒度细化

> 本 Change **不修改任何 capability spec** —— 仅对既有端点的鉴权注解做细化，
> 端点路径、请求/响应结构、权限码集合均不变。

## 变更的 spec 清单

无。`auth-rbac` 与 `rbac-permission` 两个 capability 已在前序
`2026-09-10-system-management-rbac` 中定义 `perm` 判定能力与 `system:*` 权限码；
本 Change 只是把端点注解从 `role="ADMIN"` 切换到这些**已存在的**权限码，
spec 层面无新增/无删除/无修订。

## 契约影响

- 前端契约 `docs/api/system.openapi.json` / `auth.openapi.json`：**无变更**
  （鉴权粒度属实现侧，不改变 API schema）。
- `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移。

## 数据库影响

无（V32/V33 已落 `sys_menu.perm_code` 与 ADMIN 全量授权）。
