-- 乐观锁版本列（达梦 DM8 方言，Oracle 兼容模式）
-- 与 H2 V9 对齐；三方言同步加列。DM 当前为暂停/迁移资产，本文件在启用 DM profile 时随 Flyway 执行。
-- 达梦 NUMBER(19) 对应 BIGINT；DEFAULT 0 保证存量行与插入默认值一致。

ALTER TABLE fac_alarm ADD version NUMBER(19) DEFAULT 0;
ALTER TABLE fac_security_event ADD version NUMBER(19) DEFAULT 0;
ALTER TABLE fac_fire_alarm ADD version NUMBER(19) DEFAULT 0;
ALTER TABLE fac_field_report ADD version NUMBER(19) DEFAULT 0;
