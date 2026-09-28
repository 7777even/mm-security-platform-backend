# Proposal: perimeter-alarm-device-field（后端）

## 背景
`POST /security/perimeter-alarms` 的 `PerimeterAlarmCreateRequest` 未含 `deviceId`，导致 `fac_perimeter_alarm.device_id` 对人工录入告警恒为空；前端卡片「设备」只能回落关联摄像机。

## 变更
- `PerimeterAlarmCreateRequest` 增加可选 `deviceId`。
- `SecurityService.createPerimeterAlarm` 增加 `e.setDeviceId(req.getDeviceId())`，落库 `device_id`。

## 影响
仅新增可选字段，向后兼容（不传则 device_id 为 NULL），不影响既有种子/实时告警。
