# Design: add-dashboard-alarm-trend

## 字段映射决策表

| 前端 AlarmTrendPoint 字段 | 类型 | 后端来源 | 处理方式 |
|---|---|---|---|
| `hour` | string `"08:00"` | 分桶小时起点格式化 | `String.format("%02d:00", hourStart.getHour())` |
| `count` | integer | `fac_alarm` 该小时行数 | 单条 `selectList` 后在 Java 侧按小时聚合 |

## 架构

- **新增 `AlarmTrendPoint` DTO**（`dto/` 包）：`hour`(String) / `count`(int)，与 `dashboard.openapi.json#/AlarmTrendPoint` 1:1。
- **`DashboardService.trend24h(LocalDateTime now)`**（复用已有 `alarmMapper`）：
  1. 计算窗口：`endHour = now` 截断到小时；`startHour = endHour - 23h`；窗口 `[startHour, endHour+1h)`。
  2. `selectList`：`fac_alarm`(deleted=0, occurred_at ≥ startHour, occurred_at < endHour+1h)。
  3. 在 Java 侧按「小时起点」分 24 桶，`Map<Integer,Integer>` 累加；`Duration.between(startHour, occurredAt.truncatedTo(HOURS)).toHours()` 得桶索引（0..23）。
  4. 输出固定 24 个点（桶序 0..23），无报警小时 `count=0`，标签 `HH:00`。
- **`DashboardController`** 新增 `@GetMapping("/alarm-trend")`：`dashboardService.trend24h(LocalDateTime.now())` → `Result.ok(list)`。
- `now` 由调用方（Controller）传入，`trend24h` 自身不藏时钟状态，便于单测注入固定时间。

## 为什么不写 SQL `GROUP BY` / `DATE_FORMAT`
- dev 用 H2、生产可能是 PG/MySQL，日期函数方言不同（`DATE_FORMAT` vs `TO_CHAR` vs H2 `FORMATDATETIME`），与 AGENTS §6.4「不写死方言」冲突。
- 近 24h 报警量小，单条 `selectList` + Java 分桶零方言风险、零额外依赖，且 24 桶逻辑单测可确定性覆盖。

## 契约同步
- 本 Change 仅**新增** path `/api/v1/dashboard/alarm-trend`，前端 `dashboard.openapi.json` 已有该 path 与 `AlarmTrendPoint`，属「契约有/实现无」回填，无需改前端契约。
- 跑 `scripts/check-api-contract.mjs` 后该 path 应进入「已对齐」，差异 14→13。
- 跨库四同步：openspec（本 Change）→ 前端契约（已存在，无需改）→ 后端实现（本次）→ 通知前端（无需重生成类型，AlarmTrendPoint 已定义）。
