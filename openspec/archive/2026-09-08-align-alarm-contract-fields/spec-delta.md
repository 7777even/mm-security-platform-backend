# Spec Delta: align-alarm-contract-fields

## 变更性质
**无新增 path / method**，仅对齐 `GET /api/v1/alarms` 响应体 `data.list[]` 元素的字段结构，使其与 `frontend-scaffold/docs/api/alarm.openapi.json` 的 `AlarmItem` 完全一致。

## 响应体字段增量（相对原 `FacAlarm` 序列化）

新增 / 改名（相对原 `FacAlarm` 直接序列化）：

| 字段 | 来源 | 说明 |
|---|---|---|
| `alarmId` | `alarm_id` 列 | 新增业务 ID，替代原 `id`(Long) |
| `status` | `status`(int) 映射 | 由 int 改为 string 枚举 ACTIVE/ACKED/DISPATCHED/CLOSED |
| `ts` | `occurred_at` | 时间字段别名 |
| `description` | `content` | 描述字段别名 |
| `location` | `location` 列 | 新增 |
| `category` | `category` 列 | 新增 |
| `warned` | `warned` 列 | 新增 |
| `planId` | `plan_id` 列 | 新增 |

移除（不再对外暴露）：`id`(Long)、`content`、`occurred_at`、`created_at`、`deleted`。

## 前端契约影响
- `alarm.openapi.json` 已是真实契约，无需改动 path；本 Change 使其后端实现与契约字节级对齐。
- 跨库四同步：openspec（本 Change）→ 前端契约（已对齐，无需改）→ 后端实现（本次）→ 通知前端重生成类型（无需，前端已定义 AlarmItem）。
