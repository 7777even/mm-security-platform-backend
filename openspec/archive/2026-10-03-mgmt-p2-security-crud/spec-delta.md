# Spec Delta: 安防人员·车辆检索与周界告警 CRUD

## Capability: security（安全防恐）

### ADDED — 人员识别检索记录新增 / 编辑 / 删除

- 系统 SHALL 提供 `POST /api/v1/security/search/person`，校验权限码 `security:person-write`，
  `name` 缺失或为空时返回 400 校验失败，成功返回落库后的 `PersonSearchDetail`。
- 系统 SHALL 提供 `PUT /api/v1/security/search/person/{id}`（全字段覆盖更新），
  `@Version` 冲突时返回 409。
- 系统 SHALL 提供 `DELETE /api/v1/security/search/person/{id}`（真删除），不存在返回 404。
- 三个写操作成功后 SHALL 广播 `security.person-search` 域变更事件。

### ADDED — 车辆识别检索记录新增 / 编辑 / 删除

- 系统 SHALL 提供 `POST/PUT/DELETE /api/v1/security/search/vehicle[/ {id}]`，校验权限码 `security:vehicle-write`，
  `plate` 缺失或为空时返回 400 校验失败；PUT 全字段覆盖，`confidence` 允许为空。
- 三个写操作成功后 SHALL 广播 `security.vehicle-search` 域变更事件。

### ADDED — 周界入侵告警删除

- 系统 SHALL 提供 `DELETE /api/v1/security/perimeter-alarms/{id}`，校验权限码 `security:perimeter-delete`，
  真删除；不存在返回 404；成功后广播 `security.perimeter-alarm` 域变更事件。

### ADDED — 乐观锁

- `fac_person_search` / `fac_vehicle_search` SHALL 带 `version` 列，更新走 `@Version` 乐观锁，
  并发冲突返回 409，禁止静默后写覆盖。

#### Scenario: 人员登记新增广播

- **GIVEN** 操作员具备 `security:person-write`
- **WHEN** 提交 `POST /security/search/person`（`name="张三"`）
- **THEN** 落库返回带主键的 `PersonSearchDetail`，并广播 `security.person-search.changed`

#### Scenario: 必填校验拦截

- **WHEN** 提交 `POST /security/search/vehicle` 且 `plate` 为空串
- **THEN** 返回 B3 `code=400`，不写库、不广播

#### Scenario: 并发编辑冲突

- **GIVEN** 两条 `PUT /security/search/person/{id}` 携带同一 `version`
- **WHEN** 第二条提交
- **THEN** 返回 409，库内记录保持第一次提交的结果

#### Scenario: void 删除仍广播

- **WHEN** `deletePerson(id)` / `deleteVehicle(id)` / `deletePerimeterAlarm(id)` 执行成功
- **THEN** 尽管方法返回 void，仍 MUST 广播对应域变更事件（前端据此重拉）
