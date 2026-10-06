# Design: match-mode + aliases 实现

## 配置载体 `AbacZoneMappingProperties`
- 新增 `matchMode`（默认 `exact`）与 `aliases`（`Map<String,String>`），原 `locationToZones` 保留。
- setter 对 `matchMode` 做 trim+小写归一化；非法值由解析器回退到 exact。

## 解析器 `ZoneMappingResolver`
- `lookup(key)` 按 `normalizeMode(matchMode)` 分派：exact / prefix / contains。
- `exact`：保留原「精确相等 + trim/忽略大小写兜底」。
- `prefix`/`contains`：`matchBySubstring` 对 `location-to-zones` 键按**最长键优先**尝试（避免 `罐区` 抢在 `罐区A` 前命中）；
  未命中再对 `aliases` 同样最长键优先尝试，命中则重定向到 canonical 键查防区集合。
- 子串匹配统一走 `containsIgnoreCase` / `startsWithIgnoreCase`（regionMatches，兼容中文）。
- 任意未命中/空白/空配置/模式非法 → 返回 null（fail-open）。

## dev 配置 `application-dev.yml`
- `match-mode: contains`；`location-to-zones` 覆盖 sys_zone.zone_name 全部 16 防区（V34+V46+V49 种子）；
- `aliases`：催化裂化→炼油区、水东港→码头区、乙烯裂解→乙烯区。

## 测试
- 新增 `ZoneMappingResolverTest`（8 例）：三模式边界、别名重定向、未知模式回退、空白 fail-open。
- 既有 `ZoneAwareDtoWiringTest`（5 例）保持全绿。
