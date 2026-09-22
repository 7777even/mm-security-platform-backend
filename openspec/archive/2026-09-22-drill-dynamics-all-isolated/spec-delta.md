# Spec Delta: 演练事件 12–16 响应动态各自独立展示

## 变更的能力

`emergency-event`（`openspec/specs/emergency-event/spec.md`）

## Requirement 修订：`事故救援聚合「动态快讯」按事件隔离`

- 概述：补充「全部演练事件（11–16）均已建独立 `fac_accident_incident` 行并编有演练专属动态，各自独立展示、可区分」。
- Scenario `演练事件返回演练专属动态` → 重命名为 `演练事件返回各自独立的演练专属动态`，新增：
  - **WHEN** `GET /api/v1/accident/rescue-incident?eventId=12|13|14|15|16`（其余演练事件，fac_accident_incident 各有行且各含 7 条演练专属动态）
  - **THEN** `dynamics` 仅含对应演练事件各自的 7 条演练专属动态，与事件 11 及真实事件 4 的全局动态均不混用，彼此可区分
- Scenario `无专属动态回退默认事件`：示例由「如演练事件 12–16 未建行」改为「如未建行的真实事件」，并注明演练事件 11–16 均有专属动态不走此分支。
- 约束：`动态快讯按事件隔离` 增强说明补全 V63（事件 11）+ V64（事件 12–16）迁移来源。

## 对外契约

无变更（`incident_id` 内部字段不暴露；`dynamics` schema 不变）。
