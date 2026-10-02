# Change: 应急预案主记录台账全量 CRUD 写端点（P1）

## 为什么

mgmt 分批计划的「应急预案」为 **P1 应急台账**模块：后端 `/emergency-plans/options` 仅大屏筛选只读，
管理端 `EmergencyPlanView` 只读。本次补齐可编辑主记录写端点（`/emergency-plans` 根路径 CRUD），使管理端可维护
应急预案主记录并实时广播，区别于大屏只读筛选 / 矩阵视图。

## 变更内容

- `EmergencyPlanService` 新增 4 个写方法（planMetaList / create / update / delete），全部带 `@RealtimeSync(domain = "emergency.plan")`。
- `EmergencyPlanController` 新增 `POST /api/v1/emergency-plans`、`PUT` / `DELETE /api/v1/emergency-plans/{id}`，
  受 **V98** 登记的按钮级权限码 `emergency:plan:write`（父菜单 `fm-emergency`，sort_order=139，授权 ADMIN / COMMANDER / SCHEDULER）。
- 复用既有 `fac_emergency_plan` 表，无新表。

## 设计要点

- **写请求字段名对齐主记录 DTO**（`EmergencyPlanMetaWriteRequest` ↔ `EmergencyPlanMetaItem`；
  planName/tabKey/accidentType/facility/domain/nuclear/isActive/sortNo）。
- **编辑是局部更新**（null = 不修改），与既有写端点同口径。
- **必填校验放在 service**：`planName` 缺失/空白抛 B3 `PARAM_INVALID`（100），文案点名 `planName`。
- **新增 id 取 max(id)+1**：`fac_emergency_plan` 经 `LedgerIdSupport.nextId` 分配（统一分配器，零迁移一致）；新行 `invokeCount` 置 0。
- **删除为物理删除**；不存在（含重复删除）返回 `NOT_FOUND`。
- **nuclear / isActive 为 Boolean**（前端 select 是/否）。

## 范围与非目标

- 非目标：不动 `/options` / `matrix` 大屏只读视图；不改 `invokePlan` 一键调用语义（仍 `@RequireAuth(role=ADMIN)`）。
- 零下行红线不变：全部是业务台账留痕，不触发任何物理设备。
