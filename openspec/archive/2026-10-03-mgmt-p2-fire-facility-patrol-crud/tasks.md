# Tasks: 消防设施台账与防火巡查 CRUD（后端）

## 权限与表结构（Task 1）

- [x] V99 三方言迁移 `V99__fire_facility_ledger_crud_perm.sql`：登记 `fire-facility:ledger:write` 按钮菜单并授权 ADMIN 及岗位角色
- [x] V102 三方言迁移 `V102__fire_patrol_crud_perm.sql`：登记 `fire:patrol-write` 按钮菜单并授权 ADMIN 及岗位角色；`fac_fire_patrol` 增加 `version BIGINT DEFAULT 0` 乐观锁列

## DTO（Task 2）

- [x] 新增 `dto/FireFacilityLedgerWriteRequest.java`（facilityCode/facilityName/facilityType 必填，无 `status` 字段）
- [x] 新增 `dto/FirePatrolWriteRequest.java`（patrolDate 必填，无 `status` / 检查项字段）
- [x] 实体 `FacFirePatrol` 加 `@Version` 字段

## Service（Task 3）

- [x] `FireFacilityService` 新增 `createLedger` / `updateLedger` / `deleteLedger`，标注 `@RealtimeSync(domain="fire-facility.ledger")`
- [x] `FireMonitoringService` 新增 `createFirePatrol` / `updateFirePatrol` / `deleteFirePatrol`，标注 `@RealtimeSync(domain="fire.patrol-record")`

## Controller（Task 4）

- [x] `FireFacilityController` 新增 /ledger POST / PUT / DELETE（`@RequireAuth` + `@Valid`）
- [x] `FireMonitoringController` 新增 /patrols POST / PUT / DELETE（`@RequireAuth` + `@Valid`）

## 契约四同步（Task 5）

- [x] 前端 `docs/api/fire-facility.openapi.json` / `docs/api/fire-monitoring.openapi.json` 补 6 个写端点（同 path 多 method 合并）+ 两个 WriteRequest schema
- [x] `npm run gen:api-types` 重产 `src/types/generated/fire-facility.ts` / `fire-monitoring.ts`
- [x] `node scripts/check-api-contract.mjs --strict` schema 漂移 0（路由差异为预存技术债）

## 测试与回归（Task 6）

- [x] `FireFacilityServiceTest`：台账 CRUD + 广播域断言 + notFound + 重复编码 CONFLICT（+120）
- [x] `FireMonitoringServiceTest`：巡查 CRUD + 广播域断言 + notFound + 乐观锁（+93）
- [x] `FireMonitoringControllerTest`：巡查新增返回 Record、必填缺失→B3 400、删除→200、不存在→404（+66）
- [x] `mvn -o test` 相关类全绿（50/50）；jacoco 覆盖率达标

## 收尾（Task 7）

- [x] 双仓推送（`main` 已含本批次 V99/V102 与写端点）；与前端 mgmt 台账联调（待本 Change 上列项全部完成后归档）
