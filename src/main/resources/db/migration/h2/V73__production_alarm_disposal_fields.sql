-- V73 生产报警处置字段持久化 + 乐观锁版本列（H2 方言）
-- 支撑生产报警详情写回落库（确认/处理中/已处置状态流转 + 误报标记 + 处置情况/时间/派单人员/通知方式）。
-- 仅对 fac_production_alarm 追加 6 列；共享环境后禁止改/删已有 V-file，新增列必须新开版本号。
-- 字段说明：
--   false_alarm         是否误报（是/否/未核实，可空）
--   handle_result       处置情况文本（可空）
--   handle_time         处置时间（yyyy-MM-dd HH:mm:ss，可空）
--   dispatch_personnel  派单人员（多个以英文逗号分隔，可空）
--   notify_method       通知方式（APP/SMS，多个以英文逗号分隔，可空）
--   version             乐观锁版本列（默认 0）

ALTER TABLE fac_production_alarm ADD COLUMN false_alarm VARCHAR(8);
ALTER TABLE fac_production_alarm ADD COLUMN handle_result VARCHAR(1024);
ALTER TABLE fac_production_alarm ADD COLUMN handle_time VARCHAR(32);
ALTER TABLE fac_production_alarm ADD COLUMN dispatch_personnel VARCHAR(512);
ALTER TABLE fac_production_alarm ADD COLUMN notify_method VARCHAR(32);
ALTER TABLE fac_production_alarm ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
