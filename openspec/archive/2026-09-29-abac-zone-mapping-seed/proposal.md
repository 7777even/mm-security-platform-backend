# Proposal: ABAC 防区映射配置注入与零下行闭环验证（abac-zone-mapping-seed）

> 状态：`approved` —— 对 2026-09-19 已批准 `abac-zone-mapping` 提案的落地补全：机制已就绪，本次注入 dev 示例映射并补闭环单测，使零下行防区过滤在 dev 真正生效。

## Why
`ZoneMappingResolver` + `RealtimeSyncAspect.extractZones` + `RealtimeBroadcastService.broadcast` 三态过滤机制（2026-09-17/~19）已落地，但 `abac.zone-mapping.location-to-zones` 配置此前为空 → 事件 `zones` 恒 null → 全推（fail-open）。本次注入与 V34 种子防区对齐的 dev 示例映射，并补 `RealtimeBroadcastServiceTest` 验证三态过滤真正按防区收紧，闭环零下行权限控制体系。

## What Changes
- `application-dev.yml` 追加 `abac.zone-mapping.location-to-zones` 示例映射（厂区南门/西门/码头区/乙烯装置区/芳烃罐区/特勤保障区 → 对应防区），防区名对齐 V34 种子。
- 新增 `websocket.RealtimeBroadcastServiceTest`（4 例）：受限用户仅收交集防区、未映射事件 fail-open 全推、无交集排除、会话计数反映注册会话。
- 不改任何对外接口、不改机制代码（仅配置 + 测试）。

## Impact
- 契约：无对外接口变更，不触发四同步。
- 安全：零下行红线不变；收紧规则 100% 来自配置。
- 数据：无表/字段变更。
