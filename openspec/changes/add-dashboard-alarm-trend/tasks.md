# Tasks: add-dashboard-alarm-trend

## 实现步骤

- [ ] 1. 新建 `dto/AlarmTrendPoint.java`：`hour`(String) / `count`(int)，对齐前端 AlarmTrendPoint
- [ ] 2. `DashboardService` 增加 `trend24h(LocalDateTime now)`：复用 `alarmMapper`，窗口 `[now-23h, now+1h)`，`selectList`(deleted=0, occurred_at 区间)，Java 侧按小时分 24 桶（无报警补 0），返回 `List<AlarmTrendPoint>`
- [ ] 3. `DashboardController` 增加 `@GetMapping("/alarm-trend")`：`trend24h(LocalDateTime.now())` → `Result.ok(list)`
- [ ] 4. 测试：`DashboardServiceTest.trend24h`（固定 now，断言 24 点 + 各小时 count）、`DashboardControllerTest` 端点断言（hour/count 字段）
- [ ] 5. `mvn test` 全绿（基线 35 → 37 case）
- [ ] 6. `scripts/check-api-contract.mjs`：确认 `/dashboard/alarm-trend` 进入「已对齐」，实现有/契约无=0，差异 14→13
- [ ] 7. 提交：按 scope `dashboard` / `test` / `docs`(openspec 四件套 + QA/Retro) 拆分

## 验收标准（DoD）

- [ ] `GET /api/v1/dashboard/alarm-trend` 返回 `code=0` + `data` 为 24 个 `AlarmTrendPoint`，`hour` 形如 `"08:00"`、`count` 为 int
- [ ] 近 24h 无报警的小时 `count=0`（前端拿到完整 24 点序列，不缺桶）
- [ ] `mvn test` 0 failure
- [ ] `scripts/check-api-contract.mjs` 无新增 path 漂移（实现有/契约无 = 0；差异 14→13）
- [ ] QA + Retro 写入 `engineering/`
