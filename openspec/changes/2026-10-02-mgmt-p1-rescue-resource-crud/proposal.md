# Change: 救援资源四台账补齐全量 CRUD 写端点（P1 第一批）

## 为什么

mgmt 全模块 CRUD 分批计划把「应急专家 / 应急队伍 / 应急车辆 / 应急物资」列为 **P1 应急台账** 前四
模块：后端此前**只有 GET**（列表 + 详情），管理端 4 个页面全是只读表格，既不能新增也不能改错，
更没有任何广播域——改了数据其他端无从感知。

## 变更内容

`RescueResourceService` 新增 12 个写方法，全部带 `@RealtimeSync`：

| 资源 | 表 | 新增方法 | 广播域 | 权限码 |
| --- | --- | --- | --- | --- |
| 救援人员（应急专家） | `fac_rescue_personnel` | create / update / delete | rescue.personnel | `rescue:personnel:write` |
| 消防队伍（应急队伍） | `fac_brigade_team` | create / update / delete | rescue.brigade | `rescue:brigade:write` |
| 救援车辆 | `fac_rescue_vehicle` | create / update / delete | rescue.vehicle | `rescue:vehicle:write` |
| 救援装备（应急物资） | `fac_rescue_equipment` | create / update / delete | rescue.equipment | `rescue:equipment:write` |

`RescueResourceController` 对应新增 12 个端点（POST 列表路径 / PUT+DELETE `{id}` 路径）。
Flyway **V93**（h2 / dameng / postgresql）在 `fm-emergency` 下登记 4 个按钮级菜单并授权
ADMIN / COMMANDER / SCHEDULER，sort_order 131–134 避开 V48（120–123）与 V92（130）。

顺带补齐两个只读 DTO 的返回字段（`RescuePersonnelItem` 的 personGroup / phone / dutyStatus，
`RescueEquipmentItem` 的 category / unit）——原先编辑弹窗拿不到这些值，台账维护无从下手。

## 设计要点

- **写请求字段名对齐只读 DTO**（而非实体）：前端可把列表行直接灌进表单、原样回传，
  视图层不必做 `personName ↔ name` 之类的映射，四个页面共用同一套路。
- **编辑是局部更新**（read-modify-write，null = 不修改），与既有四域写端点同口径。
- **必填校验放在 service**（不是 Bean Validation）：`name` / `plate` 缺失抛 B3 `PARAM_INVALID`，
  与 `BusinessWriteService` 风格一致，便于复用「非法值不静默回落」的约定。
- **按资源分权限码而非合并成一个**：便于后续只放开某类台账（如让装备管理员维护物资但不能改队伍编制）。
- **sort_no 由 service 取 max+1**：四张表都有 sort_no 列且部分为 NOT NULL，交给库自增会失控。
- **只写车辆本体**：乘员 / 随车装备 / 耗材 / 出动汇总分属子表，台账保存不改写子表行，避免误删。
- 删除为**物理删除**（四张表均无 `deleted` 列）；不存在（含重复删除）返回 `NOT_FOUND`。

## 范围与非目标

- 非目标：不动既有 GET 语义与 ABAC 防区过滤；不改大屏既有展示；不新增审计留痕（本批为台账维护，
  与指令类动作区分，留待统一审计策略时一并处理）。
- 零下行红线不变：全部是业务台账留痕，不触发任何物理设备。
