# Spec Delta: add-dashboard-alarm-trend

## 变更性质
**新增 path + 响应体**：新增 `GET /api/v1/dashboard/alarm-trend`，返回近 24 小时每小时报警计数序列
`data = AlarmTrendPoint[]`（`hour`/`count`）。回填前端契约中已声明但后端缺失的前瞻桩。

## 新增 path

| method | path | 响应体 |
|---|---|---|
| GET | `/api/v1/dashboard/alarm-trend` | `Result<List<AlarmTrendPoint>>` |

## 响应体字段增量（相对原 Dashboard 域）

新增 DTO `AlarmTrendPoint`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `hour` | string `"08:00"` | 小时起点标签（24 桶之一） |
| `count` | int | 该小时 `fac_alarm`(deleted=0) 行数；无报警为 0 |

## 前端契约影响
- `dashboard.openapi.json` 已含 `alarm-trend` path 与 `AlarmTrendPoint`，本 Change 为后端实现回填，**无需改前端契约**。
- 跨库四同步：openspec（本 Change）→ 前端契约（已存在，无需改）→ 后端实现（本次）→ 通知前端（无需重生成类型，AlarmTrendPoint 已定义）。
- 脚本复核后该 path 自「契约有/实现无」移入「已对齐」，差异 14→13。
