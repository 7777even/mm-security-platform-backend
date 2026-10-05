# Tasks: 归档 spec-delta 回填至 capability spec

- [x] 量化缺口并定位根因（spec-delta 未回填，非需求缺失）——`scope-inventory.md §3`
- [x] 新建 Change 四件套（proposal / design / tasks / spec-delta）
- [x] 回填 `fire-facility`（15 端点，源：fault-crud / P2 台账 / alarm-handle）
- [x] 回填 `fire-monitoring`（12 端点，源：P2 防火巡查 / monitor-report）
- [x] 回填 `fire-situation`（3 端点，源：fire-facility-zone-unification）
- [x] 回填 `fire-alarm`（4 端点，源：writeback / disposal-persist / crud）
- [x] 回填 `emergency-plan`（16 端点，源：plan-crud / plan-catalog-crud）
- [x] 回填 `hazard`（11 端点，源：hazard-special-operation-crud-realtime）
- [x] 回填 `special-operation`（5 端点，源：同上）
- [x] 回填 `security`（29 端点含黑名单，源：gate-bollard / security-crud / perimeter-maintenance-closure）
- [x] 回填 `typhoon-emergency`（7 端点，源：mgmt-p0-batch-crud 台风调度部分）
- [x] 回填 `video`（14 端点，源：mgmt-monitor-video-comm-crud-realtime）
- [x] 回填 `weather`（1 端点，无 delta 来源，按契约 + 实现反推并在 spec 内标注）
- [x] 补充 `emergency-reference`（通讯录 / 知识库 / 事故案例 / 应急指令 / 值班签到写端点）
- [x] 重跑 `scripts/gen-scope-inventory.py` 刷新 §1 / §2 / §3 并核对缺口下降
- [x] 跑 `check-openspec-hygiene` 与 `check-api-contract --strict` 确认无回归
- [x] 全勾后 `git mv` 至 `openspec/archive/2026-10-06-spec-delta-backfill` 并按 scope 提交推送
