# typhoon-emergency Specification

## Purpose

台风应急能力：台风事件、响应看板（横幅 + 预案 / 临时指令）、调度指令台账与调度资源。
承接大屏「台风应急响应」页与 mgmt 管理端「台风调度」模块。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-01-mgmt-p0-batch-crud/`（台风调度部分）、
前端 `docs/system-facts.md` 2026-09-11 V42 台风响应板条目。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/typhoon/incident` | 登录即可 | 台风事件（含路径与影响） |
| GET | `/api/v1/typhoon/response-board` | 登录即可 | 响应看板（横幅 + 预案 / 临时指令） |
| GET | `/api/v1/typhoon/dispatch-resources` | 登录即可 | 可调度资源 |
| GET | `/api/v1/typhoon/dispatch-orders` | 登录即可 | 调度指令台账列表 |
| POST | `/api/v1/typhoon/dispatch-orders` | `typhoon:dispatch:write` | 调度指令新增 |
| PUT | `/api/v1/typhoon/dispatch-orders/{id}` | `typhoon:dispatch:write` | 调度指令局部更新 |
| DELETE | `/api/v1/typhoon/dispatch-orders/{id}` | `typhoon:dispatch:write` | 调度指令删除（物理删除） |

## Requirements

### Requirement: 台风调度指令写端点

系统 SHALL 为台风调度指令资源提供 `PUT /api/v1/typhoon/dispatch-orders/{id}` 与
`DELETE /api/v1/typhoon/dispatch-orders/{id}`（与新增 `POST` 同域，权限码 `typhoon:dispatch:write`）。

- 修改 SHALL 为**局部更新**：空值字段表示"不更新"，不得清空未传字段；
- 删除为物理删除；记录不存在返回 B3 `NOT_FOUND`；
- **强枚举校验**：`dispatchAction` ∈ {`ASSIGN`, `CONFIRM`, `RELEASE`}，非法返回 B3 `PARAM_INVALID`；
- 写操作 SHALL 发布 `typhoon.dispatch` 域变更事件，并落审计（`SystemAuditHelper`）；
- 所有写操作 MUST NOT 触发任何物理设备下行。

### Requirement: 台风响应看板与事件只读端点

系统 SHALL 提供 `GET /api/v1/typhoon/incident`（台风事件）、
`GET /api/v1/typhoon/response-board`（响应看板：横幅 + 预案 / 临时指令）、
`GET /api/v1/typhoon/dispatch-resources`（可调度资源）三个只读端点（登录即可）。

> 响应看板的**模板文案为 by-design 前端渲染参数**，后端返回结构化数据（横幅文本 / 预案条目 / 临时指令），
> 非硬编码业务数据；详见前端 `docs/system-facts.md` V42 条目。

### Requirement: 失败路径 B3 包络

所有失败路径走 B3 包络（HTTP 200 + `code != 0`）：参数非法 100、不存在 404；
未鉴权 401、无权限 403。
