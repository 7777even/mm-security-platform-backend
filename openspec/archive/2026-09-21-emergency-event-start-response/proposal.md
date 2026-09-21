# Proposal: 事故救援「启动应急响应」状态持久化

## Why
事故救援处置页（fm-rescue）「启动应急响应」此前仅置前端局部状态（`responseStarted`），刷新即丢；
需新增后端写端点把事件状态推进为「处置中」，使状态可跨刷新保留，并与应急指挥大屏列表状态口径一致。

## What Changes
- 后端新增 `POST /api/v1/emergency-events/{id}/start-response`（`@RequireAuth`，仅登录态自助），
  `EmergencyEventService.startResponse` 同事务置 `fac_emergency_event.status=processing`/`status_label=处置中`，
  并同步 `fac_accident_incident.status_name=processing`/`map_status=处置中`；事件不存在返回 404。
- 前端契约 `docs/api/emergency-event.openapi.json` 增加该路径（唯一真源）；前端接线 emit + 刷新聚合。
- `scripts/check-endpoint-authz.mjs` ALLOWLIST 登记 `EmergencyEvent#startResponse`。

## Capabilities
- 处置页「启动应急响应」可持久化（状态 processing），刷新后仍为「响应已启动 / 处置中」。

## Impact
- 仅新增写端点 + 状态字段推进，无表结构变更。回退：移除端点 + 契约路径 + ALLOWLIST 条目。
