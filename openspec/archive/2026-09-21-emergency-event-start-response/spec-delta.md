# Spec Delta

## emergency-event（契约）
- 新增路径 `POST /emergency-events/{id}/start-response`：启动应急响应。参数 `id`(path)，返回 `EmergencyEventItem`。
- 语义：将状态推进为 `processing`/「处置中」（event.status/status_label + incident.status_name/map_status）；事件不存在 404。

## accident-rescue（聚合契约）
- 聚合 `status` 取 `fac_accident_incident.status_name`；本变更后新事件启动响应时 `status=processing`，
  与既有 V12 种子（`status_name=processing`、`map_status=主力扑救`）口径一致。

## 鉴权
- `EmergencyEvent#startResponse`：`@RequireAuth`（仅登录态），ALLOWLIST 显式豁免（与 create/report 同源自助场景）。
