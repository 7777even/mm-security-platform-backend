## Capability: fire-facility-monitoring

消防监测运行数据（设施类型 × 装置区）的读取与上报，真源归一为 `fac_fire_facility_monitor` 矩阵。

### ADDED

- `fac_fire_facility_monitor` 新增 `zone_code` / `zone_name` 列，数据由 12 行（按类型）扩展为 168 行（14 区 × 12 类型）。
- `GET /api/v1/fire-situation/areas` 返回的各装置区 `equipment`（消防设备数）改为按 `zone_code` 聚合自 `fac_fire_facility_monitor`，与监测总数 983 自洽（原读 `fac_fire_monitor_area.equipment`，1399 口径作废）。
- `GET /api/v1/fire-facility/monitors` 在监测表矩阵化后按 `key_code` 聚合回 12 类卡片，外部形态不变。
- `fac_fire_facility_param` 新增 `key_code` 列，参数关联由 `monitor_id` 改为 `key_code`（一对多行适配）。

#### Scenario: 装置区设备数与监测总数真源归一

- Given 监测表按 (区, 类型) 矩阵存储，按类型求和 = 983、按区求和 = 983
- When 前端请求 `GET /api/v1/fire-situation/areas`
- Then 返回的 14 个装置区 `equipment` 之和 = 983，且其中炼油一部装置区 = 92

#### Scenario: 监测卡片聚合不变

- Given 监测表含 (区, 类型) 矩阵数据
- When 前端请求 `GET /api/v1/fire-facility/monitors`
- Then 返回 12 类卡片，各类 total 与原值一致、总数 = 983
