-- =============================================================================
-- V32 系统管理域 RBAC 数据模型（H2 方言）
--   新增：sys_role / sys_role_menu / sys_dict_type / sys_dict_item
--   扩展：sys_menu(+menu_type/perm_code/visible)、sys_user(+pwd_updated_at/must_change_pwd)
--   设计：openspec/changes/2026-09-10-system-management-rbac/design.md §5
--   三方言同步：postgresql/、dameng/ 同版本文件保持列定义一致（仅类型映射不同）。
--   说明：sys_menu.allowed_roles 降级为只读兼容列（授权数据迁移见 V33），本期不删除。
--   H2 保留字规避：不新增裸 type/value/time/mode/seq 列名（用 menu_type/item_value 等）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_role (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code    VARCHAR(64)  NOT NULL,
    role_name    VARCHAR(64)  NOT NULL,
    description  VARCHAR(255),
    data_scope   VARCHAR(16)  NOT NULL DEFAULT 'SELF',
    status       TINYINT      NOT NULL DEFAULT 1,
    built_in     TINYINT      NOT NULL DEFAULT 0,
    sort_order   INT          NOT NULL DEFAULT 0,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role(role_code);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    menu_id     BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu ON sys_role_menu(role_id, menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role ON sys_role_menu(role_id);

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code    VARCHAR(64)  NOT NULL,
    dict_name    VARCHAR(64)  NOT NULL,
    description  VARCHAR(255),
    status       TINYINT      NOT NULL DEFAULT 1,
    built_in     TINYINT      NOT NULL DEFAULT 0,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_type_code ON sys_dict_type(dict_code);

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code    VARCHAR(64)  NOT NULL,
    item_value   VARCHAR(128) NOT NULL,
    item_label   VARCHAR(128) NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1,
    description  VARCHAR(255),
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sys_dict_item_code ON sys_dict_item(dict_code);

-- sys_menu：承载菜单类型与权限码（权限即菜单树节点，ADR-2）
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS menu_type VARCHAR(16) NOT NULL DEFAULT 'MENU';
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS perm_code VARCHAR(128);
ALTER TABLE sys_menu ADD COLUMN IF NOT EXISTS visible   TINYINT     NOT NULL DEFAULT 1;

-- sys_user：口令生命周期（改密时间 / 强制首登改密）
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS pwd_updated_at  TIMESTAMP;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS must_change_pwd TINYINT NOT NULL DEFAULT 0;

-- 既有 5 条 fm-* 顶层模块为目录节点
UPDATE sys_menu SET menu_type = 'DIR' WHERE code LIKE 'fm-%';
