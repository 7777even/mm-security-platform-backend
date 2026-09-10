-- =============================================================================
-- V32 系统管理域 RBAC 数据模型 —— PostgreSQL 方言（生产 profile）
--   与 H2 V32 对齐；三方言保持列定义一致（仅类型映射不同）：
--     BIGINT AUTO_INCREMENT -> BIGSERIAL，TINYINT -> SMALLINT。
--   说明：本文件未经 PG 实例实跑验证（本地无 PG 实例），按标准 PG 语法编写，需上环境复核。
--   sys_menu.allowed_roles 降级为只读兼容列（授权数据迁移见 V33），本期不删除。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_role (
    id           BIGSERIAL PRIMARY KEY,
    role_code    VARCHAR(64)  NOT NULL,
    role_name    VARCHAR(64)  NOT NULL,
    description  VARCHAR(255),
    data_scope   VARCHAR(16)  NOT NULL DEFAULT 'SELF',
    status       SMALLINT     NOT NULL DEFAULT 1,
    built_in     SMALLINT     NOT NULL DEFAULT 0,
    sort_order   INT          NOT NULL DEFAULT 0,
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role(role_code);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    menu_id     BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu ON sys_role_menu(role_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu(role_id);

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id           BIGSERIAL PRIMARY KEY,
    dict_code    VARCHAR(64)  NOT NULL,
    dict_name    VARCHAR(64)  NOT NULL,
    description  VARCHAR(255),
    status       SMALLINT     NOT NULL DEFAULT 1,
    built_in     SMALLINT     NOT NULL DEFAULT 0,
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_type_code ON sys_dict_type(dict_code);

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id           BIGSERIAL PRIMARY KEY,
    dict_code    VARCHAR(64)  NOT NULL,
    item_value   VARCHAR(128) NOT NULL,
    item_label   VARCHAR(128) NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    status       SMALLINT     NOT NULL DEFAULT 1,
    description  VARCHAR(255),
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sys_dict_item_code ON sys_dict_item(dict_code);

ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS menu_type VARCHAR(16) NOT NULL DEFAULT 'MENU';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS perm_code VARCHAR(128);
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS visible   SMALLINT    NOT NULL DEFAULT 1;

ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS pwd_updated_at  TIMESTAMP;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS must_change_pwd SMALLINT NOT NULL DEFAULT 0;

UPDATE sys_menu SET menu_type = 'DIR' WHERE code LIKE 'fm-%';
