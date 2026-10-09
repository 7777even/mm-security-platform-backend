-- V111 系统参数配置中心（PostgreSQL）
-- 镜像自 h2/V111__sys_config.sql：BIGINT AUTO_INCREMENT -> BIGSERIAL，TINYINT -> SMALLINT。
CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGSERIAL PRIMARY KEY,
    config_key   VARCHAR(128) NOT NULL,
    config_value VARCHAR(2000),
    config_name  VARCHAR(128),
    config_group VARCHAR(64),
    config_type  VARCHAR(16)  NOT NULL DEFAULT 'STRING',
    options      VARCHAR(500),
    remark       VARCHAR(255),
    sort_order   INT          NOT NULL DEFAULT 0,
    status       SMALLINT     NOT NULL DEFAULT 1,
    built_in     SMALLINT     NOT NULL DEFAULT 0,
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_config_key ON sys_config(config_key);

-- 系统管理分组下挂载「参数配置」菜单（权限码 system:config:view），使配置中心在侧栏可见
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT id, '参数配置', 'system-config', '/system/configs', 'Setting', 960, 1, 0, 'MENU', 'system:config:view', 1
FROM sys_menu WHERE code = 'system' AND menu_type = 'DIR';
