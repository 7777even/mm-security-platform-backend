# Spec Delta：<变更名称>

> 适用：L3 / L4。描述本变更对 `openspec/specs/` 的增量：新增 / 修改 / 移除。
> 段落到 Requirement / Scenario 使用 Gherkin 风格（WHEN / THEN），与既有 `spec.md` 同构。

## ADDED Requirements

### Requirement: <需求标题>

<系统须 ...（能力陈述，可被 tasks.md 验收标准印证）。>

#### Scenario: <场景名>

- **WHEN** <触发条件>
- **THEN** <系统行为（含返回码、错误码、副作用）>

## MODIFIED Requirements

### Requirement: <既有需求标题>

<修改点说明：为何改、改后行为，避免与既有 spec 冲突。>

#### Scenario: <场景名>

- **WHEN** <触发条件>
- **THEN** <修改后的系统行为>

## REMOVED Requirements

### Requirement: <移除的需求标题>

<移除理由：被取代 / 不再适用 / 降级为 L1 技术债。保留历史于 `openspec/archive/`。>

## 关联 Spec

- 目标 spec 文件：`openspec/specs/<capability>/spec.md`（不存在则本变更应新建该 capability）。
- 与 `proposal.md` 的 Capabilities、`tasks.md` 的验收标准三者一一对应、闭环。
- 涉及对外接口时，同步登记前端 `docs/api/<domain>.openapi.json` 的变更点。
