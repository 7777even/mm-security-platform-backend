# Tasks: 安防人员·车辆检索与周界告警 CRUD（后端）

## 权限与表结构（Task 1）

- [x] V100 三方言迁移 `V100__security_search_crud_perm.sql`（h2 / postgresql / dameng）：登记 `security:person-write` / `security:vehicle-write` / `security:perimeter-delete` 按钮菜单并授权 ADMIN 及岗位角色
- [x] `fac_person_search` / `fac_vehicle_search` 增加 `version BIGINT DEFAULT 0` 乐观锁列

## DTO（Task 2）

- [x] 新增 `dto/PersonSearchWriteRequest.java`（13 字段，`name` `@NotBlank`）
- [x] 新增 `dto/VehicleSearchWriteRequest.java`（15 字段，`plate` `@NotBlank`，`confidence` 为 `Integer` 可空）
- [x] 实体 `PersonSearch` / `VehicleSearch` 加 `@Version` 字段

## Service（Task 3）

- [x] `SecurityService` 新增 `createPerson` / `updatePerson` / `deletePerson`，标注 `@RealtimeSync(domain="security.person-search")`
- [x] 新增 `createVehicle` / `updateVehicle` / `deleteVehicle`，标注 `@RealtimeSync(domain="security.vehicle-search")`
- [x] 新增 `deletePerimeterAlarm`，标注 `@RealtimeSync(domain="security.perimeter-alarm")`

## Controller（Task 4）

- [x] `SecurityController` 新增人员 POST / PUT / DELETE 与车辆 POST / PUT / DELETE（`@RequireAuth` + `@Valid`）
- [x] 新增 `DELETE /security/perimeter-alarms/{id}`（权限 `security:perimeter-delete`）

## 契约四同步（Task 5）

- [x] 前端 `docs/api/security.openapi.json` 补 7 个写端点（同 path 多 method 合并）+ 两个 WriteRequest schema
- [x] `npm run gen:api-types` 重产 `src/types/generated/security.ts`
- [x] `node scripts/check-api-contract.mjs --strict` 路由差异 0 / schema 漂移 0

## 测试与回归（Task 6）

- [ ] 补 `SecurityServiceTest`：新增 / 更新 / 删除三方法断言落库字段与 `@Version` 递增；`name` / `plate` 为空时校验失败
- [ ] 补「void 写方法仍广播实时变更事件」回归（对齐 `FireFacilityServiceTest` 的既有断言范式）
- [ ] `mvn -q test` 全绿；三方言迁移脚本一致性检查通过

## 收尾（Task 7）

- [ ] 双仓推送 `feature/mgmt-p2-security-crud`；与前端 mgmt 台账联调（待本 Change 上列项全部完成后归档）
