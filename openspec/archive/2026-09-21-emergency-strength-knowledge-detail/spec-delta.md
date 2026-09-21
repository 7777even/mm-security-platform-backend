# Spec Delta: 应急力量 / 知识库明细接口富化

Capability: `emergency-reference`

## 新增

### Requirement: 应急力量明细预览

`GET /api/v1/emergency/strength` 返回的每个 `EmergencyResource` 须携带 `items`（`StrengthItem[]`，字段 `name`/`meta`）：
对「应急专家 / 应急物资 / 应急车辆 / 救援队伍」四个 ledger 源类别，从对应台账（`fac_rescue_personnel` /
`fac_rescue_equipment` / `fac_rescue_vehicle` / `fac_brigade_team`）取前 20 条真实项填充；其余类别 `items` 为 `null`。

#### Scenario: ledger 源类别返回明细

- **WHEN** `GET /api/v1/emergency/strength` 且「应急专家」台账非空
- **THEN** 该 `EmergencyResource.items` 非空，每项 `name` 为人员姓名、`meta` 由岗位/中队拼接

#### Scenario: 无明细源类别 items 为空

- **WHEN** 类别为「装备车辆 / 应急场所 / 医疗机构 / 消防设施」
- **THEN** 该 `EmergencyResource.items` 为 `null`（仅统计数量，不虚构明细）

### Requirement: 知识分类说明

`GET /api/v1/emergency/knowledge` 返回的每个 `KnowledgeItem` 须携带 `description`，
取自 `sys_knowledge_item.description`（真实可编辑文案）。

#### Scenario: 说明来自数据库

- **WHEN** `GET /api/v1/emergency/knowledge`
- **THEN** 每个 `KnowledgeItem.description` 等于该分类在 `sys_knowledge_item` 的 `description` 值

## 修改

无（既有端点路径/方法/鉴权不变，仅新增响应字段）。

## 移除

无。
