# Tasks

- [x] 在 `AbacZoneMappingProperties` 新增 `matchMode` 与 `aliases` 配置字段及归一化 setter
- [x] 重写 `ZoneMappingResolver` 支持 exact/prefix/contains 三模式 + 别名兜底，未命中 fail-open
- [x] `application-dev.yml` 改为 `match-mode: contains` + 16 防区键 + 别名表演示
- [x] 新增 `ZoneMappingResolverTest`（8 例）覆盖三模式与别名；`ZoneAwareDtoWiringTest` 保持全绿
- [x] 跑后端单测 + 三项门禁（hygiene / contract --strict / endpoint-authz）全绿
- [x] 将 match-mode/aliases 合入 `openspec/specs/realtime-broadcast/spec.md` 的「可配置 location→防区 映射」需求
