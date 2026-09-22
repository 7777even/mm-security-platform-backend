# Proposal: 事故救援「响应动态」按事件隔离（演练/真实事件各自独立）

## 为什么（Why）

- 现状：`GET /api/v1/accident/rescue-incident` 的「动态快讯」(`dynamics`) 走的是**同一份全局参考主数据**（V12 在 `fac_accident_dynamic` 种子 19 条，全部不绑定任何事件）。
- 根因：演练事件在 `fac_emergency_event` 有行，但 `fac_accident_incident` 无对应行 → 后端 `incident(eventId)` 回退默认事件 4 → 拿到真实事件 4 的全局动态。于是演练页与真实事件页「响应动态」完全相同，属于**写死同份**，违反「不同场景对应不同情况」的需求。
- 用户明确选择方案 A：**只按 `incident_id` 隔离**（演练事件即 `fac_accident_incident` 中的真实 drill 行，无需额外 `scenario` 字段），并**按现有全局参考主数据的风格**编写演练专属动态种子。

## 目标（What Changes）

1. **数据库（L4 迁移 V63，三方言）**：`fac_accident_dynamic` 增加 `incident_id BIGINT` 列 + 索引；现有 19 条全局动态归到默认事件（`incident_id=1`，对应 `event_id=4`）；新增演练事件「储罐区消防演练」(`event_id=11`) 的 `fac_accident_incident` 行 + 10 条演练专属动态（`incident_id` 经子查询引用刚插入的演练事件行）。
2. **实体**：`FacAccidentDynamic` 增加 `incidentId` 字段（Mapper 映射 `incident_id`）。
3. **服务**：`AccidentRescueService` 的 `dynamics` 由「全量全局」改为「按 `incident_id` 查询 + 空兜底默认事件动态」；调度资源 / 值班 / 辅助统计仍走全局参考主数据（不变）。
4. **测试**：新增 2 个单测（自身动态非空直接返回；自身动态为空回退默认事件），全量 `mvn test` 通过。

## 非目标（Out of Scope）

- 不引入 `scenario` 标签字段（方案 B 已否决）。
- 不为其余演练事件 12–16 建 `fac_accident_incident` 行（未建行者回退默认事件动态，与改造前一致）；后续按需补种子单列 Change。
- 不改 `dynamics` 对外结构（`incident_id` 不对外暴露，前端契约无 schema 变更）。

## 能力（Capabilities）

- `emergency-event`：`GET /api/v1/accident/rescue-incident` 的「动态快讯」按事件隔离——演练事件返回演练专属动态、真实事件返回该事件动态、二者各自独立不共用全局参考；事件无专属动态时空屏兜底回退默认事件。

## 影响（Impact）

- **前端契约同步范围**：`frontend-scaffold/docs/api/accident-rescue.openapi.json` 端点与 `dynamics` 字段 description 改为「按事件隔离」说明（无 schema/字段新增，前端 `gen:api-types` 仅 JSDoc 文字变更，无结构变化）。
- **数据结构存量影响**：仅新增列 + 种子，不删不改既有 `fac_accident_dynamic` 行；`UPDATE ... SET incident_id = 1` 把存量 19 条绑到默认事件，语义不变（改造前本就全量返回）。
- **回退方案**：若需回退，删除 V63 种子行并 `ALTER TABLE DROP COLUMN incident_id`（需在下一版本迁移中执行）；Service 默认兜底保证即便动态为空大屏不空屏。
- **三方言**：h2 / postgresql / dameng 均提供 V63（达梦不支持多行 `VALUES`，逐条 `INSERT`）；已实跑校验 h2（全量单测 661 绿）；pg/dm 静态对拍一致，待上环境复核。

## 人工确认关卡（L4 数据结构变更）

- 本变更为 L4（触及 DB 结构）。按 AGENTS §1.4 / openspec 规则，需先出提案并经人工确认。
- **确认记录**：用户在对话中明确选择「方案 A」并补充「A 按现有全局参考主数据的风格编写」，视为人工确认关卡已通过，方可进入实现。实现已落地（V63 + 实体 + 服务 + 测试 + 全量单测绿），本 Change 为交付闭环记录。
