# Spec Delta: 安防设备台账 CRUD——道闸与防恐柱

## Capability: security（安全防恐）

### ADDED — 道闸台账新增 / 编辑 / 删除

- 系统 SHALL 提供 `POST /api/v1/security/gate-controls`，校验权限码 `security:gate-write`，`name` 缺失或为空时返回 400 校验失败，成功返回落库后的 `GateControlItem`。
- 系统 SHALL 提供 `PUT /api/v1/security/gate-controls/{id}`（覆盖更新，不含 `status`），`@Version` 冲突时返回 409。
- 系统 SHALL 提供 `DELETE /api/v1/security/gate-controls/{id}`（真删除），不存在返回 404。
- 三个写操作成功后 SHALL 广播 `security.gate-control` 域变更事件。

### ADDED — 防恐柱台账新增 / 编辑 / 删除

- 系统 SHALL 提供 `POST/PUT/DELETE /api/v1/security/bollards[/ {id}]`，校验权限码 `security:bollard-write`，`name` 缺失或为空时返回 400 校验失败；PUT 覆盖更新，不含 `status`。
- 三个写操作成功后 SHALL 广播 `security.bollard` 域变更事件。

### ADDED — 零下行控制

- 道闸/防恐柱写请求 DTO SHALL 不含 `status` 字段；`status` 为设备实时状态，仅读不写，系统 MUST NOT 接受对 `status` 的写回。

### ADDED — 乐观锁

- `fac_gate_control` / `fac_bollard` SHALL 带 `version` 列，更新走 `@Version` 乐观锁，并发冲突返回 409，禁止静默后写覆盖。

#### Scenario: 道闸新增广播

- **GIVEN** 操作员具备 `security:gate-write`
- **WHEN** 提交 `POST /security/gate-controls`（`name="1#门-道闸1"`）
- **THEN** 落库返回带主键的 `GateControlItem`，并广播 `security.gate-control.changed`

#### Scenario: 必填校验拦截

- **WHEN** 提交 `POST /security/bollards` 且 `name` 为空串
- **THEN** 返回 B3 `code=400`，不写库、不广播

#### Scenario: 零下行控制

- **WHEN** 任何道闸/防恐柱写请求携带 `status` 字段
- **THEN** 该字段 MUST 被忽略（不在 DTO 内），设备实时状态不被覆盖

#### Scenario: void 删除仍广播

- **WHEN** `deleteGate(id)` / `deleteBollard(id)` 执行成功
- **THEN** 尽管方法返回 void，仍 MUST 广播对应域变更事件（前端据此重拉）
