-- V9 数据迁移 —— PostgreSQL 方言（未实跑验证）
-- 镜像自 h2/V9。本文件未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。

-- 乐观锁版本列（optimistic locking）
-- 由 MyBatis-Plus @Version 在 update 时自动比对 WHERE version=? 并自增；默认 0。
-- 仅对状态/数值可变、会被并发更新的表加列：fac_alarm / fac_security_event / fac_fire_alarm / fac_field_report。
-- 共享环境后禁止改/删已有 V-file；本文件为新增 V9（H2 方言，本地可验证）。

ALTER TABLE fac_alarm ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_security_event ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_fire_alarm ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_field_report ADD COLUMN version BIGINT DEFAULT 0;
