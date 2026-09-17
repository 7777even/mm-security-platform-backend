# 变更提案：实时广播 WS 握手鉴权 + 防区过滤（realtime-ws-scope）

> 状态：`draft` —— L3 安全加固变更。本变更承接 `2026-09-17-realtime-broadcast` 提案中显式推迟的 ADR-4（WS 鉴权 + zone 过滤），在 ABAC 归属规则（P0，待产品定 location→防区映射）落地前，先把「会话绑身份 + 按 zone_codes 过滤推送」的骨架搭好，并立即关闭「/ws 免鉴权、把全量域变更推给所有订阅者」的越权暴露面。

## Why

`realtime-broadcast` 落地后，WebSocket 仍走 `/ws` 白名单、免鉴权，把全部域变更推给所有连上的订阅者——非 ALL 用户在 ABAC 数据权限收紧后，仍可通过 WS 看到越权域变更。该缺口已写入长期记要「待补」。

浏览器 WS 升级请求无法携带 `Authorization` 头，令牌只能经 `?token=` 查询参数传递。ABAC 的 location→防区归属规则（哪些实时域应按 zone 过滤）仍待产品定，但 WS 侧必须提前具备：①握手即绑定用户身份（与 REST 鉴权口径一致）②按会话 `zone_codes` 过滤推送目标；对「产品尚未定义映射」的域保持 fail-open（广播给全部已认证会话），与当前 REST ABAC 的局部生效姿态一致。

用户于 2026-09-17 确认两点决策：
- **未映射域策略**：WS 鉴权上线后，对产品还没定义 location→防区映射的实时域，保持广播给全部已认证会话（fail-open 按域，与 REST ABAC 一致）。
- **推进方式**：直接实现（后端 + 前端 + 测试 + openspec 本轮一次性落地）。

## What Changes

- 新增 `websocket/RealtimeAuthHandshakeInterceptor`（`HandshakeInterceptor`）：从 `?token=` 抽取访问令牌，复用 `JwtUtil.parse` + `TokenVersionService.current(subject)` 做与 `JwtFilter` 一致的校验（类型须 access、未失效），通过后把 `LoginUser` 写入 session attributes（`REALTIME_LOGIN_USER_KEY`）；校验失败 `response.setStatusCode(UNAUTHORIZED)` 并拒绝升级。
- `config/WebSocketConfig`：`registry.addHandler(alarmWebSocketHandler, "/ws/alarm").addInterceptors(realtimeAuthHandshakeInterceptor)`，保留 `setAllowedOriginPatterns("*")`。
- `JwtFilter` 白名单注释澄清：`/ws` 仍跳过 `Authorization` 头校验（浏览器 WS 无法带该头），实际鉴权由握手拦截器承担，并非匿名公开端点。
- 重写 `websocket/RealtimeBroadcastService`：持有 `Map<WebSocketSession, RealtimeSessionMeta>`；`addSession(session, LoginUser)` 调 `dataScopeResolver.resolveZonesFor(user)` 解析会话防区；`broadcast(topic, payload)` 兼容重载 → 调 `broadcast(topic, payload, null)`；新增 `broadcast(topic, payload, Set<String> eventZones)` 做三态过滤。
- `websocket/EntityChangedEvent` 新增 `Set<String> zones` 字段（保留 5 参兼容构造器，新增 6 参构造器）。
- `websocket/RealtimePublisher`：`publish(domain, action, id, data)` 兼容重载 → 调 `publish(domain, action, id, data, null)`；新增 `broadcastService.broadcast(domain + ".changed", payload, zones)`。
- `annotation/RealtimeSyncAspect`：新增 `extractZones(Object ret)`——若 `ret instanceof ZoneAware` 且 `getZoneName()` 非空 → `Set.of(zoneName.trim())`，否则 null；事件带 zones 发布。
- 新增 `websocket/ZoneAware` 标记接口（唯一扩展点）：写方法返回值实现该接口，`RealtimeSyncAspect` 自动抽取防区；`getZoneName()` 取值须与 `sys_zone.zone_name` 对齐（当前仅救援队伍等少数域对齐）。
- `security/DataScopeResolver`：原 `resolveZones()` 改为 `resolveZonesFor(UserContext.get())`；新增 `resolveZonesFor(LoginUser user)` 显式传用户（不依赖线程上下文），供长连接场景使用；三态语义不变（null=ALL / 空集=最小权限 / 非空=IN 过滤）。
- `websocket/AlarmWebSocketHandler.afterConnectionEstablished`：从 session attributes 取 `LoginUser`，缺失则 `session.close(CloseStatus.NOT_ACCEPTABLE)` 并 return；否则 `broadcastService.addSession(session, loginUser)`。

## Capabilities

### Modified Capabilities

- `realtime-broadcast`：在既有「数据变更事件与通用广播 / 写入口显式广播覆盖 / 零下行控制红线」基础上，新增「WS 握手鉴权」与「按 zone_codes 防区过滤（三态 fail-open）」两项能力——关闭免鉴权广播的越权暴露面，并为 ABAC 防区过滤预留唯一扩展点 `ZoneAware`。

## Impact

- 受影响范围：`websocket/` 包（新增 `ZoneAware` / `RealtimeAuthHandshakeInterceptor`；重写 `RealtimeBroadcastService` / `RealtimePublisher` / `AlarmWebSocketHandler`；改造 `EntityChangedEvent`）、`annotation/RealtimeSyncAspect`、`security/DataScopeResolver`、`config/WebSocketConfig`、`security/JwtFilter` 注释。
- 契约同步：扩展前端契约 `frontend-scaffold/docs/api/realtime.openapi.json`（新增 `?token=` 握手鉴权说明，版本升 1.2.0）；前端 Change `realtime-channel-scope` 同步。
- 安全语义：关闭「/ws 免鉴权、全量域变更推给所有订阅者」暴露面——未认证连接被握手 401 拒绝；已认证会话按三态防区过滤。未映射域 fail-open 广播给全部已认证（与 REST ABAC 局部生效一致）。零下行控制红线不变。
- 数据影响：不涉及表 / 字段 / 索引变更，无存量数据影响，无回退数据风险。
- 行为变更：现有客户端不带 `?token=` 将在握手被拒（401）。三端前端本次同步注入令牌（见前端 Change），dev 自包含登录保证令牌存在。
- 回归面：既有 `alarm.push` / `<domain>.changed` 广播行为不变（仅增加鉴权与可选过滤）；既有 WS 单测已扩展覆盖握手鉴权与防区过滤。

## 人工确认关卡（L3 须过）

- [x] 提案范围与用户确认一致：用户明确「未映射域 fail-open 广播给全部已认证」与「直接实现（后端+前端+测试+openspec）」，无需求扩散。
- [x] API 契约未违反：零下行控制红线保持；新增 `?token=` 握手参数属 WebSocket 升级鉴权，不新增下行端点；复用既有 `/ws/alarm`。
- [x] 跨库四同步已排定：前端 `realtime.openapi.json` 与后端握手鉴权同步；前端 Change `realtime-channel-scope` 与后端 `realtime-ws-scope` 同交付。
- [x] 数据变更影响已确认：不涉及表 / 字段 / 索引，无存量数据风险，无回退数据需求。
- [x] 高风险项：触及 WS 鉴权与 `DataScopeResolver`（ABAC 核心），但均为**增量式**——未映射域 fail-open、三态语义与 REST ABAC 一致；本变更为 L3，不需要 L4 人工确认。
