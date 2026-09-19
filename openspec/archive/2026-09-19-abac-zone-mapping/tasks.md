# Tasks

## 1. 后端机制
- [x] 新增 `config.AbacZoneMappingProperties`（`@Component @ConfigurationProperties(prefix="abac.zone-mapping")`，默认空 `Map<String,List<String>>` locationToZones）
- [x] 新增 `security.ZoneMappingResolver`（`@Service`）：`Set<String> resolveZonesByLocation(String location)`，配置驱动、空/未命中→null（fail-open），Caffeine 缓存（TTL 10min）
- [x] `websocket.ZoneAware` 扩充 `default String getLocation()`（向后兼容；`getZoneName()` 改为 `default` 返回 null）
- [x] `annotation.RealtimeSyncAspect` 注入 `ZoneMappingResolver`；`extractZones` 改为包可见，优先 `getZoneName()`，否则 `getLocation()`→resolver（resolver 返回 null 即 fail-open）

## 2. 规格回填
- [x] spec-delta 合入 `openspec/specs/realtime-broadcast/spec.md`：新增「可配置 location→防区 映射」Requirement + 更新「ZoneAware 唯一扩展点」Scenario 提及 getLocation()

## 3. 验证（TDD）
- [x] [TDD] `security.ZoneMappingResolverTest`：空配置→null；配置命中→集合；大小写/空白归一；blank location→null
- [x] [TDD] `annotation.RealtimeSyncAspectTest`：ZoneAware-with-location + 空 resolver → 事件 zones=null（fail-open）；配置命中 → 返回映射集合；getZoneName 优先于 getLocation
- [x] 后端单测全绿（`./mvnw test` 或本机 `mvn.cmd -s ci-settings.xml test`）

## 4. 归档
- [x] 全部勾选后 `git mv` 本 Change 到 `openspec/archive/2026-09-19-abac-zone-mapping`
- [x] `node scripts/check-openspec-hygiene.mjs` 通过
