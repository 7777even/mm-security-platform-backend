# 设计文档：实时广播 WS 握手鉴权 + 防区过滤（realtime-ws-scope）

## 目标与约束

- 目标：①关闭「/ws 免鉴权、全量域变更推给所有订阅者」的越权暴露面（会话绑定身份）；②为 ABAC 防区过滤预留「按 zone_codes 过滤推送」骨架，对未映射域 fail-open，与 REST ABAC 局部生效姿态一致。
- 硬约束：零下行控制红线不变；浏览器 WS 升级无法带 `Authorization` 头 → 令牌经 `?token=` 查询参数传递；令牌为纯内存态（前端 token.ts 红线）；WS 鉴权口径与 `JwtFilter` 完全一致（类型 access、未失效、`TokenVersionService` 版本校验）。

## 架构与方案

### 握手鉴权
- `RealtimeAuthHandshakeInterceptor implements HandshakeInterceptor`：`beforeHandshake` 从 `request.getURI().getQuery()` 解析 `token` 参数；调 `jwtUtil.parse(token)` 得 `claims`，校验 `claims.get("type") == "access"` 且 `tokenVersionService.current(subject)` 与 `claims` 版本一致；通过则 `attributes.put(REALTIME_LOGIN_USER_KEY, loginUser)` 并返回 true；失败则 `response.setStatusCode(HttpStatus.UNAUTHORIZED)` 返回 false。
- `WebSocketConfig`：`addHandler(...).addInterceptors(realtimeAuthHandshakeInterceptor)`；`@Component` 注入。
- `AlarmWebSocketHandler.afterConnectionEstablished`：从 `session.getAttributes().get(REALTIME_LOGIN_USER_KEY)` 取 `LoginUser`；为 null → `session.close(CloseStatus.NOT_ACCEPTABLE)` 并 return（防御性兜底，握手已拦截但双保险）；否则 `broadcastService.addSession(session, loginUser)`。

### 会话防区解析与三态过滤
- `RealtimeSessionMeta(LoginUser user, Set<String> zones)`：`zones` 由 `dataScopeResolver.resolveZonesFor(user)` 解析（null=ALL / 空集=最小权限 / 非空=IN）。
- `RealtimeBroadcastService`：会话容器改为 `Map<WebSocketSession, RealtimeSessionMeta>`（ConcurrentHashMap）；`broadcast(topic, payload, eventZones)` 遍历会话，按三态判定是否推送：
  - 会话 ALL（`zones == null`）→ 推；
  - 事件未映射（`eventZones == null`）→ 推所有已认证会话（fail-open，与 REST ABAC 局部生效一致）；
  - 二者皆非 null → 仅 `eventZones ∩ session.zones` 非空才推（最小权限）。
  - 关闭/异常会话在遍历时跳过并移除。
- `broadcast(topic, payload)` 兼容重载 → 调 `broadcast(topic, payload, null)`（fail-open）。

### 防区注入唯一扩展点 `ZoneAware`
- `ZoneAware { String getZoneName(); }`：写方法返回值实现该接口，`RealtimeSyncAspect.extractZones(ret)` 抽取防区名（须与 `sys_zone.zone_name` 对齐）。当前无任何写方法返回 `ZoneAware` 实体（如救援队伍域 area 词表已对齐 `sys_zone`，但其写方法返回 DTO 且未标 `@RealtimeSync`），故 zone 过滤是「按域自动生效的预备基建」，与「待产品定」状态完全吻合——靠 `ZoneAware` 扩展点 + 单测证明。
- `EntityChangedEvent` 增加 `zones` 字段（6 参构造器，保留 5 参兼容）；`RealtimePublisher.publish(..., zones)` 透传；`RealtimeSyncAspect` 仅当返回值 `instanceof ZoneAware` 时填 zones，否则 null（fail-open）。

### 长连接与线程上下文
- `DataScopeResolver.resolveZonesFor(LoginUser)` 显式传用户、不依赖 `UserContext`（线程上下文为请求级，长连接跨请求无效）；原 `resolveZones()` 委托 `resolveZonesFor(UserContext.get())` 保持 REST 路径不变。

## 决策记录（ADR）

- ADR-1 令牌走 `?token=` 而非自定义头：浏览器 WS 升级无法设置 `Authorization` / 自定义请求头（被规范禁止），查询参数是唯一可行通道；令牌纯内存态、连接级一次性使用，XSS 暴露面与 REST 一致（且 REST 走 HttpOnly Cookie 刷新）。
- ADR-2 未映射域 fail-open：防区归属规则（哪些实时域需按 zone 过滤）待产品定；与 REST ABAC「局部生效」口径对齐——已定义映射的域收紧，未定义的域不收紧，避免上线即「非 ALL 用户看不到任何实时数据」的过度拦截。
- ADR-3 `ZoneAware` 作为唯一扩展点：与 `DataScopeHelper` 显式调用（ADR-4）一致；新增域只要让写方法返回 `ZoneAware` 实体即自动接入防区过滤，无需改广播核心。
- ADR-4 三态语义复用 REST ABAC：会话 ALL / 事件未映射 / 两者皆非空时的交集判定，直接复用 `DataScopeResolver` 三态，保证 WS 与 REST 数据权限口径单一真源。

## 风险与缓解

| 风险 | 可能影响 | 缓解 |
| --- | --- | --- |
| 前端未注入 token 导致全站 WS 401 | 三端实时刷新失效 | 前端 Change `realtime-channel-scope` 同步注入 `?token=`；dev 自包含登录保证令牌存在；既有单测覆盖握手拒收 |
| 防区过滤误伤（事件/会话 zones 解析偏差） | 合法用户收不到应有实时刷新 | 三态 fail-open（任一为 null 即广播）；单测覆盖 ALL/交集/空集/未映射四类 |
| 握手拦截器性能 | 每连接一次 JWT 解析 | JWT 解析为纯 CPU 操作，连接建立频次低；与 REST 鉴权同成本 |
| ABAC 归属规则长期未定 | 防区过滤始终 fail-open | 骨架已就位，产品定规则后仅需在写方法返回 `ZoneAware` 实体即可生效，无需改广播核心 |

## 依赖

- 上游：既有 `JwtUtil` / `TokenVersionService` / `DataScopeResolver` / `WebSocketConfig` / `AlarmWebSocketHandler`；`JwtFilter` 白名单。
- 下游：前端 `realtime.ts` 注入 `?token=` + 鉴权失败刷新（前端 Change `realtime-channel-scope`）；三端入口 `startRealtime` 接线。
- 待确认：ABAC location→防区归属规则（产品定）后，让相关写方法返回 `ZoneAware` 实体即可启用按域收紧。
