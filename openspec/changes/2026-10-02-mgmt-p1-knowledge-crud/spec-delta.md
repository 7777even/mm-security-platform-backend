# Spec Delta: 应急知识库台账写端点

## ADDED Requirements

### Requirement: 应急知识库写端点

系统应为 `sys_knowledge_item` 台账提供 `POST /api/v1/emergency/knowledge`、
`PUT` 与 `DELETE /api/v1/emergency/knowledge/{id}`。

- **权限**：需 `emergency:knowledge:write`（V94 登记，授权 ADMIN / COMMANDER / SCHEDULER）。
- **新增必填**：`title`；缺失或空白返回 B3 `PARAM_INVALID`（文案点名 `title`）。
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改。
- **删除**：物理删除（表无 `deleted` 列）；不存在（含重复删除）返回 B3 `NOT_FOUND`。
- **主键**：新增行 id 取当前 `max(id) + 1`，空表从 1 起（避免 V8 种子显式插 id 撞主键）。
- **广播**：成功写入后发布 `emergency.knowledge` 域变更。
- **缓存**：写后失效 `knowledgeCache`，保证大屏/管理端实时一致。

#### Scenario: 局部更新不覆盖未传字段

- **WHEN** `PUT /api/v1/emergency/knowledge/4` 请求体为 `{"title":"新标题","icon":"New"}`
- **THEN** 该行 `title` 变「新标题」、`icon` 变「New」，`count`/`description` 保持原值。

#### Scenario: 新增必填缺失

- **WHEN** `POST /api/v1/emergency/knowledge` 未传 `title`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何插入。

#### Scenario: 重复删除

- **WHEN** 对同一个已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）。

## MODIFIED Requirements

### Requirement: 应急知识库接口不再是只读

`EmergencyController` 的 `/emergency/knowledge` 由「仅 GET 查询」变更为读 + 写；
读端点仍需登录态，写端点另需按钮级权限码 `emergency:knowledge:write`。既有 GET 语义不变。
