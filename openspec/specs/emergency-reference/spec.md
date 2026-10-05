# emergency-reference Specification

## Purpose

应急指挥参考数据端点（应急力量 / 已结案件 / 值班 / 通讯录 / 知识库）。由 Change `implement-remaining-contracts`（已归档）回填。

## Requirements

### Requirement: 应急参考数据只读端点

系统须提供以下只读端点，全部以 B3 包络返回：

| 端点 | 响应 |
| --- | --- |
| `GET /api/v1/emergency/strength` | EmergencyStrength（应急力量） |
| `GET /api/v1/emergency/closed-cases` | ClosedCaseList（`fac_alarm` CLOSED 聚合） |
| `GET /api/v1/emergency/duty` | DutyRoster（值班表） |
| `GET /api/v1/emergency/phones` | EmergencyPhoneBook（应急通讯录） |
| `GET /api/v1/emergency/knowledge` | KnowledgeList（知识库） |

#### Scenario: 端点鉴权

- **WHEN** 未携带有效令牌访问上述任一端点
- **THEN** 返回 401（B3 包络），这些端点不在免鉴权白名单内

#### Scenario: 已结案件来源

- **WHEN** `GET /api/v1/emergency/closed-cases`
- **THEN** 数据来源于 `fac_alarm` 中 `status=CLOSED` 的聚合，不读取虚构事实

### Requirement: 应急力量明细预览

`GET /api/v1/emergency/strength` 返回的每个 `EmergencyResource` 须携带 `items`（`StrengthItem[]`，字段 `name`/`meta`）：
- 「应急专家 / 应急物资 / 应急车辆 / 救援队伍」四个 ledger 源类别，从对应台账（`fac_rescue_personnel` /
  `fac_rescue_equipment` / `fac_rescue_vehicle` / `fac_brigade_team`）取前 20 条真实项填充；
- 「消防设施」从真实台账 `fac_fire_facility_ledger` 取前 20 条填充，`count` 由台账实时计数覆盖；
- 「应急场所 / 医疗机构」从运营参考表 `sys_emergency_strength_item` 取前 20 条填充；
- 「装备车辆」`items` 仍为 `null`（其明细由前端 `fac_rescue_equipment` 台账另路展示，不在此接口）。

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

### Requirement: 知识分类说明

`GET /api/v1/emergency/knowledge` 返回的每个 `KnowledgeItem` 须携带 `description`，
取自 `sys_knowledge_item.description`（真实可编辑文案）。

#### Scenario: 说明来自数据库

- **WHEN** `GET /api/v1/emergency/knowledge`
- **THEN** 每个 `KnowledgeItem.description` 等于该分类在 `sys_knowledge_item` 的 `description` 值

### Requirement: 应急通讯录台账写端点

系统应为应急通讯录提供 `POST /api/v1/emergency/phones`、`PUT` 与 `DELETE /api/v1/emergency/phones/{id}`。

- **权限**：需 `emergency:phone:write`；
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改；
- **删除**：物理删除；不存在（含重复删除）返回 B3 `NOT_FOUND`；
- **广播**：成功写入后发布 `emergency.phone` 域变更。

> 本条及以下四条来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次）：
> `openspec/archive/2026-10-02-mgmt-p1-phone-crud/`。

#### Scenario: 重复删除
- **WHEN** 对同一个已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）

### Requirement: 应急知识库台账写端点

系统应为应急知识库提供 `POST /api/v1/emergency/knowledge`、`PUT` 与
`DELETE /api/v1/emergency/knowledge/{id}`，权限码 `emergency:knowledge:write`，
编辑为局部更新、删除为物理删除且不存在返回 `NOT_FOUND`，成功写入后发布 `emergency.knowledge` 域变更。

> 来源：`openspec/archive/2026-10-02-mgmt-p1-knowledge-crud/`。

### Requirement: 事故案例库台账写端点

系统应为事故案例库提供 `POST /api/v1/emergency/cases`、`PUT` 与
`DELETE /api/v1/emergency/cases/{id}`，权限码 `emergency:case:write`，
编辑为局部更新、删除为物理删除且不存在返回 `NOT_FOUND`，成功写入后发布 `emergency.case` 域变更。

> 来源：`openspec/archive/2026-10-02-mgmt-p1-case-crud/`。

### Requirement: 应急指令与值班签到写端点

系统应为应急指令记录 `/api/v1/emergency/command-records` 与值班签到 `/api/v1/emergency/duty-sign-ins`
各提供 `PUT /{id}` 与 `DELETE /{id}`（权限码分别为 `emergency:command:write` / `emergency:duty:write`）。

- 修改须为**局部更新**：空值字段表示"不更新"，不得清空未传字段；
- 删除为物理删除；记录不存在返回 B3 `NOT_FOUND`；
- **强枚举校验**：`signAction` ∈ {`SIGN_IN`, `SIGN_OUT`}，非法返回 B3 `PARAM_INVALID`；
- 写操作须发布 `emergency.command` / `emergency.duty` 域变更事件，并落审计（`SystemAuditHelper`）；
- 所有写操作 MUST NOT 触发任何物理设备下行。

> 来源：`openspec/archive/2026-10-01-mgmt-p0-batch-crud/`。

### Requirement: 应急辅助统计与指挥只读端点

系统应提供 `GET /api/v1/emergency/assist-stats`（应急助力统计）、
`GET /api/v1/emergency/commands` 与 `/api/v1/emergency/commands/{commandId}`（应急指令）、
`GET /api/v1/emergency/dispatch-personnel`（派单名册，替代大屏硬编码人名）、
`GET /api/v1/emergency/process/panorama`、`/process/guidances`、`/process/node-configs`
（应急流程全景 / 指引 / 节点联动配置，后者 `PUT` 为 `role:ADMIN`）
等只读端点；响应统一 B3 包络，只读端点不产生广播事件。
