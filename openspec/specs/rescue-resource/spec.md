# rescue-resource Specification

## Purpose

救援资源域（装备 / 人员 / 车辆 / 消防队伍）的唯一真源口径与端点契约。由 Change
`rescue-force-single-source`（V62）建立，取代「扁平台账 + 队伍体系子表」两套并行表。

## Requirements

### Requirement: 救援力量唯一真源

救援力量域（装备 / 人员 / 车辆）SHALL 以扁平资源台账 `fac_rescue_equipment` / `fac_rescue_personnel` /
`fac_rescue_vehicle` 为唯一真源；`fac_brigade_team` 仅作为「中队主表」保留；队伍体系子表
`fac_brigade_equipment` / `fac_brigade_person` / `fac_brigade_vehicle` SHALL 退役。

#### Scenario: 大屏与接口同源
- **WHEN** 消防大屏读取 `GET /fire-monitoring/rescue-forces`
- **THEN** 「救援人员 / 救援装备 / 救援车辆」分别为 `fac_rescue_personnel` / `fac_rescue_equipment` /
  `fac_rescue_vehicle` 的实时计数，与 `GET /rescue-resources/{personnel,equipment,vehicles}` 结果条数一致
- **AND** 「消防队伍」为 `fac_brigade_team` 的实时计数

#### Scenario: 队伍详情由扁平表归组
- **WHEN** 调用 `GET /rescue-resources/brigades[/{id}]`
- **THEN** 每个队伍的 `vehicles` / `personnel` / `equipment` 由扁平表按 `squadron == team_name` 归组得到
- **AND** 人员含 `group` / `phone` / `dutyStatus`，装备含 `category` / `unit`（由 V62 补充列提供）

#### Scenario: 业务总量取真实条数
- **WHEN** 调用 `GET /rescue-resources/equipment` 或 `/rescue-resources/personnel`
- **THEN** `totalSets` / `totalCount` 为对应台账的实时条数，SHALL NOT 使用任何硬编码常量（原 `375` 已移除）

#### Scenario: 应急力量「救援装备」与「应急物资」分离
- **WHEN** 调用 `GET /emergency/strength`
- **THEN** `kind == 救援装备` 的计数与明细取自 `fac_rescue_equipment`
- **AND** `kind == 应急物资` 为统计口径：计数沿用 `sys_emergency_strength` 人工维护值，`items` 为 null

### Requirement: 救援资源四台账全量 CRUD

系统应为救援人员 / 消防队伍 / 救援车辆 / 救援装备四张台账各提供
`POST /api/v1/rescue-resources/{resource}`、`PUT` 与 `DELETE /{resource}/{id}`，
并提供按 id 的详情读端点 `GET /{resource}/{id}`。

- **权限**：分别需 `rescue:personnel:write` / `rescue:brigade:write` /
  `rescue:vehicle:write` / `rescue:equipment:write`（V93 登记，授权 ADMIN / COMMANDER / SCHEDULER）；
- **新增必填**：人员与队伍与装备的 `name`、车辆的 `plate`；缺失返回 B3 `PARAM_INVALID`；
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改；
- **删除**：物理删除（四张表均无 `deleted` 列）；不存在（含重复删除）返回 B3 `NOT_FOUND`；
- **排序**：新增行的 `sort_no` 取当前 `max(sort_no) + 1`，空表从 1 起；
- **广播**：成功写入后分别发布 `rescue.personnel` / `rescue.brigade` /
  `rescue.vehicle` / `rescue.equipment` 域变更。

#### Scenario: 局部更新不覆盖未传字段
- **WHEN** `PUT /api/v1/rescue-resources/personnel/5` 请求体为 `{"dutyStatus":"休整"}`
- **THEN** 该行 `duty_status` 变为「休整」，姓名 / 中队 / 岗位保持原值

#### Scenario: 新增必填缺失
- **WHEN** `POST /api/v1/rescue-resources/vehicles` 未传 `plate`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何插入

#### Scenario: 重复删除
- **WHEN** 对同一个已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）

### Requirement: 车辆写端点只改本体

`PUT / POST /api/v1/rescue-resources/vehicles` 只接受车辆本体字段，
**不接受**乘员 / 随车装备 / 耗材 / 出动汇总子集合；子集合仍由详情端点聚合返回。

#### Scenario: 保存不误删子表
- **WHEN** 编辑车辆只改状态并保存
- **THEN** `fac_rescue_vehicle_crew` 等子表行保持不变

### Requirement: 救援资源只读条目字段扩展

`RescuePersonnelItem` 应返回 `personGroup` / `phone` / `dutyStatus`；
`RescueEquipmentItem` 应返回 `category` / `unit`。

#### Scenario: 编辑弹窗能带出完整值
- **WHEN** 前端以列表行作为 `editRow` 打开编辑弹窗
- **THEN** 上述字段均有值，用户不必重新填写

### Requirement: 救援资源接口不再是只读

`RescueResourceController` 由「全部为 GET 查询」变更为读 + 写；读端点仍需登录态，
写端点另需各资源按钮级权限码。既有 GET 语义、ABAC 防区过滤（`brigades` 按 `area`）均不变。

### 端点全量清单（rescue-resource，显式路径）

| Method | Path | 权限 |
| ------ | ---- | ---- |
| GET | `/api/v1/rescue-resources/personnel` | 登录即可 |
| POST | `/api/v1/rescue-resources/personnel` | `rescue:personnel:write` |
| GET | `/api/v1/rescue-resources/personnel/{id}` | 登录即可 |
| PUT | `/api/v1/rescue-resources/personnel/{id}` | `rescue:personnel:write` |
| DELETE | `/api/v1/rescue-resources/personnel/{id}` | `rescue:personnel:write` |
| GET | `/api/v1/rescue-resources/brigades` | 登录即可（按 `area` 套防区过滤） |
| POST | `/api/v1/rescue-resources/brigades` | `rescue:brigade:write` |
| GET | `/api/v1/rescue-resources/brigades/{id}` | 登录即可 |
| PUT | `/api/v1/rescue-resources/brigades/{id}` | `rescue:brigade:write` |
| DELETE | `/api/v1/rescue-resources/brigades/{id}` | `rescue:brigade:write` |
| GET | `/api/v1/rescue-resources/vehicles` | 登录即可 |
| POST | `/api/v1/rescue-resources/vehicles` | `rescue:vehicle:write` |
| GET | `/api/v1/rescue-resources/vehicles/{id}` | 登录即可 |
| PUT | `/api/v1/rescue-resources/vehicles/{id}` | `rescue:vehicle:write` |
| DELETE | `/api/v1/rescue-resources/vehicles/{id}` | `rescue:vehicle:write` |
| GET | `/api/v1/rescue-resources/equipment` | 登录即可 |
| POST | `/api/v1/rescue-resources/equipment` | `rescue:equipment:write` |
| GET | `/api/v1/rescue-resources/equipment/{id}` | 登录即可 |
| PUT | `/api/v1/rescue-resources/equipment/{id}` | `rescue:equipment:write` |
| DELETE | `/api/v1/rescue-resources/equipment/{id}` | `rescue:equipment:write` |

> 上述 `{resource}` ∈ {personnel, brigades, vehicles, equipment}；写端点语义见上一条 Requirement
> （局部更新 / 物理删除 / `sort_no=max+1` / 按资源广播）。
