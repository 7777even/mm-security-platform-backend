-- =============================================================================
-- V50 数据权限三核心表补 zone 列（达梦 DM8 方言）—— 与 h2/V50 语义完全一致
--   方言翻译：达梦 ADD 子句不带 COLUMN 关键字（见 dameng/V45__user_token_version.sql 范式）。
--   未实跑验证：本机无 DM8 实例、无 docker，待 DM8 环境激活后 flyway 校验。
-- =============================================================================

ALTER TABLE fac_alarm ADD zone VARCHAR2(64 CHAR);
ALTER TABLE fac_video_camera ADD zone VARCHAR2(64 CHAR);
ALTER TABLE fac_major_hazard ADD zone VARCHAR2(64 CHAR);
