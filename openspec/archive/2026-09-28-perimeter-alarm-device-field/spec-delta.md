# Spec Delta: perimeter-alarm-device-field

## Capability: perimeter-alarm

### MODIFIED

#### Requirement: 创建周界入侵告警
`POST /api/v1/security/perimeter-alarms` 的创建请求 SHALL 接受可选 `deviceId` 字段，并落库 `fac_perimeter_alarm.device_id`，使前端卡片「设备」可展示真实录入设备。

##### Scenario: 录入时携带 deviceId
- **WHEN** 请求体包含 `deviceId`
- **THEN** 新建记录 `device_id` 写入该值，返回 `PerimeterAlarmDetail.deviceId` 同步

##### Scenario: 不携带 deviceId
- **WHEN** 请求体不含 `deviceId`
- **THEN** `device_id` 为 NULL，返回 `deviceId` 为空串，前端回落关联摄像机
