# Design: 救援资源四台账写端点

## 1. 为什么写请求用 DTO 字段名而不是实体字段名

实体是 `personName / teamName / equipName / vehicleType / vehicleStatus`，只读 DTO 是
`name / name / name / type / status`。若写请求用实体字段名，前端每个页面都要写一份
「列表行 → 表单」的映射，四个页面四份重复代码，且与编辑弹窗的 `editRow` 直接灌入机制冲突。

选 DTO 字段名后：`openEdit(row)` 直接 `{ ...row }`，保存时原样回传——**四个页面零映射代码**。
代价是 service 内要做一次实体↔DTO 映射（`row.setPersonName(req.getName())`），但这只写一次。

## 2. 为什么必填校验放 service 而不是 `@NotBlank`

- 与既有 `BusinessWriteService`（四域写端点）保持同风格，降低认知成本；
- create 与 update **共用一个 DTO**，Bean Validation 无法表达「新增必填、编辑可不传」；
- 非法值直接抛 `PARAM_INVALID`，不做静默回落——静默回落会让调用方误以为已生效。

## 3. 新增排序号为什么取 max+1

四张表都有 `sort_no` 列，且 `fac_rescue_*` 的种子数据带了显式 sort_no。
若依赖数据库自增或省略该列，新行排序位置不可控（可能插到列表最前、也可能撞 NOT NULL 约束）。
统一由 service 取 `max(sort_no) + 1`，空表从 1 起，行为跨方言一致。

## 4. 车辆为什么只写本体

`fac_rescue_vehicle` 关联 `fac_rescue_vehicle_crew`（乘员）、`_equipment`（随车装备）、
`_kv`（耗材 / 出动汇总）三张子表。若让「保存」一次改写全部子表，前端必须实现子表编辑器，
且任何一次保存都要做「删旧插新」——一旦前端少传一个子表数组，就会把既有行删光。

本 Change 只写车辆本体，子表仍由详情端点聚合展示。这是**有意的收窄**，后续若要支持子表维护
应做成独立的子资源端点（如 `PUT /vehicles/{id}/crew`），而不是塞进本体保存。

## 5. 权限码为什么按资源拆四个

合并成一个 `rescue:resource:write` 也能跑，但四类台账的管理员往往不是同一批人
（装备管理员维护物资、指挥岗维护队伍编制）。按资源拆码让后续授权可以精确下放，
代价只是迁移里多三行 INSERT。

## 6. 顺带补的 DTO 返回字段

`RescuePersonnelItem` 原先只返回 `id / name / squadron / role`，但库里还有
`person_group / phone / duty_status`。编辑弹窗拿不到值就只能清空重填——等于**编辑即丢数据**。
同理 `RescueEquipmentItem` 缺 `category / unit`。

补字段属于**只读 DTO 的扩展**，不影响既有展示（新增字段、不改名、不改语义），
但必须同步契约与前端类型，否则守门会报 schema 漂移。
