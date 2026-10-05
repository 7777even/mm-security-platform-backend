# special-operation Specification

## Purpose

特殊作业票能力：以 `fac_special_operation_ticket` 承接特殊作业（动火 / 受限空间 / 高处作业等）的
列表、详情与全量 CRUD，供 mgmt 管理端「特殊作业」模块与移动端 `tickets` / `ticket-exec` 页面消费。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-04-mgmt-hazard-special-operation-crud-realtime/`（special-operation 部分）。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/special-operations` | `special-operation:write` | 作业票分页列表 |
| POST | `/api/v1/special-operations` | `special-operation:write` | 作业票新增 |
| GET | `/api/v1/special-operations/{id}` | `special-operation:write` | 作业票详情 |
| PUT | `/api/v1/special-operations/{id}` | `special-operation:write` | 作业票更新（乐观锁） |
| DELETE | `/api/v1/special-operations/{id}` | `special-operation:write` | 作业票删除 |

## Requirements

### Requirement: 特殊作业票 CRUD 与广播

系统 SHALL 提供 `POST /api/v1/special-operations`、`PUT /api/v1/special-operations/{id}`、
`DELETE /api/v1/special-operations/{id}`（权限码 `special-operation:write`）；
`SpecialOperationService` 的 createTicket / updateTicket / deleteTicket 在事务提交成功后
SHALL 广播 `special-operation.changed`，使订阅面板自动重拉。

- 写请求体 `SpecialOperationWriteRequest` 含 26 字段（含 opType / ticketArea / opLevel /
  ticketStatus / workUnit / workLocation / permitNo 等）；
- 本域契约各自本地定义 `DeleteResult`（**不可跨域共享引用**）；
- 删除不存在 → B3 `NOT_FOUND`。

### Requirement: 乐观锁

`fac_special_operation_ticket` SHALL 带 `version BIGINT DEFAULT 0` 列（迁移 V105，三方言一致），
实体相应补 `@Version`；并发冲突返回 409，禁止静默后写覆盖。

### Requirement: 作业票子表不经写端点编辑

**特殊作业票的三张子表（视频 / 气体 / 人员）仍不经写端点编辑**；写端点只编辑主记录字段。
读端点响应 schema `SpecialOperationPage` / `SpecialOperationDetail` 不变。

### Requirement: 零下行控制

作业票写操作只做业务留痕，MUST NOT 触发任何物理设备下行；失败路径走 B3 包络
（参数非法 100、不存在 404、冲突 409；未鉴权 401、无权限 403）。
