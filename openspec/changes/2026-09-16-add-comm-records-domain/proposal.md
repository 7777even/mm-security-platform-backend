# Proposal: 新增通讯通知记录域（comm-records，后台管理端通讯通知管理）

## Why
`public/pc-admin` 原型的 `/comm-sms`、`/comm-call`、`/comm-broadcast`、`/comm-push`、`/comm-intercom`
五个记录页在 mgmt 端为 `module-embed` iframe 占位（见 `docs/frozen-prototype.md` 冻结清单），
冻结原因是 `CommDeviceController` 仅有通讯设备、无通知记录端点。按跨库四同步补齐记录端点后解除冻结。

## What Changes
- Flyway **V57**（h2 / dameng / postgresql 三方言）新增只读表 `fac_comm_record` + 15 条种子（五类各 3 条）。
- 后端新增 `CommRecordController`（`GET /api/v1/communication/records?type=`）/ `CommRecordService` /
  `FacCommRecordMapper` / `FacCommRecord` 实体 / `dto.CommunicationRecord`、`dto.CommunicationRecordList`。
- 契约真源**扩展** `docs/api/communication.openapi.json`（与既有 devices 端点同域，不新建域文件）。
- 前端 mgmt 五页改接 `@/services/communication` 的 `fetchCommunicationRecords`。

## Capabilities
- 「通讯通知管理」五类记录（短信/电话通话/广播播报/APP推送/语音对讲）由 `fac_comm_record` 表驱动（纯只读）。
- **语义边界**：仅记录查询，不含任何发送/外呼/播报动作；零下行控制红线 `HardControlPaths` 不变。

## Impact
- 仅新增表 + 端点。`/api/v1/communication` 前缀下与 `CommDeviceController` 的 `/devices` 并存、路径不重叠。
- 达梦 / PostgreSQL 方言沿现状「静态同步维护、未实跑」状态，待 DM8/PG 实例 `flyway migrate` 校验。
- 列名用 `result_text`（而非 `result`）：回避方言保留字风险，Java 字段仍为 `result`（`@TableField` 映射），
  与 V54 `task_level` 同范式。
