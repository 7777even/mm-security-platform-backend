# Proposal: 全量数据变更实时广播（realtime-broadcast）

> 状态：`draft` —— L3 业务能力变更。用户于 2026-09-17 确认需求：「任何数据改动 → 三端实时刷新」「全部数据改动都要实时刷新」，并要求按 openspec 四同步顺序落地。

## Why
当前 WebSocket 仅广播 `alarm.push`（告警增量）。设备 / 工作站 / RBAC / 台账 / R1–R4 等任何写操作的结果，大屏与移动端只能「进入页面或按需拉取」时才看到，不能实时刷新。三端读一致性已由架构保证（共用 `http.ts` 同源 baseURL、同库同读），缺的是「写后实时推送 + 三端自动刷新」这一环。用户已明确：全部数据改动都要实时刷新，不接收「刷新后看到」的折中。

## What Changes
- 新增 `EntityChangedEvent`（Spring `ApplicationEvent`）：`{domain, action(CREATED/UPDATED/DELETED), id?, data?}`。
- 新增 `RealtimeBroadcastService`：持有 WS 会话集合，提供 `broadcast(topic, payload)` 通用广播；`AlarmWebSocketHandler` 的 `broadcastAlarm` 改为委托它，复用既有 `/ws/alarm` 端点与同一连接——`alarm.push` 与 `<domain>.changed` 同连接下发。
- 新增 `RealtimePublisher`：`@EventListener(EntityChangedEvent)` → 调 `RealtimeBroadcastService.broadcast("<domain>.changed", payload)`。
- 新增 `@RealtimeSync(domain="...")` 注解 + `@AfterReturning` 切面：在标注的写方法返回后自动发布 `EntityChangedEvent`；沿用本项目「显式优于通用拦截器」的既有约定（ADR-4：DataScopeHelper 即显式调用）。覆盖「全部写路径」靠「每个写方法带注解」保证，新增域只要按约定标注即自动广播。
- 在 DeviceService / WorkstationService / 系统管理（user·role·menu）/ 台账 / R1–R4 业务写服务 / 通讯 / 演练 / 危化等写方法上标注 `@RealtimeSync`。

## Capabilities

### Added Capabilities
- `realtime-broadcast`：服务端通用数据变更广播能力（事件 + 多 topic 广播器 + 写入口注解切面），覆盖全部写操作。

## Impact
- 受影响范围：新增 `websocket/EntityChangedEvent`、`websocket/RealtimeBroadcastService`、`websocket/RealtimePublisher`、`annotation/RealtimeSync` + 切面；改造 `AlarmWebSocketHandler`（委托广播）；标注现有写服务方法。
- 契约同步：扩展前端契约 `frontend-scaffold/docs/api/realtime.openapi.json`（新增 `<domain>.changed` 通用消息 + `RealtimeDataChange` schema；WS 端点仍为 `/ws/alarm`）。
- 数据影响：不涉及表 / 字段 / 索引变更，无存量数据影响，无回退数据风险。
- 不触碰的边界：不改 WS 鉴权（当前 `/ws` 免鉴权，与契约 security 声明存在历史缺口，本变更不修，待 ABAC 落地一并处理）；不引入 zone 过滤（同 ABAC 待客户调研）；不新增任何下行硬控写端点（零下行控制红线不变）。
- 安全语义：零下行控制红线不破——`<domain>.changed` 仅为「刷新通知」，客户端据此重新拉取只读数据，不向设备 / 系统下发任何控制指令。当前全角色可见现状下，免鉴权广播与既有「全员可见」语义一致。
- 回归面：现有 `AlarmWebSocketHandler.broadcastAlarm` 行为不变（委托后输出包络一致）；`AlarmSimulator` 不受影响；既有 WS 单测仍有效。

## 人工确认关卡（L3 须过）
- [x] 提案范围与用户确认一致：用户于 2026-09-17 明确「任何数据改动 → 三端实时刷新」「全部数据改动都要实时刷新」，并指示按 openspec 四同步顺序落地（「按顺序做」）。无需求扩散、无自造平行任务。
- [x] API 契约未违反：零下行控制红线保持（`<domain>.changed` 仅刷新通知，不下发控制）；复用既有 `/ws/alarm`，不新增下行端点。
- [x] 跨库四同步已排定：前端 `realtime.openapi.json` 与后端广播包络同步；前端 Change `realtime-data-sync` 与后端 `realtime-broadcast` 同交付。
- [x] 数据变更影响已确认：不涉及表 / 字段 / 索引，无存量数据风险，无回退数据需求。
- [x] 高风险项：未触及 security 权限模型 / 数据库结构 / 安全过滤器链（WS 鉴权与 zone 过滤显式排除，待 ABAC 落地）；本变更为 L3，不需要 L4 人工确认。
