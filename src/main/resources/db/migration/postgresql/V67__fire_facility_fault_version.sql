-- V67 消防设施故障表乐观锁版本列（PostgreSQL 方言，未实跑验证）
-- 镜像自 h2/V67__fire_facility_fault_version.sql。本文件未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。
ALTER TABLE fac_fire_facility_fault ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
