# Spec Delta: 应急预案主记录台账写端点

## ADDED Requirements

### Requirement: 应急预案主记录写端点

系统应为 `fac_emergency_plan` 主记录提供 `POST /api/v1/emergency-plans`、`PUT` 与 `DELETE /api/v1/emergency-plans/{id}`。

- **权限**：需 `emergency:plan:write`（V98 登记，授权 ADMIN / COMMANDER / SCHEDULER）。
- **新增必填**：`planName`；缺失或空白返回 B3 `PARAM_INVALID`（文案点名 `planName`）。
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改。
- **删除**：物理删除；不存在（含重复删除）返回 B3 `NOT_FOUND`。
- **主键**：新增行 id 取当前 `max(id) + 1`。
- **广播**：成功写入后发布 `emergency.plan` 域变更。

#### Scenario: 局部更新不覆盖未传字段

- **WHEN** `PUT /api/v1/emergency-plans/1` 请求体为 `{"facility":"乙烯罐区"}`
- **THEN** 该行 `facility` 变「乙烯罐区」，`planName`/`domain` 保持原值。

#### Scenario: 新增必填缺失

- **WHEN** `POST /api/v1/emergency-plans` 未传 `planName`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何插入。

#### Scenario: 重复删除

- **WHEN** 对同一已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）。
