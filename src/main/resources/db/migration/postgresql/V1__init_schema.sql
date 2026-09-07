-- =============================================================================
-- V1 全量快照（空库直达最新结构）—— PostgreSQL 方言
--   双轨策略中的「完整快照」。后续 DDL 变更一律新增 V<yyyyMMddHHmmss>__<snake>.sql 增量文件。
--   与 H2 / 达梦 两套方言保持列定义一致（仅类型映射不同）：
--     BIGINT/TINYINT -> BIGSERIAL/SMALLINT，TEXT/CLOB -> TEXT，BOOLEAN 同源，
--     CHAR(20) 设备物理主键、VARCHAR 业务列同源。
--   说明：本文件未经 PostgreSQL 实例实跑验证（本地无 PG 实例），按标准 PG 语法编写，需上环境复核。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    role VARCHAR(32) NOT NULL DEFAULT 'VIEWER',
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(128),
    path VARCHAR(255),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fac_device (
    device_code CHAR(20) PRIMARY KEY,
    device_name VARCHAR(128) NOT NULL,
    device_type VARCHAR(32),
    zone VARCHAR(64),
    status SMALLINT NOT NULL DEFAULT 0,
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fac_alarm (
    id BIGSERIAL PRIMARY KEY,
    alarm_id VARCHAR(32),
    device_code CHAR(20),
    level SMALLINT NOT NULL,
    type VARCHAR(32),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status SMALLINT NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP NOT NULL,
    location VARCHAR(128),
    category VARCHAR(32),
    warned BOOLEAN DEFAULT FALSE,
    plan_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fac_alarm_alarm_id UNIQUE (alarm_id)
);

CREATE TABLE IF NOT EXISTS fac_workstation (
    workstation_id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    zone VARCHAR(64),
    online BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fac_audit_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(128) NOT NULL,
    module VARCHAR(128),
    detail_json TEXT,
    event_at BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
