# Design: fac_audit_log.actor 服务端锚定

## 数据层（三方言迁移）
- 新增迁移 `V108__audit_log_actor.sql`（h2 / postgresql / dameng 三目录各一份）：
  `ALTER TABLE fac_audit_log ADD actor VARCHAR(64)`。
- 列可空（既有行无 actor，迁移不能 NOT NULL）；新行始终由服务端写入。
- H2 dev 文件库已应用 V≤107，不可改原文件，必须新增 V108（Flyway checksum 规则）。
- 达梦语法注意：不支持 `ADD COLUMN IF NOT EXISTS`，直接 `ALTER TABLE ... ADD actor VARCHAR(64)`；
  PG/H2 同样不加 `IF NOT EXISTS`（保证三方言一致、幂等于"未应用→应用"语义）。

## 实体与 DTO
- `entity/FacAuditLog.java`：新增 `private String actor;`（`@TableField("actor")`）。
- `dto/AuditLogItem.java`：新增 `private String actor;`，`UplinkService.toAuditItem` 映射 `e.getActor()`。
- 输入 `AuditEvent` / `AuditEventBatch` **不加 actor 字段**（服务端覆盖，客户端不传提交人）。

## 落库逻辑
- `UplinkService.reportAudit`：遍历事件前取 `String actor = UserContext.username();`；
  若 `actor == null` → 抛 `BusinessException(ResultCode.UNAUTHORIZED, "未登录或令牌过期")`
  （防御性，`@RequireAuth` 保证非 null）；逐条 `log.setActor(actor)`。
- 不改动 `best-effort` 语义（单条失败继续），但 actor 取一次、所有事件同源。

## 契约与前端
- `docs/api/uplink.openapi.json`：`AuditLogItem` 增 `actor` 属性（中文 description「操作提交人，
  服务端按当前登录态写入，客户端不可伪造」）+ 更新示例；**外科式字符串插入**，不整份回写。
- 前端 `npm run gen:api-types` 重生 `AuditLogItem` 类型含 `actor`；`apps/mgmt/views/system/AuditView.vue`
  增 `el-table-column prop="actor" label="操作人"`（默认仅展示不筛选）。
