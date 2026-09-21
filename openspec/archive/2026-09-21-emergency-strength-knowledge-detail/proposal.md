# Proposal: 应急力量 / 知识库明细接口富化

## Why
大屏「应急指挥首页」（fm-emergency）右侧「应急力量救援」「应急生产安全知识」两个面板点击卡片弹出的详情，
此前只有聚合数字 + 前端写死的说明文案（`EmergencyRescuePanel`/`SafetyKnowledgePanel` 内的常量），
无法反映真实台账，与「避免纯展示死数据」的目标冲突。

后端其实已有真实明细源：`fac_rescue_personnel`/`fac_rescue_equipment`/`fac_rescue_vehicle`/`fac_brigade_team`
（与 `GET /rescue-resources/*` 同源）；知识分类说明需要一个可编辑的描述列。

## What Changes
- `GET /api/v1/emergency/strength`：`EmergencyResource` 新增 `items: StrengthItem[]`（`name`/`meta`），
  对 4 个 ledger 源类别（应急专家/应急物资/应急车辆/救援队伍）返回各台账前 20 条真实项；其余 4 类为 `null`。
- `GET /api/v1/emergency/knowledge`：`KnowledgeItem` 新增 `description`（来自 `sys_knowledge_item.description`）。
- **DB 结构变更（L4）**：Flyway `V59__knowledge_description.sql` 为 `sys_knowledge_item` 增加 `description` 列并回填种子文案；h2 / postgresql / dameng 三方言。
- 契约真源 `frontend-scaffold/docs/api/emergency.openapi.json` 新增 `StrengthItem` schema、`EmergencyResource.items`、`KnowledgeItem.description`（四铁律齐备）。

## Capabilities
- 大屏「应急力量救援」点击资源类别 → 弹窗展示该类别真实台账明细（前 20 项，名称 + 岗位/规格/车型/区域）。
- 大屏「应急生产安全知识」点击知识卡 → 弹窗展示真实分类说明（后端可编辑）。

## Impact
- 存量接口**仅新增响应字段**，路径/方法不变，向后兼容；前端旧解析不受影响。
- DB：`sys_knowledge_item` 增列（可空），无回填缺失风险（新列默认 NULL，UPDATE 仅命中种子 3 行）。
- 回退：删除 V59（列随迁移回滚策略处理）+ 移除 DTO 新字段 + 契约新增段。

## 人工确认关卡（L4 必过）
| 关卡 | 状态 | 说明 |
| --- | --- | --- |
| DB 结构变更确认 | ☑ 已确认 | 用户明确选择「知识加列 + strength 明细（全做）」，同意为 `sys_knowledge_item` 增加 `description` 列 |
| 接口范围确认 | ☑ 已确认 | 仅富化既有 2 个只读端点，不新增路由、不引入写端点 |
| 三方言迁移方案确认 | ☑ 已确认 | 依既有 V55+ 迁移风格，DM 用 `ADD col VARCHAR2(.. CHAR)` |
