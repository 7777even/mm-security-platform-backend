-- V105 重大危险源 / 监测点位 / 特殊作业票写支持
-- 加乐观锁 version 列（默认 0），供 MyBatis-Plus @Version 与实时广播（@RealtimeSync）使用。
-- 仅加列，不改动既有种子数据与自增序列（H2 / PostgreSQL / 达梦三方言一致）。
ALTER TABLE fac_major_hazard ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_monitoring_point ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_special_operation_ticket ADD COLUMN version BIGINT DEFAULT 0;
