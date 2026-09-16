# Spec Delta: drills-domain（应急演练域）

## ADDED Requirements

### Requirement: 应急演练只读台账
系统 SHALL 提供应急演练只读查询（列表含任务数 + 按 id 详情含任务子项），数据源为只读表 `fac_drill` / `fac_drill_task`。

#### Scenario: 演练列表
- **WHEN** 已登录用户请求 `GET /api/v1/drills`
- **THEN** 返回全部演练（按 id 升序）与总数，每项含 `taskCount`（`Result<DrillList>`）

#### Scenario: 演练详情命中
- **WHEN** 请求 `GET /api/v1/drills/{id}` 且 `id` 存在
- **THEN** 返回演练详情与 `tasks[]` 子项（`Result<DrillDetail>`）

#### Scenario: 演练详情未命中
- **WHEN** 请求 `GET /api/v1/drills/{id}` 且 `id` 不存在
- **THEN** 返回业务码 404（HTTP 200 + B3 包络）

### Requirement: 只读边界
应急演练域 SHALL 仅提供查询能力，不含演练执行下发动作。
