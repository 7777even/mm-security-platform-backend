# Proposal: ABAC 实时广播防区映射匹配模式（exact/prefix/contains + 别名兜底）

## 背景
P0 ABAC 实时广播防区收紧的最后一步：写方法返回 DTO 已通过 `ZoneAware` 接入防区（18 个 DTO，
`2026-10-06-abac-zone-aware-dto-wiring` 已归档），但 `ZoneMappingResolver` 仅支持「精确相等」匹配。
业务库 location 多为自由文本（如 `炼油区-催化裂化装置西侧`、`水东港区码头平台`），逐条精确枚举既不现实也易腐，
导致绝大多数 location 实际未命中 → fail-open，防区收敛名存实亡。

## 目标
在**不硬编码任何业务规则**（AGENTS §11.3）的前提下，让 `abac.zone-mapping` 配置可驱动「自由文本 location → 防区」：
1. 新增 `match-mode`（exact/prefix/contains，默认 exact 行为不变），contains 适配自由文本子串命中；
2. 新增 `aliases`（同义表述 → canonical 防区键）兜底；
3. 未命中/空白/模式非法一律 fail-open，语义不变；
4. dev 配置给出 `contains` + 16 防区键 + 别名表的演示实现，证明闭环生效，待产品确认规则后按确认结果调整。

## 非目标
- 不改动任何对外 API / 契约 / 端点（纯配置与内部解析器行为）。
- 不引入新的写方法返回 DTO 接线（沿用既有 18 个）。
