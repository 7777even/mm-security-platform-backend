# communication Specification

## Purpose
通讯通知能力：以只读表 `fac_comm_record` 承接短信 / 电话通话 / 广播播报 / APP推送 / 语音对讲五类通讯通知记录的查询，供后台管理端与移动端「通讯通知」页渲染。设计依据见 `openspec/archive/2026-09-16-add-comm-records-domain/design.md`。

## Requirements

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

### Requirement: 只读边界与空值约定

- 纯只读：本域 SHALL NOT 提供任何写端点，不含发送 / 外呼 / 播报动作（零下行控制红线不变）。
- 各类型记录按需填充字段，未使用字段 SHALL 返回空字符串而非 `null`（前端统一按空值渲染占位符）。
