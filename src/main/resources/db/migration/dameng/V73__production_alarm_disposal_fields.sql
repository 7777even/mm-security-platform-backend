-- V73 生产报警处置字段持久化 + 乐观锁版本列（达梦 DM8 方言）
-- 与 h2/V73 同义；VARCHAR/BIGINT 在达梦中可直接使用（避开 status/level 等保留字，本批列名均非保留字）。
ALTER TABLE fac_production_alarm ADD COLUMN false_alarm VARCHAR(8);
ALTER TABLE fac_production_alarm ADD COLUMN handle_result VARCHAR(1024);
ALTER TABLE fac_production_alarm ADD COLUMN handle_time VARCHAR(32);
ALTER TABLE fac_production_alarm ADD COLUMN dispatch_personnel VARCHAR(512);
ALTER TABLE fac_production_alarm ADD COLUMN notify_method VARCHAR(32);
ALTER TABLE fac_production_alarm ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
