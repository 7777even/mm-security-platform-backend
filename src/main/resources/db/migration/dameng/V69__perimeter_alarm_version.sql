-- =============================================================================
-- V69 周界入侵告警表乐观锁版本列（达梦 DM8 方言，Oracle 兼容，未实跑验证）
--   镜像自 dameng/V67__fire_facility_fault_version.sql。本文件未经达梦实例实跑验证，按 DM8 语法编写，需上环境复核。
--   DM8 不支持 COLUMN 关键字，ADD 后跟列名（无需 COLUMN 关键字）；
--   历史行由 DEFAULT 0 填充，保证 MyBatis-Plus @Version 乐观锁非空（与 V67 加列风格保持一致）。
-- =============================================================================

ALTER TABLE fac_perimeter_alarm ADD version BIGINT DEFAULT 0;
