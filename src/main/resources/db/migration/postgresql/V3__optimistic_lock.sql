-- 乐观锁版本列（PostgreSQL 方言，生产回退 profile）
-- 与 H2 V9 对齐；三方言同步加列。PG 当前为回退 profile，本文件在启用 prod profile 时随 Flyway 执行。
-- BIGINT DEFAULT 0 保证存量行与插入默认值一致。

ALTER TABLE fac_alarm ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_security_event ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_fire_alarm ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_field_report ADD COLUMN version BIGINT DEFAULT 0;
