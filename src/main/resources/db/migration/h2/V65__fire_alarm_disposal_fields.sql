-- V65 消防报警处置字段持久化：处置情况文本 / 处置时间 / 派单人员 / 通知方式
-- 仅对 fac_fire_alarm 追加 4 列；共享环境后禁止改/删已有 V-file，新增列必须新开版本号（V5/V9 已应用，不可回改）。
-- 字段说明：
--   handle_result      处置情况文本（可空）
--   handle_time        处置时间（格式 yyyy-MM-dd HH:mm:ss，可空）
--   dispatch_personnel 派单人员（多个以英文逗号分隔，可空）
--   notify_method      通知方式（APP/SMS，多个以英文逗号分隔，如 APP,SMS，可空）

ALTER TABLE fac_fire_alarm ADD COLUMN handle_result VARCHAR(1024);
ALTER TABLE fac_fire_alarm ADD COLUMN handle_time VARCHAR(32);
ALTER TABLE fac_fire_alarm ADD COLUMN dispatch_personnel VARCHAR(512);
ALTER TABLE fac_fire_alarm ADD COLUMN notify_method VARCHAR(32);
