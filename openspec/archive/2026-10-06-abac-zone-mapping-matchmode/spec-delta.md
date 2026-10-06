# Spec Delta: realtime-broadcast

## 变更点
更新 `openspec/specs/realtime-broadcast/spec.md` 的 Requirement「可配置 location→防区 映射（产品驱动收紧）」：

- 解析器 SHALL 支持三种可配置匹配模式 `exact`/`prefix`/`contains`（由 `abac.zone-mapping.match-mode` 选择，默认 `exact`）。
- 解析器 SHALL 支持别名兜底 `abac.zone-mapping.aliases`（alias → canonical 键），prefix/contains 模式最长键优先。
- 三种模式未命中/配置为空/location 空白/模式非法一律 fail-open。
- 新增 Scenario：exact 仅精确相等命中、contains 按子串最长键优先、别名重定向 canonical 防区、规则来自配置不来自代码（扩展到 match-mode/aliases 三个键）。

## 已合入
内容已写入 `openspec/specs/realtime-broadcast/spec.md`（对应 Requirement 段落）。
