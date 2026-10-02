# Change: 应急知识库台账补齐全量 CRUD 写端点（P1）

## 为什么

mgmt 全模块 CRUD 分批计划把「应急知识库」列为 **P1 应急台账** 模块：后端此前**只有 GET**
（列表 + 聚合），管理端 `KnowledgeView` 是只读表格，既不能新增也不能改错/删除，
更没有任何广播域——改了数据其他端无从感知。

## 变更内容

`EmergencyService` 新增 3 个写方法，全部带 `@RealtimeSync(domain = "emergency.knowledge")`：

| 资源 | 表 | 新增方法 | 广播域 | 权限码 |
| --- | --- | --- | --- | --- |
| 应急知识库条目 | `sys_knowledge_item` | create / update / delete | `emergency.knowledge` | `emergency:knowledge:write` |

`EmergencyController` 对应新增 3 个端点
（`POST /api/v1/emergency/knowledge`、`PUT`/`DELETE /api/v1/emergency/knowledge/{id}`），
受 **V94**（h2 / dameng / postgresql）三方言登记的按钮级权限码 `emergency:knowledge:write`
（父菜单 `fm-emergency`，sort_order=135，避开 V48 的 120–123、V92 的 130、V93 的 131–134，
授权 ADMIN / COMMANDER / SCHEDULER）。

## 设计要点

- **写请求字段名对齐只读 DTO**（`KnowledgeWriteRequest` ↔ `KnowledgeItem` 同名 title/count/icon/description）：
  前端 `openEdit(row)` 直接 `{ ...row }` 灌入表单、原样回传，零字段映射。
- **编辑是局部更新**（null = 不修改），与既有写端点同口径。
- **必填校验放在 service**：`title` 缺失/空白抛 B3 `PARAM_INVALID`（100），文案点名 `title`，
  与 `BusinessWriteService` 风格一致。
- **新增 id 取 max(id)+1**：`sys_knowledge_item` 虽设自增，但 V8 种子显式插 id 致序列滞后、撞主键 → 409，
  统一走 `LedgerIdSupport.nextId` 修复（与救援资源/表单记录同一套分配器）。
- **删除为物理删除**（`sys_knowledge_item` 无 `deleted` 列）；不存在（含重复删除）返回 `NOT_FOUND`。
- **缓存一致性**：`sys_knowledge_item` 走 `knowledgeCache` 读穿缓存，写后 `invalidateAll()`，
  保证大屏/管理端实时一致。

## 范围与非目标

- 非目标：不动既有 GET 语义；不改大屏既有展示（订阅 `emergency.knowledge` 域为后续可选跟随）；
  不新增审计留痕（台账维护，与指令类动作区分）。
- 零下行红线不变：全部是业务台账留痕，不触发任何物理设备。
