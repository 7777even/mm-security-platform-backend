# Proposal: ABAC 实时广播防区映射接线（abac-zone-mapping）

> 状态：`approved` —— L4 安全域变更（ABAC 防区注入 + 横切配置）。用户于 2026-09-19 在「继续未完内容」中选择「P0 ABAC 可配置映射」，明确范围：「把 location→防区 抽成可配置表/配置，fail-open 行为不变；产品给规则后即收紧。不擅自定业务语义，只把接线做通。」即本提案的人工确认。

## Why
`realtime-broadcast` 能力的三态 fail-open 骨架（WS 握手鉴权 + `RealtimeBroadcastService` 按 `zone_codes` 三态过滤 + `ZoneAware` 扩展点）已于 2026-09-17 落地，但生产侧仅支持 `ZoneAware.getZoneName()`（实体直接返回防区名）。而监测告警/设备等多数域的写实体只携带 `location`（装置区编码/区域名），并不持有防区名——`location→防区` 的映射语义「待产品定」。因此当前没有任何写方法实现 `ZoneAware`，事件 `zones` 恒为 null → 全推（fail-open）。本提案补上「可配置的 location→防区 映射」这一层，使防区过滤在产品填充配置后**自动收紧**，且**不硬编码任何映射规则**（规则由产品/运维在配置中提供，规避 AI 自造权限模型，AGENTS §11.3）。

## What Changes
- 新增 `security.ZoneMappingResolver`（`@Service`）：`Set<String> resolveZonesByLocation(String location)`，配置驱动（`abac.zone-mapping.location-to-zones`，`Map<String,List<String>>`），空/未命中→`null`（fail-open），Caffeine 缓存。
- 新增 `config.AbacZoneMappingProperties`（`@ConfigurationProperties(prefix="abac.zone-mapping")`，默认空 Map）。
- `websocket.ZoneAware` 扩充 `default String getLocation()`（向后兼容；`getZoneName()` 保留为直接防区）。
- `annotation.RealtimeSyncAspect` 注入 `ZoneMappingResolver`；`extractZones` 改为：优先 `getZoneName()`，否则 `getLocation()`→resolver（resolver 返回 null 即 fail-open）。

## Capabilities

### Added Capabilities
- （在既有 `realtime-broadcast` capability 内新增 Requirement，不新建 capability）

## Impact
- 受影响范围：`security/ZoneMappingResolver`、`config/AbacZoneMappingProperties`、`websocket/ZoneAware`、`annotation/RealtimeSyncAspect`。
- 契约同步：本次**不改任何对外接口**（无新增/变更 REST 端点、WS 包络与端点 `/ws/alarm` 不变），仅细化服务端广播过滤语义，属服务端内部 ABAC。按 AGENTS §11 四同步「仅当对外接口变才触发」——本次不触发前端契约变更/重生成。
- 数据影响：不涉及表/字段/索引变更（映射走配置而非 `sys_zone_mapping` 表，避免三方言 Flyway 负担）；无存量数据风险，无回退数据需求。配置为空时行为与今日完全一致（fail-open）。
- 安全语义：零下行控制红线不变；ABAC 防区过滤仅在配置填充后收紧，且收紧规则来自产品配置而非代码硬编码，符合「AI 禁止自建权限模型」。
- 不触碰：WS 握手鉴权逻辑、`DataScopeResolver`（消费侧）、过滤器链顺序、生产基线 `application*.yml`（仅新增独立 `@ConfigurationProperties` 类，默认空）。

## 人工确认关卡（L4 须过）
- [x] 用户于 2026-09-19 明确选择「P0 ABAC 可配置映射」，范围=只做接线、不擅自定映射语义；fail-open 不变。
- [x] 未违反零下行控制红线：仅细化广播过滤，不下发任何控制指令。
- [x] 未违反「AI 禁止自建权限模型」：映射规则 100% 来自配置（`abac.zone-mapping.location-to-zones`），代码无硬编码映射；缺省 fail-open。
- [x] 未触发跨库契约四同步：无对外接口变更。
- [x] 数据影响已确认：仅配置，无表/字段变更，无存量数据风险。
- [x] 高风险项：涉及 ABAC 防区（L4），但改动为「扩展点 + 配置驱动解析器」，且 fail-open 默认行为不变，经用户确认范围后实施。
