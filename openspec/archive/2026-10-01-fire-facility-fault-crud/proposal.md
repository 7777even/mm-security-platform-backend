# Change: 消防设施故障全量 CRUD 写端点与实时广播

## 为什么

mgmt 管理端「消防设施故障管理」页（`apps/mgmt/views/fire/FaultMgmtView.vue`）此前是只读列表：
后端仅有 `PUT /api/v1/fire-facility/faults/{faultId}`（状态流转写回），缺新增与删除端点，
且 `fire-facility.fault` 域虽已广播但前端无人订阅。用户要求 mgmt 全部模块实现真 CRUD、
实时更新、三端数据互通，故本 Change 补齐该模块的全栈写链路。

## 变更内容

1. 新增 `POST /api/v1/fire-facility/faults`（权限码 `fire-facility:fault-create`）：
   台账录入落库 `fac_fire_facility_fault`，返回新建条目（含空时间线）。
2. 新增 `DELETE /api/v1/fire-facility/faults/{faultId}`（权限码 `fire-facility:fault-delete`）：
   级联清理 `fac_fire_facility_fault_timeline` 后物理删除（真删除）。
3. 扩展 `PUT /api/v1/fire-facility/faults/{faultId}`：在既有状态流转 + 派单/维修/验收字段之外，
   增加基础字段（facilityName / facilityType / faultType / faultLevel / discoverTime /
   discoverMethod / phenomenon / cause）的局部更新，使管理端可全字段编辑。
4. 三个写方法均标注 `@RealtimeSync(domain = "fire-facility.fault")`，触发三端实时刷新。
5. 新增迁移 `V91__fire_facility_fault_crud_perm.sql`（h2 / dameng / postgresql 三方言），
   登记按钮级权限码并授权 ADMIN 及五类岗位角色。

## 范围与非目标

- **目标表**：`fac_fire_facility_fault`、`fac_fire_facility_fault_timeline`。
- **非目标**：故障状态机本身不改（仍是 待确认→已确认→已派单→维修中→待验收→已闭环）；
  不新增物理设备下行控制（本模块只做业务留痕，零下行红线不变）。

## 兼容性

新增字段全部可选，`PUT` 保持 read-modify-write 局部更新语义，大屏既有
「确认/派单/维修/验收」链路（只传 faultStatus / timelines）行为不变。
