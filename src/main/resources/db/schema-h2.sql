-- H2 内存库启动脚本（dev 环境兜底，与 PostgreSQL 表结构保持一致）
-- 用户
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    role VARCHAR(32) NOT NULL DEFAULT 'VIEWER',
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 菜单/权限
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(128),
    path VARCHAR(255),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 设备（主键为 20 位 MDM 编码，非自增）
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

-- 告警
CREATE TABLE IF NOT EXISTS fac_alarm (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_code CHAR(20),
    level TINYINT NOT NULL,
    type VARCHAR(32),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status TINYINT NOT NULL DEFAULT 0,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 默认账号 admin/admin@2026
MERGE INTO sys_user (id, username, password_hash, real_name, role) KEY(id) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'ADMIN');
