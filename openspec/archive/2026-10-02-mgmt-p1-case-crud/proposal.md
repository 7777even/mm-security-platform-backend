# Change: 事故案例库台账全量 CRUD 写端点（P1）

## 为什么

mgmt 分批计划的「事故案例库」为 **P1 应急台账**模块：此前无独立可编辑表（只读聚合取自 `fac_alarm` 结案），
管理端 `CaseLibView` 是只读占位。本次新建可编辑台账表 `fac_emergency_case`，补齐新增 / 编辑 / 删除写端点
并接入实时广播域，使三端案例数据一致。

## 变更内容

- 新建表 `fac_emergency_case`（V96 三方言 h2 / dameng / postgresql）。
- `EmergencyService` 新增 3 个写方法，全部带 `@RealtimeSync(domain = "emergency.case")`。
- `EmergencyController` 新增 `POST` / `PUT` / `DELETE /api/v1/emergency/cases`，受 **V96** 登记的按钮级权限码
  `emergency:case:write`（父菜单 `fm-emergency`，sort_order=137，授权 ADMIN / COMMANDER / SCHEDULER）。
  V96 另预置 2 条示例案例（explicit id=1/2）。

## 设计要点

- **写请求字段名对齐只读 DTO**（`EmergencyCaseWriteRequest` ↔ `EmergencyCaseItem` 同名 title/accidentType/location/occurredAt/summary/lessons）：
  前端 `openEdit(row)` 直接 `{ ...row }` 灌入表单、原样回传，零字段映射。
- **编辑是局部更新**（null = 不修改），与既有写端点同口径。
- **必填校验放在 service**：`title` 缺失/空白抛 B3 `PARAM_INVALID`（100），文案点名 `title`。
- **新增 id 取 max(id)+1**：`fac_emergency_case` 经 `LedgerIdSupport.nextId` 分配（V96 种子显式插 id=1/2 已落库，
  分配器规避自增序列滞后撞主键）。
- **删除为物理删除**；不存在（含重复删除）返回 `NOT_FOUND`。
- **时间格式化** `yyyy-MM-dd HH:mm:ss`。

## 范围与非目标

- 非目标：不动既有只读聚合；不改大屏既有展示；不新增审计留痕（台账维护，与指令类动作区分）。
- 零下行红线不变：全部是业务台账留痕，不触发任何物理设备。
