# Design: 全量数据变更实时广播（realtime-broadcast）

## 目标与约束
- 目标：任何写操作完成后，三端（大屏 / 管理端 / 移动端）订阅对应域的客户端在秒级内自动刷新，无需手动刷新或重进页面。
- 硬约束：零下行控制红线（不向设备 / 系统下发任何控制指令）；`/ws` 免鉴权现状不改动（WS 鉴权 + zone 过滤待 ABAC 落地一并处理）；沿用项目「显式优于通用拦截器」约定（ADR-4）。

## 架构与方案
- 事件：`EntityChangedEvent extends ApplicationEvent`，字段 `domain(String)` / `action(enum CREATED/UPDATED/DELETED)` / `id(String, 可空)` / `data(Object, 可空)`。
- 会话与广播：`RealtimeBroadcastService`（@Component）持有 `CopyOnWriteArraySet<WebSocketSession>` 与 `broadcast(topic, payload)`；`AlarmWebSocketHandler` 改为委托它管理会话与广播，`broadcastAlarm` 变为 `broadcast(TOPIC_ALARM_PUSH, alarmItem)` 的薄封装——既有 `/ws/alarm` 端点与连接复用，`alarm.push` 与 `<domain>.changed` 同连接下发。
- 发布器：`RealtimePublisher`（@Component）`@EventListener(EntityChangedEvent)` → `broadcastService.broadcast(event.domain + ".changed", {domain, action, id, data})`。
- 写入口切面：新增 `@RealtimeSync(domain="<domain>")` 注解 + `@AfterReturning` 切面；动作由方法名推断（create/save/add/insert→CREATED；update/modify/edit/patch→UPDATED；delete/remove/cancel→DELETED；默认 UPDATED）。切面仅发布事件，`id`/`data` 留空（客户端收到后按域重新拉取权威 REST 数据，保证一致，避免脆弱的返回值解析）。
- 覆盖保证：每个写方法显式标注 `@RealtimeSync(domain)`；本变更对全部现有写服务（设备 / 工作站 / 系统管理 user·role·menu / 台账 / R1–R4 业务写服务 / 通讯 / 演练 / 危化等）做标注扫掠，做到「全部数据改动」即广播。新增域按约定标注即自动接入。评审 / CI 以「写方法是否带注解」为覆盖审计点。

## 决策记录（ADR）
- ADR-1 单一 WS 端点：复用 `/ws/alarm` 承载多 topic，不新建端点——包络 `{topic,payload}` 已支持 topic 路由，前端零新增连接。
- ADR-2 显式注解而非通用拦截器：与 ADR-4（DataScopeHelper 显式调用）一致，覆盖确定、可审计、不误伤内部方法；代价是写方法需逐个标注，由本变更一次性扫掠 + 新域约定补齐。
- ADR-3 不推送 id/data：客户端收到 `<domain>.changed` 后整域 refetch，规避返回值解析脆弱性；域刷新频率非高频，refetch 成本可接受。
- ADR-4 WS 鉴权与 zone 过滤不在本变更：当前 `/ws` 免鉴权，且全角色可见现状下广播语义一致；待 ABAC 归属规则（客户调研）落地后，在 WS 握手绑定身份并按 zone_codes 过滤推送目标。

## 风险与缓解
| 风险 | 可能影响 | 缓解 |
| --- | --- | --- |
| 写方法漏标 `@RealtimeSync` 导致该域不刷新 | 局部不实时 | 扫掠全部写服务 + 评审 / CI 审计写方法注解 |
| 高频写造成广播风暴 | 客户端频繁 refetch | 域刷新非高频；前端客户端同域去抖（未来）合并 |
| WS 免鉴权广播扩大暴露面 | 未授权连接可收全部变更 | 当前全角色可见，语义一致；ABAC 落地后补 WS 鉴权 + zone 过滤（ADR-4） |

## 依赖
- 上游：既有 `AlarmWebSocketHandler` / `WebSocketConfig` / `AlarmSimulator`；`realtime.openapi.json` 契约扩展（前端库）。
- 下游：前端 `realtime.ts` topic 路由 + 各域 store 订阅（前端 Change `realtime-data-sync`）；大屏 / 移动端面板刷新。
- 待确认：WS 鉴权与 zone 过滤方案（依赖客户调研的 ABAC 归属规则）——本变更仅预留 hook，不实现。
