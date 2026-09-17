# Tasks

## 1. WS 握手鉴权
- [x] 新增 `websocket/RealtimeAuthHandshakeInterceptor`：`?token=` 抽取 + 与 `JwtFilter` 一致的校验（access 类型、未失效、TokenVersion 版本校验），通过写 `LoginUser` 到 session attributes，失败返回 401
- [x] `config/WebSocketConfig` 注册拦截器到 `/ws/alarm`；`JwtFilter` 白名单注释澄清（仅跳过 Authorization 头，实际鉴权由握手拦截器承担）
- [x] `AlarmWebSocketHandler.afterConnectionEstablished` 从 session attributes 取 `LoginUser`，缺失则 `CloseStatus.NOT_ACCEPTABLE` 兜底

## 2. 会话防区解析与三态过滤
- [x] 重写 `RealtimeBroadcastService`：`Map<WebSocketSession, RealtimeSessionMeta>` + `addSession` 调 `resolveZonesFor` 解析防区；`broadcast(topic, payload, eventZones)` 三态过滤；保留 `broadcast(topic, payload)` 兼容重载（fail-open）
- [x] `security/DataScopeResolver` 新增 `resolveZonesFor(LoginUser)` 显式重载（不依赖线程上下文，供长连接使用）；原 `resolveZones()` 委托它
- [x] `EntityChangedEvent` 增加 `zones` 字段（6 参构造器，保留 5 参兼容）；`RealtimePublisher.publish(..., zones)` 透传

## 3. 防区注入扩展点
- [x] 新增 `websocket/ZoneAware` 标记接口（`getZoneName()` 须对齐 `sys_zone.zone_name`）
- [x] `annotation/RealtimeSyncAspect` 新增 `extractZones(ret)`：返回值 `instanceof ZoneAware` 且 `getZoneName()` 非空 → `Set.of(trimmed)`，否则 null；事件带 zones 发布

## 4. 契约与守门
- [x] 扩展 `frontend-scaffold/docs/api/realtime.openapi.json`（`?token=` 握手鉴权说明，版本升 1.2.0）；前端 Change `realtime-channel-scope` 同步
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异（后端 WS 广播包络与契约一致，无新增 REST 端点）

## 5. 验证与归档
- [x] 后端单测：`RealtimeAuthHandshakeInterceptorTest`（5 类：valid/invalid/wrongType/stateToken/missing）、`RealtimeBroadcastServiceTest`（7 类：ALL/交集/未映射/fail-open/关闭会话跳过/无交集不推/会话计数）、`DataScopeResolverTest`（resolveZonesFor 三态）、`RealtimePublisherTest`、`AlarmWebSocketHandlerTest`（含 missing identity 关闭）全绿
- [x] 前端单测：`ws.spec.ts`（token 注入 URL + 鉴权失败刷新一次）、`realtime.spec.ts`（token 注入不破坏既有）全绿
- [x] spec-delta 合入 `openspec/specs/realtime-broadcast/spec.md`；归档 `git mv` 到 `openspec/archive/2026-09-17-realtime-ws-scope`
- [x] `node scripts/check-openspec-hygiene.mjs` 通过
