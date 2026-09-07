-- =============================================================================
-- V1 全量快照（空库直达最新结构）—— H2 方言
--   双轨策略中的「完整快照」：新建库时由本文件一次性建出全部表，
--   后续 DDL 变更一律新增 V<yyyyMMddHHmmss>__<snake>.sql 增量文件，禁止改本文件。
--   已进共享环境的 V 文件禁止修改 / 重命名 / 删除（AGENTS.md §数据库）。
--   源真值：原 src/main/resources/schema.sql（dev H2 快照，含 fac_alarm 全列）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    role VARCHAR(32) NOT NULL DEFAULT 'VIEWER',
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(128),
    path VARCHAR(255),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fac_device (
    device_code CHAR(20) PRIMARY KEY,
    device_name VARCHAR(128) NOT NULL,
    device_type VARCHAR(32),
    zone VARCHAR(64),
    status TINYINT NOT NULL DEFAULT 0,
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fac_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alarm_id VARCHAR(32),
    device_code CHAR(20),
    level TINYINT NOT NULL,
    type VARCHAR(32),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status TINYINT NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP NOT NULL,
    location VARCHAR(128),
    category VARCHAR(32),
    warned BOOLEAN DEFAULT FALSE,
    plan_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fac_alarm_alarm_id UNIQUE (alarm_id)
);

CREATE TABLE IF NOT EXISTS fac_workstation (
    workstation_id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    zone VARCHAR(64),
    online BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fac_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(128) NOT NULL,
    module VARCHAR(128),
    detail_json CLOB,
    event_at BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
