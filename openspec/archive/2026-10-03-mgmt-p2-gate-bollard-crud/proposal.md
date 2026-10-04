# Proposal: 安防设备台账 CRUD——道闸与防恐柱（P2 批次 2）

## 问题

安防域 Batch 1 已补齐人员/车辆检索与周界告警 CRUD，但**卡口门禁（道闸）与液压防恐柱**两张设备台账此前只有只读列表：

1. 道闸/防恐柱台账无新增/编辑/删除入口，设备信息变更只能改库。
2. 写请求若接受 `status`，会突破零下行控制红线（设备实时状态只能读、不能由管理端下发）。
3. `fac_gate_control` / `fac_bollard` 缺版本列，并发编辑会静默后写覆盖。

## 目标

扩展 `SecurityController` / `SecurityService`，把道闸与防恐柱做成**全套 CRUD**，并严守零下行控制：

- 道闸：`POST /security/gate-controls`、`PUT /security/gate-controls/{id}`、`DELETE /security/gate-controls/{id}`，权限码 `security:gate-write`，`@RealtimeSync(domain="security.gate-control")`。
- 防恐柱：`POST /security/bollards`、`PUT /security/bollards/{id}`、`DELETE /security/bollards/{id}`，权限码 `security:bollard-write`，`@RealtimeSync(domain="security.bollard")`。
- 写请求 DTO：`GateControlWriteRequest`（`name` `@NotBlank` 必填，`location`/`longitude`/`latitude` 可空）、`BollardWriteRequest`（`name` `@NotBlank` 必填，`zone`/`longitude`/`latitude` 可空）。**两个 DTO 均不含 `status` 字段**（零下行控制）。
- V101 三方言迁移：在 `fm-security` 父菜单下登记按钮级菜单 `fm-security-gate-write` / `fm-security-bollard-write`（sort 135/136）并授权 ADMIN 及岗位角色；给 `fac_gate_control` / `fac_bollard` 增加 `version BIGINT DEFAULT 0` 乐观锁列。
- 契约四同步：前端 `docs/api/security.openapi.json` 为唯一真源；`check-api-contract.mjs --strict` 守门（目标 0 差异）。

## 非目标

- 不改只读列表的字段集与匹配语义。
- 不为道闸/防恐柱接入设备实时状态下发（零下行控制，`status` 仅读）。
- 不新增分页/批量写接口。

## 影响面

- 新增 `dto/GateControlWriteRequest.java`、`dto/BollardWriteRequest.java`。
- `SecurityService` 新增 `createGate`/`updateGate`/`deleteGate`、`createBollard`/`updateBollard`/`deleteBollard`，均标注 `@RealtimeSync`。
- `SecurityController` 新增 3 + 3 个写端点，全部 `@RequireAuth(perm=...)` + `@Valid`，`@Version` 乐观锁。
- 三方言迁移 `V101__security_device_ledger_perm.sql`（h2 / postgresql / dameng 同名同号）。
- 回退：移除 6 个写端点与 V101 种子、回退 `version` 列即可；前端恢复为只读台账。

## Capabilities

- `security`（安全防恐）：新增「道闸台账」与「防恐柱台账」全量 CRUD 能力，新增 `security.gate-control` / `security.bollard` 实时广播域。
