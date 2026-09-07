# Retro · 新增 Dashboard 报警趋势端点 alarm-trend（2026-09-07）

## 做了什么
开了 `openspec/changes/add-dashboard-alarm-trend/` 四件套，并实现 `GET /api/v1/dashboard/alarm-trend`：
返回近 24 小时每小时报警数（`List<AlarmTrendPoint>`，`hour` 整点字符串 + `count`）。这是消化 14 项
「契约有/实现无」前瞻桩的第一项（选只读聚合、零新表、零方言风险，与 #16 真实聚合工作同构）。

## 做对了
- **挑最连贯的桩先吃**：14 项里 `alarm-trend` 是纯只读聚合，复用已存在的 `fac_alarm`，无 schema 变更、
  无写路径、无权限边界变化，回归面最小，适合作为「消化桩」的开胃菜建立节奏。
- **分桶在 Java 侧而非 SQL**：单条 `selectList`（窗口 `[now-23h, now+1h)`）+ `Duration.between(start,
  occ.truncatedTo(HOURS)).toHours()` 算桶索引，规避 `DATE_FORMAT`/`TO_CHAR` 方言差异，符合
  `AGENTS.md §6.4`「不写死方言」。量小（近 24h 报警），内存分桶成本可忽略。
- **24 桶补零**：即使某小时无报警也返回 `count=0`，前端拿到完整 24 点序列，避免折线图缺段/错位。
- **契约顺序正确**：先读前端 `dashboard.openapi.json` 确认字段（`hour` 形如 `"08:00"`、`count` 为 int），
  再写 DTO/Service/Controller，脚本复核即进「已对齐」，无需回改前端、无需重生成类型。
- **测试基线同构**：2 个 Service 用例（分桶+补零、空窗全 0）+ 1 个端点用例（包络 `code=0` + 字段断言），
  纯 Mockito 不起 Spring，沿用 #8 纪律；基线 35→38 全绿。

## 可以更好
- **窗口边界表达**：当前窗口是 `[now-23h, now+1h)`（覆盖 now 所在整点及其后一小时），桶标签用
  `startHour.plusHours(i).getHour()` 取「小时」字段。若业务期望「自然整点对齐 24 桶」（如固定 00:00–23:00），
  需与前端确认 `hour` 语义，避免「看起来对齐实则有 1 小时错位」。
- **实时性**：趋势为请求时聚合，非预计算。当前量小可接受；若日后报警量上涨，可考虑物化/缓存 5 分钟窗口。
- **跨库四同步提醒**：本轮仅动后端实现 + 测试 + OpenSpec，前端契约本已声明完全相同字段，无需重生成；
  但若后续要把 `AlarmTrendPoint` 也作为前端 TS 类型消费，仍走「通知前端重生成类型」一步闭环。

## 行动项
- [x] 消化 14→13：alarm-trend 已落地并验证（差异 14→13，实现超前 0）
- [ ] 待开 Change：剩余 13 项前瞻桩（alarm CRUD POST/PUT/DELETE、risk-heatmap、emergency/map/uplink）
- [ ] 候选下一步：risk-heatmap（同为只读聚合，与 alarm-trend 同构，可沿用本 Change 模板）
- [ ] P3：生产库在线迁移引入 Flyway（fac_alarm 等表当前仅 H2 重建）
