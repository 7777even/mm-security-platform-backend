-- =============================================================================
-- V111 系统参数配置中心（H2 方言）
--   新增 sys_config 表，支撑运行期可配项（标题、阈值开关、ABAC 防区规则等）自助管理。
--   三方言同步：postgresql/、dameng/ 同版本文件保持列定义一致（仅类型映射不同）。
--   H2 保留字规避：不新增裸 type/value/key/group 列名（用 config_type/config_value/config_key/config_group）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(128) NOT NULL,
    config_value VARCHAR(2000),
    config_name  VARCHAR(128),
    config_group VARCHAR(64),
    config_type  VARCHAR(16)  NOT NULL DEFAULT 'STRING',
    options      VARCHAR(500),
    remark       VARCHAR(255),
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1,
    built_in     TINYINT      NOT NULL DEFAULT 0,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_config_key ON sys_config(config_key);

-- 系统管理分组下挂载「参数配置」菜单（权限码 system:config:view），使配置中心在侧栏可见
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT id, '参数配置', 'system-config', '/system/configs', 'Setting', 960, 1, 0, 'MENU', 'system:config:view', 1
FROM sys_menu WHERE code = 'system' AND menu_type = 'DIR';
