# Spec Delta: 消防设施台账与防火巡查 CRUD（P2 批次 3）

## Capability: fire-facility（消防设施台账）

### ADDED — 台账新增 / 编辑 / 删除

- 系统 SHALL 提供 `POST /api/v1/fire-facility/ledger`，校验权限码 `fire-facility:ledger:write`，设施编码重复时返回 B3 `code=409`，成功返回落库后的 `FireFacilityLedgerItem`。
- 系统 SHALL 提供 `PUT /api/v1/fire-facility/ledger/{id}`（覆盖更新，不含 `status`），不存在返回 404。
- 系统 SHALL 提供 `DELETE /api/v1/fire-facility/ledger/{id}`（级联清理维保记录后真删除），不存在返回 404。
- 三个写操作成功后 SHALL 广播 `fire-facility.ledger` 域变更事件。

### ADDED — 零下行控制

- 消防设施台账写请求 DTO SHALL 不含 `status` 字段；`status` 为设备实时状态，仅读不写，系统 MUST NOT 接受对 `status` 的写回。

## Capability: fire-monitoring（防火巡查记录）

### ADDED — 巡查记录新增 / 编辑 / 删除

- 系统 SHALL 提供 `POST /api/v1/fire/patrols`，校验权限码 `fire:patrol-write`，`patrolDate` 缺失或为空时返回 400 校验失败，成功返回落库后的 `FirePatrolRecord`。
- 系统 SHALL 提供 `PUT /api/v1/fire/patrols/{id}`（覆盖更新，不含 `status`），`@Version` 冲突时返回 409。
- 系统 SHALL 提供 `DELETE /api/v1/fire/patrols/{id}`（真删除），不存在返回 404。
- 三个写操作成功后 SHALL 广播 `fire.patrol-record` 域变更事件。

### ADDED — 乐观锁

- `fac_fire_patrol` SHALL 带 `version` 列，更新走 `@Version` 乐观锁，并发冲突返回 409，禁止静默后写覆盖。

### ADDED — 零下行控制

- 巡查写请求 DTO SHALL 不含 `status` 字段与检查项结果（`checkItems` 本轮只读），检查项结果 MUST NOT 经由写请求写回。

#### Scenario: 台账新增广播

- **GIVEN** 操作员具备 `fire-facility:ledger:write`
- **WHEN** 提交 `POST /fire-facility/ledger`（`facilityCode="F001"`、`facilityName="消防水泵"`、`facilityType="消防水泵"`）
- **THEN** 落库返回带主键的 `FireFacilityLedgerItem`，并广播 `fire-facility.ledger.changed`

#### Scenario: 必填校验拦截

- **WHEN** 提交 `POST /fire-facility/ledger` 且 `facilityName` 为空串
- **THEN** 返回 B3 `code=400`，不写库、不广播

#### Scenario: 零下行控制

- **WHEN** 任何台账/巡查写请求携带 `status` 字段
- **THEN** 该字段 MUST 被忽略（不在 DTO 内），设备实时状态不被覆盖

#### Scenario: void 删除仍广播

- **WHEN** `deleteLedger(id)` / `deleteFirePatrol(id)` 执行成功
- **THEN** 尽管方法返回 void，仍 MUST 广播对应域变更事件（前端据此重拉）
