# Capability: Realtime Broadcast

## ADDED Requirements

### Requirement: 数据变更事件与通用广播
系统 SHALL 提供统一的「数据变更事件 → 多 topic WebSocket 广播」机制，使任意写操作可被三端实时感知。

#### Scenario: 事件发布
- **WHEN** 标注 `@RealtimeSync(domain)` 的写方法成功返回
- **THEN** 系统发布 `EntityChangedEvent(domain, action, id?, data?)`，并由 `RealtimePublisher` 经既有 `/ws/alarm` 连接向所有会话广播 `{topic:"<domain>.changed", payload:{domain, action, id?, data?}}`

#### Scenario: 端点复用
- **WHEN** 客户端已连接 `/ws/alarm`
- **THEN** 既可接收既有 `alarm.push`，也可接收任意 `<domain>.changed`，无需建立第二条连接

### Requirement: 写入口显式广播覆盖
系统 SHALL 通过 `@RealtimeSync(domain)` 注解在写方法上显式声明广播域，保证全部数据改动可被发现与审计。

#### Scenario: 覆盖审计
- **WHEN** 检视服务层写方法
- **THEN** 每个对外写方法均标注 `@RealtimeSync(domain)`，未标注者视为不广播且可被评审 / CI 识别

### Requirement: 零下行控制红线不变
实时广播 SHALL 仅下发「刷新通知」（`*.changed`），客户端据此重新拉取只读数据，不含任何硬控下行写指令。

#### Scenario: 只通知不控制
- **WHEN** 检视实时广播相关代码
- **THEN** 不存在向设备 / 系统下发消防泵 / 应急广播 / 逃生门禁等生命安全类硬控写端点的路径

### Requirement: WS 握手鉴权（会话绑定身份）
实时广播通道 SHALL 在 WebSocket 握手阶段对访问令牌做鉴权，并将会话绑定到用户身份，关闭「/ws 免鉴权、全量域变更推给所有订阅者」的越权暴露面。

#### Scenario: 握手鉴权通过
- **WHEN** 客户端以 `?token=<accessToken>` 发起 `/ws/alarm` 升级，且令牌为有效 access 类型、未失效、版本匹配
- **THEN** 握手成功，会话被绑定到 `LoginUser` 身份，可接收后续广播

#### Scenario: 握手鉴权失败
- **WHEN** 客户端未带令牌、令牌失效、非 access 类型或版本不符
- **THEN** 握手返回 HTTP 401 并拒绝升级，连接不建立

#### Scenario: 无身份兜底
- **WHEN** 已建立的 WS 会话在连接后置属性中缺失 `LoginUser`（防御性兜底）
- **THEN** 服务端以 `CloseStatus.NOT_ACCEPTABLE` 关闭会话，不入广播集合

### Requirement: 按 zone_codes 防区过滤（三态 fail-open）
实时广播 SHALL 按会话 `zone_codes` 与目标事件的防区做三态过滤，与 REST ABAC 数据权限口径保持单一真源。

#### Scenario: 会话 ALL 用户
- **WHEN** 会话解析出的防区为 null（ALL，不限防区）
- **THEN** 任何域变更均推送给该会话

#### Scenario: 事件未映射域 fail-open
- **WHEN** 某次广播的事件未携带防区（`zones == null`，即产品尚未定义该域的 location→防区映射）
- **THEN** 该事件广播给全部已认证会话（fail-open，与 REST ABAC 局部生效一致）

#### Scenario: 防区交集命中
- **WHEN** 会话防区与事件防区均非空，且二者交集非空
- **THEN** 该事件仅推送给交集命中的会话（最小权限）

#### Scenario: 防区无交集不推送
- **WHEN** 会话防区与事件防区均非空，但交集为空
- **THEN** 该事件不推送给该会话

### Requirement: 防区注入唯一扩展点 ZoneAware
系统 SHALL 以 `ZoneAware` 标记接口作为实时广播防区注入的唯一扩展点：写方法返回值实现该接口时，其 `getZoneName()`（须与 `sys_zone.zone_name` 对齐）自动成为该次事件的防区。

#### Scenario: 带防区写方法
- **WHEN** 标注 `@RealtimeSync(domain)` 的写方法返回 `ZoneAware` 实体且其 `getZoneName()` 非空
- **THEN** 发布的 `EntityChangedEvent` 携带该防区，广播时按三态过滤

#### Scenario: 带 location 写方法经映射
- **WHEN** 写方法返回 `ZoneAware` 实体且其 `getLocation()` 非空、但 `getZoneName()` 为空
- **THEN** 系统经 `ZoneMappingResolver` 将 `getLocation()` 配置映射为防区集合注入事件 `zones`；映射未命中（null）则 fail-open

#### Scenario: 无防区写方法（预备基建）
- **WHEN** 当前无任何写方法返回 `ZoneAware`（归属规则待产品定）
- **THEN** 广播事件 `zones == null`，全部已认证会话 fail-open 接收；`ZoneAware` 作为扩展点就绪，产品定规则后相关写方法返回 `ZoneAware` 即自动启用按域收紧

### Requirement: 可配置 location→防区 映射（产品驱动收紧）
系统 SHALL 提供配置驱动的 `location → 防区` 映射解析（`ZoneMappingResolver`，配置 `abac.zone-mapping.location-to-zones`），将写实体携带的 `location`/`area` 标识解析为与 `sys_zone.zone_name` 对齐的防区集合，作为实时广播防区过滤的输入。

#### Scenario: 配置为空 fail-open
- **WHEN** `abac.zone-mapping.location-to-zones` 为空（或某 location 未命中）
- **THEN** `resolveZonesByLocation` 返回 null，广播事件 `zones == null`，全部已认证会话 fail-open 接收（与现状一致）

#### Scenario: 配置命中收紧
- **WHEN** 某 location 在配置中存在映射
- **THEN** 返回对应的防区集合，广播时按三态过滤仅推送给防区交集命中的会话

#### Scenario: 规则来自配置不来自代码
- **WHEN** 检视 `ZoneMappingResolver` 实现
- **THEN** 不存在硬编码的 location→防区 映射规则；映射完全由 `abac.zone-mapping.location-to-zones` 配置提供（AI 不自建权限模型）

### Requirement: 零下行控制红线不变（重申）
实时广播 SHALL 仍仅下发「刷新通知」（`*.changed`），客户端据此重新拉取只读数据，不含任何硬控下行写指令；本变更新增的鉴权与过滤均不改变此红线。

#### Scenario: 只通知不控制
- **WHEN** 检视实时广播相关代码
- **THEN** 不存在向设备 / 系统下发消防泵 / 应急广播 / 逃生门禁等生命安全类硬控写端点的路径
