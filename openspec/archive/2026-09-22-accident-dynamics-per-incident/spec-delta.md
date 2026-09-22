# Spec Delta: 事故救援「响应动态」按事件隔离

Capability: `emergency-event`

## 新增

### Requirement: 事故救援聚合「动态快讯」按事件隔离

`GET /api/v1/accident/rescue-incident` 返回的 `dynamics`（动态快讯）须按事件隔离：演练事件返回该演练事件的专属动态、真实事件返回该事件的动态，二者各自独立、不共用同一份全局参考；事件无专属动态时回退默认事件（is_default=TRUE）动态，保证大屏不空屏。

#### Scenario: 演练事件返回演练专属动态

- **WHEN** `GET /api/v1/accident/rescue-incident?eventId=11`（储罐区消防演练，fac_accident_incident 有行且含 10 条演练专属动态）
- **THEN** `dynamics` 仅含该演练事件的 10 条演练专属动态（category 含 rescue/command/brief/awareness），不含真实事件 4 的全局动态

#### Scenario: 真实事件返回该事件动态

- **WHEN** `GET /api/v1/accident/rescue-incident?eventId=4`（默认事件，含 19 条全局参考动态）
- **THEN** `dynamics` 返回该默认事件的 19 条全局动态

#### Scenario: 无专属动态回退默认事件

- **WHEN** `GET /api/v1/accident/rescue-incident?eventId=<fac_accident_incident 无行或 dynamics 为空 的事件>`（如演练事件 12–16 未建行）
- **THEN** `dynamics` 回退返回默认事件动态（与改造前一致），大屏不空屏

#### Scenario: 调度资源/值班/辅助统计仍走全局

- **WHEN** 任意事件请求聚合数据
- **THEN** 可调度资源、值班人员、辅助统计三项仍来自全局参考主数据（fac_accident_* 的 allSorted 全量），不随事件隔离

## 修改

- 端点 `GET /api/v1/accident/rescue-incident` 的 `dynamics` 语义由「全量全局参考动态」改为「按事件隔离的动态快讯」；字段结构（`RescueDynamicEntry`）不变。

## 移除

无。

## 约束修订

- 原约束「复用既有表（V12 fac_accident_incident / V17 fac_emergency_event），SHALL NOT 新增 Flyway 迁移。」为 emergency-event 写端点（create/report/start-response）专属——其仅置标志、复用既有表。本增强为「动态快讯按事件隔离」，需 **V63 迁移**（`fac_accident_dynamic` 加 `incident_id` 列 + 索引 + 演练事件/动态种子），属新增迁移、不冲突。即：写端点不新增迁移；动态隔离增强新增 V63（列 + 种子）。
