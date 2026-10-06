# Spec Delta: uplink-audit

## 变更点
更新 `openspec/specs/uplink-audit/spec.md`，在「审计日志批量落库」需求下新增提交人服务端锚定约束：

- 系统 SHALL 在 `POST /api/v1/audit/log` 落库 `fac_audit_log` 时，将 `actor` 字段设为当前认证用户
  （`UserContext.username()`），不得接受或信任客户端传入的提交人身份。
- `GET /api/v1/audit/log` 返回的 `AuditLogItem` SHALL 包含 `actor`（操作提交人，服务端写入）。
- 新增 Scenario：上报审计时提交人由服务端按登录态写入（客户端不可伪造） / 查询审计返回提交人。

## 已合入
内容已写入 `openspec/specs/uplink-audit/spec.md`（对应 Requirement 段落）。
