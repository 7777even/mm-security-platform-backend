-- V65 消防报警处置字段持久化（PostgreSQL 方言，未实跑验证）
-- 镜像自 h2/V65__fire_alarm_disposal_fields.sql。本文件未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。
-- 字段说明：handle_result 处置情况文本 / handle_time 处置时间 / dispatch_personnel 派单人员 / notify_method 通知方式。

ALTER TABLE fac_fire_alarm ADD COLUMN handle_result VARCHAR(1024);
ALTER TABLE fac_fire_alarm ADD COLUMN handle_time VARCHAR(32);
ALTER TABLE fac_fire_alarm ADD COLUMN dispatch_personnel VARCHAR(512);
ALTER TABLE fac_fire_alarm ADD COLUMN notify_method VARCHAR(32);
