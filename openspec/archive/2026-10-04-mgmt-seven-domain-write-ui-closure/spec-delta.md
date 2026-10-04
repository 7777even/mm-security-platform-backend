# Spec Delta: 七域写端点授权与 schema

## 变更（授权）
7 个 Controller 的写端点由 `@RequireAuth(role = "ADMIN")` 改为按域权限码：
`video:camera-write` / `communication:device-write` / `communication:record-write` /
`device:write` / `hazard:write` / `hazard:point-write` / `special-operation:write`。

## 新增 schema（数据库）
- `sys_menu` 新增 7 条 `menu_type='BUTTON'`、`visible=0` 的按钮级菜单，父级统一挂 `fm-production`。
- `sys_role_menu` 按角色播种（ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER）。

## 不变
- HTTP 路径、请求体字段、响应 schema 全部不变。
- 读端点授权与行为不变。
- `VideoController` 的 video.linkage 三个既有写端点仍为 `role="ADMIN"`（非本批范围）。
- `@RealtimeSync` 广播域不变。
