# Tasks: 写方法返回 DTO 接线 ZoneAware

- [x] 扫 `@RealtimeSync` 全部写方法，确认扩展点取「返回值」而非实体
- [x] 扫返回类型中含 location / zone / area 字段的 DTO，确定接线清单（18 个）
- [x] 接线 16 个 getLocation DTO（含 parkingLocation / storageLocation / operationArea 三个异名字段）
- [x] 接线 2 个 getZoneName DTO（BollardItem / TvMonitorSummary）
- [x] 新增 `ZoneAwareDtoWiringTest`（5 例：接线完整性 / 字段不漂移 / 未映射必 fail-open / 映射命中 / 空白）
- [x] 更新 `ZoneAware` Javadoc：写明接线对象 = 写方法返回类型 + 已接线清单 + 已知限制
- [x] 建 Change 四件套
- [x] 全量 `mvn test` 通过（950 tests / 0 failure / jacoco 门禁达标）+ 门禁复跑后归档提交推送
