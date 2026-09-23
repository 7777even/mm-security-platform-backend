-- V67 消防设施故障表乐观锁版本列：对齐消防报警 fac_fire_alarm（建表即含 version），
-- 支撑故障状态流转写回（确认/派单/维修/验收）的并发防护。
-- 仅对 fac_fire_facility_fault 追加 1 列；共享环境后禁止改/删已有 V-file，新增列必须新开版本号。
ALTER TABLE fac_fire_facility_fault ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
