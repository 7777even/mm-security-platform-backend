# Design: fire-alarm-disposal-persist

## 数据建模
前端详情面板持有 4 字段的本地形态与后端列形态不一致，需在边界做转换：

| 前端（AlarmDetailItem）        | 后端列                       | 转换规则                                   |
| ----------------------------- | ---------------------------- | ------------------------------------------ |
| `handleResult: string`        | `handle_result VARCHAR(1024)`| 直通（可空字符串）                         |
| `handleTime: string`          | `handle_time VARCHAR(32)`    | 直通（yyyy-MM-dd HH:mm:ss，可空）          |
| `dispatchPersonnel: string[]` | `dispatch_personnel VARCHAR(512)` | 写：`join(',')`；读：`split(',').filter(Boolean)` |
| `notifyApp/notifySms: boolean`| `notify_method VARCHAR(32)`  | 写：`[APP?,SMS?].filter.join(',')`；读：`includes('APP'/'SMS')` |

选择「逗号分隔串」而非拆多列的原因：派单人员是开放式名单（来自 `/emergency/dispatch-personnel` 动态选项），
通知方式仅有 APP/短信两态；用单列分隔串避免表结构随选项膨胀，且 read-modify-write 局部更新语义清晰。

## 写回语义
复用 `FireAlarmService.update` 既有 read-modify-write + `@Version` 乐观锁：4 个字段**仅当请求体非空时覆盖**，
不传则不更新（与 status/falseAlarm 同策略）。不做服务端校验（自由文本），`null` 入参即"不更新"。

## 迁移策略（三方言）
- h2 / postgresql：`ALTER TABLE fac_fire_alarm ADD COLUMN <col> VARCHAR(n);`
- 达梦 DM8（Oracle 兼容）：`ALTER TABLE fac_fire_alarm ADD <col> VARCHAR2(n CHAR);`（DM 不支持 `IF NOT EXISTS`，
  且 V5/V9 已应用，故必须新开 V65，绝不改已应用文件）。
- 当前各方言最高版本均为 V64 → 新脚本统一命名 `V65__fire_alarm_disposal_fields.sql`。

## 契约对齐
`FireAlarmUpdateRequest` / `FireAlarmItem` 字段名与前端 OpenAPI `docs/api/fire-alarm.openapi.json` 完全一致
（守门脚本 `check-api-contract.mjs` 按同名 DTO 逐字段对拍）。前端 `FireAlarmUpdatePayload` 一并扩展对应 4 字段。
