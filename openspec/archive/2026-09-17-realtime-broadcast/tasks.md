# Tasks

## 1. 后端机制
- [x] 新增 `EntityChangedEvent`（ApplicationEvent：domain / action / id? / data?）
- [x] 新增 `RealtimeBroadcastService`（会话集合 + `broadcast(topic, payload)`），`AlarmWebSocketHandler` 改为委托它（broadcastAlarm 薄封装），复用 `/ws/alarm`
- [x] 新增 `RealtimePublisher`（`@EventListener(EntityChangedEvent)` → 广播 `<domain>.changed`）
- [x] 新增 `@RealtimeSync(domain)` 注解 + `@AfterReturning` 切面（动作按方法名推断）
- [x] 全量写服务标注扫掠：设备 / 工作站 / 系统管理(user·role·menu) / 台账 / R1–R4 业务写服务 / 通讯 / 演练 / 危化等
- [x] [TDD] `RealtimePublisherTest` / 切面单测（Mockito 验证事件 → 广播包络一致）

## 2. 契约与守门
- [x] 扩展 `frontend-scaffold/docs/api/realtime.openapi.json`（RealtimeDataChange schema + `<domain>.changed` 消息，端点仍 `/ws/alarm`）
- [x] 前端 `npm run gen:api-types` 重生成（若类型受影响）
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异（后端 WS 广播与契约包络一致）

## 3. 验证与归档
- [x] 后端单测全绿 + 覆盖率门禁
- [x] 起服 + Python 冒烟验证：写一条设备 / 台账 → WS 收到 `<domain>.changed`（含负例：非法 topic 容错）
- [x] spec-delta 合入 `openspec/specs/realtime-broadcast/spec.md`；归档 `git mv` 到 `openspec/archive/2026-09-17-realtime-broadcast`
- [x] `node scripts/check-openspec-hygiene.mjs` 通过
