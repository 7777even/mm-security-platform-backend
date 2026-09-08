# Proposal: align-alarm-contract-fields

## 问题
后端 `FacAlarm` 与前端契约 `AlarmItem`（`frontend-scaffold/docs/api/alarm.openapi.json`）字段严重失配：

- `alarmId`(string) ↔ `id`(Long) —— 命名+类型都不匹配
- `status`(string 枚举 ACTIVE/ACKED/DISPATCHED/CLOSED) ↔ `status`(int 0/1/2/3)
- `ts`(date-time) ↔ `occurred_at`
- `description` ↔ `content`
- **缺字段**：`location` / `category` / `warned` / `planId` 后端完全没有
- `type` 枚举不一致：后端 {FIRE,GAS,FLOOD,INTRUSION,TEMP} vs 前端 {FIRE,GAS,TEMP,CCTV,SOS}

导致前端按 `AlarmItem` 解析时大量字段 `undefined`，演示数据也无法正确展示。

## 目标
让 `GET /api/v1/alarms` 返回的 `data.list[]` 元素**完全对齐**前端 `AlarmItem`，零 `undefined` 字段。不改任何 path/method（仅改返回结构）。

## 非目标
- 不新增 alarm 的 POST/PUT/DELETE 端点（属前瞻桩，后续 Change）
- 不改其他域（device/dashboard 已对齐或独立）

## 影响面
- `schema.sql`：`fac_alarm` 加列（迁移）
- `FacAlarm` 实体：加字段
- 新增 `AlarmItem` DTO + `FacAlarm→AlarmItem` 转换
- `AlarmPageResult.list` 由 `FacAlarm` 改为 `AlarmItem`
- `data.sql` 种子：补新字段，`type` 统一为前端枚举
- 测试：断言 `AlarmItem` 字段
