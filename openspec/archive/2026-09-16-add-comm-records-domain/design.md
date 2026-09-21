# Design: 通讯通知记录域

## 数据模型
单表 `fac_comm_record`，以 `record_type` 区分五类记录：`sms` / `call` / `broadcast` / `push` / `intercom`。

五类记录字段语义同构（谁、何时、经何通道、对谁、内容、结果），差异项收敛为三列：
- `channel`：信道 / 关联设备 / 业务通道（对讲 CH-3、广播 BC-D-01、推送业务类型）
- `direction`：呼叫方向（呼入 / 外呼 / 组呼 / 单呼；广播为定时 / 手动）
- `content_type`：内容类型（文本 / 语音）

## 为什么不建 5 张表 / 5 个 schema
五类结构高度同构，且一期仅列表只读展示。单表 + 统一 schema 可省去 5 套 DTO / 端点 / 契约的重复，
前端按当前页签取用所需列。若后续某类记录长出独立字段（如短信计费、通话录音地址），再按类型拆分。

## 端点
`GET /api/v1/communication/records?type=`：type 缺省返回全部五类，便于前端一次拉取后切换；
也可按类型单取。后端**不做分页**（记录量小、原型无分页器），`total` 即 items 长度。

## 列名与保留字
`result` 在部分方言存在保留字风险 → 列名 `result_text`，Java 字段 `result` 用 `@TableField` 映射
（与 V54 `task_level` 同范式，避免重演 H2 `value`/`command`/`type` 的 500 坑）。

## 与既有通讯域的关系
`CommDeviceController` 负责通讯**设备**（广播/电话/对讲三类设备台账 + 地图撒点），
`CommRecordController` 负责通讯**记录**，二者共用 `/api/v1/communication` 前缀、路径不重叠。
