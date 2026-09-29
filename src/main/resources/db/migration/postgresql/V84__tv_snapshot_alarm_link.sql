-- V84 工业电视录像截图关联生产告警（跨域联动，PostgreSQL 方言）
-- 为 fac_tv_snapshot 增加 alarm_id / alarm_type 关联列，使生产告警详情可精准内嵌关联抓拍
-- （取代此前前端按 location 字符串软匹配的方式）。
ALTER TABLE fac_tv_snapshot ADD COLUMN alarm_id BIGINT;
ALTER TABLE fac_tv_snapshot ADD COLUMN alarm_type VARCHAR(32);
CREATE INDEX idx_tv_snapshot_alarm ON fac_tv_snapshot (alarm_id);
