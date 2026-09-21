# Proposal: 事故救援「事件预警」状态持久化

## Why
事故救援处置页（fm-rescue）「事件预警」按钮此前仅置前端局部状态，刷新即丢失，且未同步
`fac_emergency_event` / `fac_accident_incident` 的 `reported` 标志，与应急指挥大屏列表「已预警」口径不一致。
需新增后端写端点将预警状态落库，使状态可跨刷新、跨页面保留。

## What Changes
- 后端新增 `POST /api/v1/emergency-events/{id}/report`（`@RequireAuth`，仅登录态自助），
  `EmergencyEventService.report` 同事务置 `fac_emergency_event` + `fac_accident_incident` 的 `reported=true`，
  事件不存在返回 404。
- 前端契约 `docs/api/emergency-event.openapi.json` 增加该路径（唯一真源）；前端接线 emit('report') + 刷新聚合。
- `scripts/check-endpoint-authz.mjs` ALLOWLIST 登记 `EmergencyEvent#report`。

## Capabilities
- 事故救援处置页「事件预警」可持久化（`reported=true`），刷新后仍为「已预警」，与列表口径一致。

## Impact
- 仅新增写端点 + `reported` 标志更新，无存量字段/表结构变更。回退：移除端点 + 契约路径 + ALLOWLIST 条目。
