# Spec Delta: 应急事件与流程填报写端点

## ADDED Requirements

### Requirement: 应急事件编辑端点

系统应提供 `PUT /api/v1/emergency-events/{id}`，对既有应急事件做局部更新。

- **权限**：需 `emergency:event:write`（V92 登记，授权 ADMIN / COMMANDER / SCHEDULER）。
- **语义**：请求体中为 `null` 的字段表示不修改；`status` 取值必须在
  `{pending, processing, done}` 内，非法返回 B3 `PARAM_INVALID`（HTTP 200 + `code != 0`）。
- **联动**：只传 `status` 时按枚举推导 `statusLabel`。
- **双源同步**：同事务同步 `fac_accident_incident` 与其详情字段中
  事故时间 / 事发地点 / 事件描述 / 事件名称 / 事件级别 / 涉事区域 六项（按 label 命中才更新，不新增行）。
- **广播**：成功写入后发布 `emergency.event` 域变更。

#### Scenario: 局部更新且只传状态

- **WHEN** 请求体为 `{"status":"processing"}`
- **THEN** 事件 `status` 变为 `processing`、`statusLabel` 变为「处置中」，
  其余字段（location / description 等）保持原值。

#### Scenario: 状态取值非法

- **WHEN** 请求体为 `{"status":"unknown-status"}`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何更新。

#### Scenario: 事件不存在

- **WHEN** 请求 `PUT /api/v1/emergency-events/999`（库内无此 id）
- **THEN** 返回 B3 `NOT_FOUND`。

### Requirement: 应急事件删除端点

系统应提供 `DELETE /api/v1/emergency-events/{id}`，删除事件及其关联数据。

- **权限**：需 `emergency:event:write`。
- **级联顺序**：先删 `fac_accident_detail_field`，再删 `fac_accident_incident`，最后删 `fac_emergency_event`。
- **不存在**：返回 B3 `NOT_FOUND`（重复删除同此，不静默成功）。
- **广播**：成功后发布 `emergency.event` 域变更。

#### Scenario: 级联清理顺序

- **WHEN** 删除一个带关联救援行与详情字段的事件
- **THEN** 三次删除按「详情字段 → 救援行 → 事件」顺序执行，不残留孤儿行。

### Requirement: 流程填报删除端点

系统应提供 `DELETE /api/v1/form-records/{id}`，需 ADMIN 角色（与既有 `PUT` 同口径）。

- 记录不存在（含重复删除）返回 B3 `NOT_FOUND`。
- `fac_form_record` 无 `deleted` 列，故为物理删除。
- 广播 `form.record` 域变更。

### Requirement: 流程填报新增必须显式分配主键

`FormRecordService.create` 应为新行显式分配 `max(id) + 1`，不得依赖数据库自增序列。

原因：V66 种子以**显式 id（1、2）** 插入 `fac_form_record`，而 H2 / PostgreSQL / 达梦的
自增序列都**不会**因显式插入而推进，于是新增行仍从 `id=1` 起跳撞主键 → create 恒定 409，
dev 环境「新建填报」按钮彻底不可用（真机复现：库内仅 2 条种子，三种 payload 全部 409）。

#### Scenario: 种子显式插 id 后仍能新增

- **WHEN** 库内最大 id 为 2（来自种子）
- **THEN** 新增记录分配 `id=3` 并插入成功。

#### Scenario: 空表新增

- **WHEN** 表内无记录
- **THEN** 新增记录分配 `id=1`。

### Requirement: 应急事件与流程填报写路径实时广播

`EmergencyEventService` 的 `create / report / startResponse / update / delete` 与
`FormRecordService` 的 `create / update / delete` 均应标记 `@RealtimeSync`，
分别广播 `emergency.event` 与 `form.record` 域。

#### Scenario: 三端同源

- **WHEN** 任一端写入应急事件（新增 / 报送 / 启动响应 / 编辑 / 删除）
- **THEN** 订阅了 `emergency.event` 的页面在 400ms 内自动重拉，无需手动刷新。

## MODIFIED Requirements

### Requirement: 应急事件写端点权限口径（分层说明）

既有 `POST /emergency-events`、`POST /{id}/report`、`POST /{id}/start-response`
维持「仅登录态」口径不变——它们是大屏值守岗自助上报链路，收权限码会打断既有业务。
仅 `update / delete` 收 `emergency:event:write`。
