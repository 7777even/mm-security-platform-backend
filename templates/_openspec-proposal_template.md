# 变更提案：<变更名称（中文，简短名词短语）>

> 适用：L3 / L4 改动（业务能力 / 高风险）。L0–L2 不建此文件。
> 约束：本文件须含 Why / What Changes / Capabilities / Impact 四节，控制在 500 字内，聚焦单一变更（openspec/config.yaml）。

## Why

<为什么做：触发背景、现状痛点、与哪条红线 / 契约 / 决策冲突、与用户或设计方拍板确认的约束。讲清"为什么现在做、不做会怎样"。>

## What Changes

- <具体改动点 1：类 / 包 + 改什么（如 `controller/DeviceController` 新增分页查询）。>
- <具体改动点 2：Service / Mapper / Entity / DTO 的落点。>
- <具体改动点 3：安全层、配置、SQL 脚本等横切改动。>

## Capabilities

### Added Capabilities

- `<capability-id>`：<一句话能力描述，对应 `openspec/specs/<capability>/spec.md` 的新增需求。>

### Modified Capabilities

- `<capability-id>`：<被本变更修改的既有能力（复用或增强既有 capability）。>

## Impact

- <受影响范围：哪些包 / 文件 / 表 / 端点。>
- <契约同步：本次触及的前端契约文件与端点（**必填**；如不涉及对外接口写"不涉及"）。例：`frontend-scaffold/docs/api/device.openapi.json` 的 `GET /api/v1/devices`。>
- <数据影响：是否改表 / 字段 / 索引 / 约束；存量数据影响与回退方案（**必填**；不涉及写"不涉及"）。>
- <不触碰的边界：声明明确不受影响的模块，避免误判回归面。>
- <安全语义：是否改变鉴权白名单、权限语义、签名或硬控路径（零下行控制红线、B3 包络）。>
- <回归面：可能受影响的既有单测、冒烟用例、前端调用方。>

## 人工确认关卡（L3 须过 / L4 实施前须过）

- [ ] 提案范围与用户 / 设计方确认一致，无需求扩散、无自造平行任务。
- [ ] API 契约（AGENTS.md §3）未违反：零下行控制 / B3 包络 / 20 位 MDM / 防重放签名 / 无状态 JWT / 错误码分段。
- [ ] 跨库四同步已排定：前端 `docs/api/*.openapi.json` 同步范围与负责人明确。
- [ ] 数据变更影响与回退方案已确认（schema 快照 + 增量 SQL）。
- [ ] 高风险项（L4：契约语义 / 权限模型 / 数据库结构 / 安全过滤器链 / 部署配置基线 / 框架升级）已明确并取得人工确认。
