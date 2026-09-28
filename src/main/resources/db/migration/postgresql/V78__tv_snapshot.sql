-- V78 工业电视录像截图采集入库表（PostgreSQL 方言，未实跑验证）
-- 镜像自 h2/V78。本文件未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。
CREATE TABLE fac_tv_snapshot (
    id             BIGSERIAL PRIMARY KEY,
    monitor_code   VARCHAR(32)  NOT NULL,
    monitor_name   VARCHAR(64),
    capture_time   VARCHAR(32),
    event_type     VARCHAR(32),
    review_status  VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    source         VARCHAR(16)  NOT NULL DEFAULT 'DEVICE',
    snapshot_bytes BYTEA,
    created_at     VARCHAR(32)  NOT NULL,
    sort_no        INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_tv_snapshot_monitor ON fac_tv_snapshot (monitor_code);
