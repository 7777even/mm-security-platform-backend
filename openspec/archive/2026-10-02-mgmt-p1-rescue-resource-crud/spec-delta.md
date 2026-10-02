# Spec Delta: 救援资源四台账写端点

## ADDED Requirements

### Requirement: 救援资源四台账写端点

系统应为救援人员 / 消防队伍 / 救援车辆 / 救援装备四张台账各提供
`POST /api/v1/rescue-resources/{resource}`、`PUT` 与 `DELETE /{resource}/{id}`。

- **权限**：分别需 `rescue:personnel:write` / `rescue:brigade:write` /
  `rescue:vehicle:write` / `rescue:equipment:write`（V93 登记，授权 ADMIN / COMMANDER / SCHEDULER）。
- **新增必填**：人员与队伍与装备的 `name`、车辆的 `plate`；缺失返回 B3 `PARAM_INVALID`。
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改。
- **删除**：物理删除（四张表均无 `deleted` 列）；不存在（含重复删除）返回 B3 `NOT_FOUND`。
- **排序**：新增行的 `sort_no` 取当前 `max(sort_no) + 1`，空表从 1 起。
- **广播**：成功写入后分别发布 `rescue.personnel` / `rescue.brigade` /
  `rescue.vehicle` / `rescue.equipment` 域变更。

#### Scenario: 局部更新不覆盖未传字段

- **WHEN** `PUT /api/v1/rescue-resources/personnel/5` 请求体为 `{"dutyStatus":"休整"}`
- **THEN** 该行 `duty_status` 变为「休整」，姓名 / 中队 / 岗位保持原值。

#### Scenario: 新增必填缺失

- **WHEN** `POST /api/v1/rescue-resources/vehicles` 未传 `plate`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何插入。

#### Scenario: 重复删除

- **WHEN** 对同一个已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）。

### Requirement: 车辆写端点只改本体

`PUT / POST /api/v1/rescue-resources/vehicles` 只接受车辆本体字段，
**不接受**乘员 / 随车装备 / 耗材 / 出动汇总子集合；子集合仍由详情端点聚合返回。

#### Scenario: 保存不误删子表

- **WHEN** 编辑车辆只改状态并保存
- **THEN** `fac_rescue_vehicle_crew` 等子表行保持不变。

### Requirement: 救援资源只读条目字段扩展

`RescuePersonnelItem` 应返回 `personGroup` / `phone` / `dutyStatus`；
`RescueEquipmentItem` 应返回 `category` / `unit`。

#### Scenario: 编辑弹窗能带出完整值

- **WHEN** 前端以列表行作为 `editRow` 打开编辑弹窗
- **THEN** 上述字段均有值，用户不必重新填写。

## MODIFIED Requirements

### Requirement: 救援资源接口不再是只读

`RescueResourceController` 由「全部为 GET 查询」变更为读 + 写；读端点仍需登录态，
写端点另需各资源按钮级权限码。既有 GET 语义、ABAC 防区过滤（`brigades` 按 `area`）均不变。
