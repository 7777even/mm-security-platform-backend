# QA — 新增 Dashboard 近 24 小时报警趋势端点（alarm-trend）

- 日期: 2026-09-07
- 效率等级: L3（新增只读端点，复用既有表，无 schema 变更）
- 范围:
  - 端点：`GET /api/v1/dashboard/alarm-trend`
  - DTO：`com.sinopec.mmsecurity.dto.AlarmTrendPoint`（新增）
  - Service：`DashboardService.trend24h(LocalDateTime)`（新增方法）
  - Controller：`DashboardController.alarmTrend()`（新增端点）
  - 测试：`DashboardServiceTest`（+2 case）、`DashboardControllerTest`（+1 case）
  - OpenSpec Change：`openspec/changes/add-dashboard-alarm-trend/`（四件套）

## 验收口径

- 依据 `AGENTS.md §3`（契约真源在前端 `dashboard.openapi.json`）：`GET /api/v1/dashboard/alarm-trend`
  返回 `B3 Result<List<AlarmTrendPoint>>`，`AlarmTrendPoint = { hour: "08:00"(string), count: int }`，
  语义为「近 24 小时每小时报警数」。
- 依据 `AGENTS.md §6.4`（不写死方言）：趋势分桶在 Java 侧完成（单条 `selectList` + `Duration.between`
  算桶索引），不写 `DATE_FORMAT` / `TO_CHAR` / `to_char`，规避 H2/PG/MySQL 方言差异。
- 依据 `AGENTS.md §5 DoD`：端点返回 `code=0` 才取 `data`；24 个整点桶补零（无报警小时 `count=0`），
  前端拿到完整 24 点序列。
- 依据 `AGENTS.md §3` 契约同步：仅新增 path，前端契约本已声明，无字段改动，无需重生成类型；
  脚本复核 `/dashboard/alarm-trend` 进入「已对齐」。

## 实际执行命令与结果

- `node scripts/check-api-contract.mjs`
  - 摘要：**实现 12 / 契约 24 / 已对齐 11 / 差异 13**（上轮基线差异 14 → 本轮 13）。
  - `GET /api/v1/dashboard/alarm-trend` 已列入「已对齐 (11)」。
  - **实现有 / 契约无 = 0**（无后端超前漂移）。
  - 外部网关 `gis.openapi.json`（2 项）不实现，符合预期。
- `JAVA_HOME='D:\jdk-17_windows-x64_bin\jdk-17.0.4.1' D:\apache-maven-3.9.11\apache-maven-3.9.11\bin\mvn.cmd -s ci-settings.xml test`
  - **Tests run: 38, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS**。
  - 基线 35 → 38：新增 `DashboardServiceTest` 2 case（`trend24h_bucketsByHourWithZeroPadding`、
    `trend24h_emptyWindow_returnsAllZeros`）+ `DashboardControllerTest` 1 case（`alarmTrend_returnsTrendPoints`）。
  - 分布：`DashboardServiceTest` 5 / `DashboardControllerTest` 3 / `AlarmSimulatorTest` 4 / 其余类稳定。

## 未运行项

- 带 DB 的 `*IT` 未执行：沿用基线纪律（standalone MockMvc + 纯 Mockito，不起 Spring 上下文），
  `trend24h` 用 `alarmMapper.selectList` 的 Mockito 桩覆盖窗口与分桶逻辑，不依赖真实 H2/PG。
- `smoke-test.ps1` 针对设备/告警分页链路，非本端点范围，未重跑（本变更为只读聚合，不影响既有链路）。

## 证据附件

- 契约校验快照：见本地执行输出，`/dashboard/alarm-trend` 在「已对齐」清单；差异 14→13。
- 测试快照：`DashboardServiceTest.trend24h_*` 断言 24 点序列 + 桶计数（now=2026-09-07T11:30，
  4 条报警落桶 20/21/21/23，预期 08:00=1、09:00=2、11:00=1、00:00=0、07:00=0、23:00=0，空窗全 0）。
- 端点测试：`DashboardControllerTest.alarmTrend_returnsTrendPoints` 断言 `$.data[0].hour="08:00"`、
  `count=3`、`$.data[1].hour="09:00"`、`count=12`，包络 `code=0`。
- OpenSpec 四件套：`openspec/changes/add-dashboard-alarm-trend/{proposal,design,tasks,spec-delta}.md`。

## 结论

- **达成**：端点实现、契约对齐（差异 14→13，实现超前 0）、测试基线 35→38 全绿，三项 DoD 全满足。
- 遗留项（非本变更范围）：剩余 13 项前瞻桩待各自 Change；`risk-heatmap` 为下一可读聚合候选。
