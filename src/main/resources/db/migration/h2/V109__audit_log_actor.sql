-- V108 审计提交人溯源（H2）
-- 背景：渗透复核清单 §6 标记风险项——POST /api/v1/audit/log 登录即可提交，且 fac_audit_log
--       无操作人字段、内容纯客户端断言，导致任意登录用户可伪造审计记录且不可溯源。
-- 处置（方案 A）：新增 actor 列，落库时由服务端按当前登录态写入（UplinkService.reportAudit），
--       客户端不可伪造。列可空以兼容历史行，新行始终由服务端写入。

ALTER TABLE fac_audit_log ADD actor VARCHAR(64);
