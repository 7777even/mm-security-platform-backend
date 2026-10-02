# Change: 应急通讯录台账补齐全量 CRUD 写端点（P1）

## 为什么

mgmt 全模块 CRUD 分批计划把「应急通讯录」列为 **P1 应急台账** 模块：后端此前**只有 GET**
（`phones()` 聚合通讯录），管理端 `ContactsView` 是只读表格，既不能新增也不能改错/删除，
更没有任何广播域——改了数据其他端无从感知。

## 变更内容

`EmergencyService` 新增 3 个写方法，全部带 `@RealtimeSync(domain = "emergency.phone")`：

| 资源 | 表 | 新增方法 | 广播域 | 权限码 |
| --- | --- | --- | --- | --- |
| 应急通讯录条目 | `sys_emergency_phone` | create / update / delete | `emergency.phone` | `emergency:phone:write` |

`EmergencyController` 对应新增 3 个端点
（`POST /api/v1/emergency/phones`、`PUT`/`DELETE /api/v1/emergency/phones/{id}`），
受 **V95**（h2 / dameng / postgresql）三方言登记的按钮级权限码 `emergency:phone:write`
（父菜单 `fm-emergency`，sort_order=136，避开 V48 的 120–123、V92 的 130、V93 的 131–134、V94 的 135，
授权 ADMIN / COMMANDER / SCHEDULER）。

## 设计要点

- **写请求字段名对齐只读 DTO**（`PhoneWriteRequest` ↔ `EmergencyPhone` 同名 name/number/category）：
  前端 `openEdit(row)` 直接 `{ ...row }` 灌入表单、原样回传，零字段映射。
- **编辑是局部更新**（null = 不修改），与既有写端点同口径。
- **必填校验放在 service**：`name` / `number` 缺失或空白抛 B3 `PARAM_INVALID`（100），文案分别点名 `name` / `number`，
  与 `BusinessWriteService` 风格一致。
- **新增 id 取 max(id)+1**：`sys_emergency_phone` 虽设自增，但 V8 种子显式插 id 致序列滞后、撞主键 → 409，
  统一走 `LedgerIdSupport.nextId` 修复（与救援资源/知识库同一套分配器）。
- **删除为物理删除**（`sys_emergency_phone` 无 `deleted` 列）；不存在（含重复删除）返回 `NOT_FOUND`。
- **缓存一致性**：`phones()` 走 `phoneCache` 读穿缓存，写后 `invalidateAll()`，
  保证大屏/管理端实时一致。

## 范围与非目标

- 非目标：不动既有 GET 语义；不改大屏既有展示（订阅 `emergency.phone` 域为后续可选跟随）；
  不新增审计留痕（台账维护，与指令类动作区分）。
- 零下行红线不变：全部是业务台账留痕，不触发任何物理设备。
