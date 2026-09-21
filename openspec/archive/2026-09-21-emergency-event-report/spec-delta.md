# Spec Delta

## emergency-event（契约）
- 新增路径 `POST /emergency-events/{id}/report`：事件预警（报送）。参数 `id`(path)，返回 `EmergencyEventItem`。
- 语义：仅置 `reported=true`，不改动其他字段；事件不存在 404。

## accident-rescue（聚合契约）
- `AccidentRescueIncident.reported` 已存在；新增端点不改变聚合结构，仅使预警后 `reported` 变为 `true`，
  前端据此派生「已预警」状态。

## 鉴权
- `EmergencyEvent#report`：`@RequireAuth`（仅登录态），ALLOWLIST 显式豁免（与 `create` 同源自助场景）。
