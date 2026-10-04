# Proposal: 安防人员·车辆检索与周界告警 CRUD（P2 批次 1）

## 问题

安防域此前只有**只读**检索与两类既有写能力：`GET /security/search/person|vehicle`（模糊检索）、
`GET /security/search/{person|vehicle}/{id}`（详情）、周界告警的 `POST`（`security:perimeter-create`）
与 `PUT`（`security:perimeter-ack` 写回）。管理端「人员登记 / 车辆登记」台账与周界告警列表
**无法编辑、无法销条**：

1. **无更新 / 删除入口**：识别记录录错（车牌识别失败、访客信息补全）只能改库，误录记录无法销条。
2. **周界告警无删除**：`POST` 创建后无法反悔，误建告警会长期停留在态势面板的「待处置」里。
3. **无并发保护**：`fac_person_search` / `fac_vehicle_search` 缺版本列，多人同时编辑会静默后写覆盖。

## 目标

扩展既有 `SecurityController` / `SecurityService`，把安防检索做成**全套 CRUD**，并补齐周界删除：

- 人员：`POST /security/search/person`、`PUT /security/search/person/{id}`、`DELETE /security/search/person/{id}`，
  权限码 `security:person-write`，`@RealtimeSync(domain = "security.person-search")`。
- 车辆：`POST /security/search/vehicle`、`PUT /security/search/vehicle/{id}`、`DELETE /security/search/vehicle/{id}`，
  权限码 `security:vehicle-write`，`@RealtimeSync(domain = "security.vehicle-search")`。
- 周界：`DELETE /security/perimeter-alarms/{id}`，权限码 `security:perimeter-delete`，
  `@RealtimeSync(domain = "security.perimeter-alarm")`（复用既有域）。
- 写请求 DTO：`PersonSearchWriteRequest`（13 字段，`name` `@NotBlank` 必填）、
  `VehicleSearchWriteRequest`（15 字段，`plate` `@NotBlank` 必填，`confidence` 为 `Integer` 可空）。
- V100 三方言迁移：在 `fm-security` 父菜单下登记按钮级菜单 `security:person-write` /
  `security:vehicle-write` / `security:perimeter-delete` 并授权 ADMIN 及岗位角色，
  同时给 `fac_person_search` / `fac_vehicle_search` 增加 `version BIGINT DEFAULT 0` 乐观锁列。
- 契约四同步：前端 `docs/api/security.openapi.json` 为唯一真源，本端**不得复制第二份 OpenAPI**；
  由 `check-api-contract.mjs --strict` 守门（目标 0 差异）。

## 非目标

- 不改只读检索接口的字段集与匹配语义（`keyword` 服务端模糊匹配保持）。
- 不动周界告警既有 `PUT` 写回的权限码（保持 `security:perimeter-ack`）。
- 不新增 `fac_perimeter_alarm` 的删除软标记：本次为**真删除**，与消防报警删除（`fire-alarm:delete`）同范式。
- 不引入分页 / 批量写接口。

## 影响面

- 新增 `dto/PersonSearchWriteRequest.java`、`dto/VehicleSearchWriteRequest.java`。
- `SecurityService` 新增 `createPerson/updatePerson/deletePerson`、`createVehicle/updateVehicle/deleteVehicle`、
  `deletePerimeterAlarm`，均标注 `@RealtimeSync`。
- `SecurityController` 新增 3 + 3 + 1 个写端点，全部 `@RequireAuth(perm=...)`。
- 三方言迁移 `V100__security_search_crud_perm.sql`（h2 / postgresql / dameng 同名同号）。
- 数据影响：`fac_person_search` / `fac_vehicle_search` 的增删改（真删除）+ 2 张表新增 `version` 列；
  `fac_perimeter_alarm` 新增删除能力，无 DDL。
- 回退：移除 7 个写端点与 V100 种子、回退 `version` 列即可；前端恢复为只读台账。

## Capabilities

- `security`（安全防恐）：新增「人员登记 / 车辆登记 全量 CRUD」与「周界告警删除」能力，
  复用既有 `security.perimeter-alarm`、新增 `security.person-search` / `security.vehicle-search` 实时广播域。
