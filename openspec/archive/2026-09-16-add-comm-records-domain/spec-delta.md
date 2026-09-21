# Spec Delta: communication（只读域扩展）

## ADDED

### Requirement: 通讯通知记录查询

系统 SHALL 提供 `GET /api/v1/communication/records`，返回五类通讯通知记录
（短信 / 电话通话 / 广播播报 / APP推送 / 语音对讲），并 SHALL 支持 `type` 查询参数过滤；
`type` 缺省时 SHALL 返回全部类型。返回结构为 `CommunicationRecordList`（`items` + `total`）。

#### Scenario: 按类型查询

- **WHEN** 请求 `GET /api/v1/communication/records?type=sms`
- **THEN** 仅返回 `recordType=sms` 的记录，且 `total` 等于 `items` 长度

#### Scenario: 缺省返回全部

- **WHEN** 请求 `GET /api/v1/communication/records`
- **THEN** 返回全部五类记录，按 `recordType` 升序排列

## 约束

- 纯只读：本域 SHALL NOT 提供任何写端点，不含发送 / 外呼 / 播报动作（零下行控制红线不变）。
- 各类型记录按需填充字段，未使用字段 SHALL 返回空字符串而非 `null`（前端统一按空值渲染占位符）。
