-- =============================================================================
-- V65 消防报警处置字段持久化（达梦 DM8 方言，Oracle 兼容，未实跑验证）
--   镜像自 h2/V65__fire_alarm_disposal_fields.sql。本文件未经达梦实例实跑验证，按 DM8 语法编写，需上环境复核。
--   DM8 不支持 IF NOT EXISTS，新增列必须新开版本号；ADD 后跟列名（无需 COLUMN 关键字）。
--   字段说明：handle_result 处置情况文本 / handle_time 处置时间 / dispatch_personnel 派单人员 / notify_method 通知方式。
-- =============================================================================

ALTER TABLE fac_fire_alarm ADD handle_result VARCHAR2(1024 CHAR);
ALTER TABLE fac_fire_alarm ADD handle_time VARCHAR2(32 CHAR);
ALTER TABLE fac_fire_alarm ADD dispatch_personnel VARCHAR2(512 CHAR);
ALTER TABLE fac_fire_alarm ADD notify_method VARCHAR2(32 CHAR);
