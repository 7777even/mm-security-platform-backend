# Spec Delta: tasks-domain（处置任务域）

## ADDED Requirements

### Requirement: 处置任务只读台账
系统 SHALL 提供处置任务只读查询（列表 + 按 id 详情），数据源为只读表 `fac_dispatch_task`。

#### Scenario: 任务列表
- **WHEN** 已登录用户请求 `GET /api/v1/tasks`
- **THEN** 返回全部处置任务（按 id 升序）与总数（`Result<TaskList>`）

#### Scenario: 任务详情命中
- **WHEN** 请求 `GET /api/v1/tasks/{id}` 且 `id` 存在
- **THEN** 返回该任务详情（`Result<TaskItem>`）

#### Scenario: 任务详情未命中
- **WHEN** 请求 `GET /api/v1/tasks/{id}` 且 `id` 不存在
- **THEN** 返回业务码 404（HTTP 200 + B3 包络 `{code:404,...}`）

### Requirement: 下行红线不变
处置任务域 SHALL 仅提供查询能力，不含任何物理下发动作；`HardControlPaths` 红线不被放宽。
