# Change: 应急预案目录台账补齐全量 CRUD 写端点（P1）

## 为什么

mgmt 分批计划的「预案目录」为 **P1 应急台账**模块：后端 `/emergency-plans/catalog` 仅层次化只读摘要（大屏展示用），
管理端 `PlanCatalogView` 只读。本次补齐可编辑扁平台账写端点（`/emergency-plans/catalog-items`），使管理端可维护
预案目录行并实时广播，区别于大屏只读层次化摘要。

## 变更内容

- `EmergencyPlanService` 新增 4 个写方法（catalogItems / create / update / delete），全部带 `@RealtimeSync(domain = "emergency.plan-catalog")`。
- `EmergencyPlanController` 新增 `POST` / `PUT` / `DELETE /api/v1/emergency-plans/catalog-items`，受 **V97** 登记的按钮级权限码
  `emergency:plan-catalog:write`（父菜单 `fm-emergency`，sort_order=138，授权 ADMIN / COMMANDER / SCHEDULER）。
- 复用既有 `fac_emergency_plan_catalog` 表，无新表。

## 设计要点

- **写请求字段名对齐扁平行 DTO**（`EmergencyPlanCatalogWriteRequest` ↔ `EmergencyPlanCatalogRow`；label/planCode/planName/canSwitch/isCurrent/sortNo），
  区别于 `/catalog` 层次化只读 DTO（`EmergencyPlanCatalogItem` 的 id 为层级编码）。
- **编辑是局部更新**（null = 不修改），与既有写端点同口径。
- **必填校验放在 service**：`label` 缺失/空白抛 B3 `PARAM_INVALID`（100），文案点名 `label`。
- **新增 id 取 max(id)+1**：`fac_emergency_plan_catalog` 经 `LedgerIdSupport.nextId` 分配（V39 种子显式插 id，分配器规避序列滞后）。
- **删除为物理删除**；不存在（含重复删除）返回 `NOT_FOUND`。
- **canSwitch / isCurrent 为 0/1 整型**（前端 select 0/1）。

## 范围与非目标

- 非目标：不动 `/catalog` 层次化只读语义；不改大屏既有展示（订阅 `emergency.plan-catalog` 域为后续可选跟随）。
- 零下行红线不变：全部是业务台账留痕，不触发任何物理设备。
