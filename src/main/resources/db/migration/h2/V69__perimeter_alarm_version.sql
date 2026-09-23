-- V69 周界入侵告警表乐观锁版本列（H2 方言）
--   镜像自 h2/V67__fire_facility_fault_version.sql，支撑周界告警确认/派单/处置状态流转写回的并发防护。
--   仅对 fac_perimeter_alarm 追加 1 列；共享环境后禁止改/删已有 V-file，新增列必须新开版本号。
ALTER TABLE fac_perimeter_alarm ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
