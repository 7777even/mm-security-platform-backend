-- =============================================================================
-- V50 数据权限三核心表补 zone 列（postgresql 方言）—— 与 h2/V50 语义完全一致
--   方言翻译：PG 用 ADD COLUMN 标准语法。
--   未实跑验证：本机无 PG 实例、无 docker，待 PG 环境激活后 flyway 校验。
-- =============================================================================

ALTER TABLE fac_alarm ADD COLUMN zone VARCHAR(64);
ALTER TABLE fac_video_camera ADD COLUMN zone VARCHAR(64);
ALTER TABLE fac_major_hazard ADD COLUMN zone VARCHAR(64);
