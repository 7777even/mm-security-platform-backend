-- V78 工业电视录像截图采集入库表（H2 方言）
-- 支撑「设备采集 → 录像截图入库 → 实时广播 → 大屏上屏」闭环链路。
-- 与 V26 fac_video_camera.snapshot_bytes 同源范式：截图以 BLOB 存库，字节端点按 id 取回。
CREATE TABLE fac_tv_snapshot (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    monitor_code   VARCHAR(32)  NOT NULL,
    monitor_name   VARCHAR(64),
    capture_time   VARCHAR(32),
    event_type     VARCHAR(32),
    review_status  VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    source         VARCHAR(16)  NOT NULL DEFAULT 'DEVICE',
    snapshot_bytes BLOB,
    created_at     VARCHAR(32)  NOT NULL,
    sort_no        INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_tv_snapshot_monitor ON fac_tv_snapshot (monitor_code);
