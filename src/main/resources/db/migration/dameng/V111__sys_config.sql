-- V111 系统参数配置中心（达梦 DM8 / Oracle 兼容）
-- 镜像自 h2/V111__sys_config.sql：NUMBER(19) IDENTITY(1,1) + VARCHAR2(... CHAR) + NUMBER(3)。
-- 注意：达梦不支持 IF NOT EXISTS，直接 CREATE（Flyway 按版本幂等应用一次）。
CREATE TABLE sys_config (
    id           NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    config_key   VARCHAR2(128 CHAR) NOT NULL,
    config_value VARCHAR2(2000 CHAR),
    config_name  VARCHAR2(128 CHAR),
    config_group VARCHAR2(64 CHAR),
    config_type  VARCHAR2(16 CHAR)  NOT NULL DEFAULT 'STRING',
    options      VARCHAR2(500 CHAR),
    remark       VARCHAR2(255 CHAR),
    sort_order   NUMBER(9) NOT NULL DEFAULT 0,
    status       NUMBER(3) NOT NULL DEFAULT 1,
    built_in     NUMBER(3) NOT NULL DEFAULT 0,
    deleted      NUMBER(3) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_sys_config_key ON sys_config(config_key);

-- 系统管理分组下挂载「参数配置」菜单（权限码 system:config:view），使配置中心在侧栏可见
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT id, '参数配置', 'system-config', '/system/configs', 'Setting', 960, 1, 0, 'MENU', 'system:config:view', 1
FROM sys_menu WHERE code = 'system' AND menu_type = 'DIR';
