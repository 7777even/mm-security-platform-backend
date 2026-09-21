# Spec Delta: 应急力量「无明细」三类别补真实参考数据

Capability: `emergency-reference`

## 新增

无（不新增 Requirement，仅放宽既有"应急力量明细预览"约束）。

## 修改

### Requirement: 应急力量明细预览

`GET /api/v1/emergency/strength` 返回的每个 `EmergencyResource` 须携带 `items`（`StrengthItem[]`，字段 `name`/`meta`）：
- 「应急专家 / 应急物资 / 应急车辆 / 救援队伍」四个 ledger 源类别，从对应台账（`fac_rescue_personnel` /
  `fac_rescue_equipment` / `fac_rescue_vehicle` / `fac_brigade_team`）取前 20 条真实项填充；
- 「消防设施」从真实台账 `fac_fire_facility_ledger` 取前 20 条填充，`count` 由台账实时计数覆盖；
- 「应急场所 / 医疗机构」从运营参考表 `sys_emergency_strength_item` 取前 20 条填充；
- 「装备车辆」`items` 仍为 `null`（其明细由前端 `fac_rescue_equipment` 台账另路展示）。

#### Scenario: ledger 源类别返回明细

- **WHEN** `GET /api/v1/emergency/strength` 且「应急专家」台账非空
- **THEN** 该 `EmergencyResource.items` 非空，每项 `name` 为人员姓名、`meta` 由岗位/中队拼接

#### Scenario: 消防设施返回真实台账明细

- **WHEN** `GET /api/v1/emergency/strength` 且 `fac_fire_facility_ledger` 非空
- **THEN** 「消防设施」`items` 非空，`name` 为设施名、`meta` 由位置/类型拼接，`count` 为台账实时计数

#### Scenario: 应急场所/医疗机构返回参考表明细

- **WHEN** `GET /api/v1/emergency/strength` 且 `sys_emergency_strength_item` 含对应 kind 行
- **THEN** 该 `EmergencyResource.items` 非空，每项 `name`/`meta` 取自参考表

#### Scenario: 装备车辆无明细

- **WHEN** 类别为「装备车辆」
- **THEN** 该 `EmergencyResource.items` 为 `null`（仅统计数量）

## 移除

无。
