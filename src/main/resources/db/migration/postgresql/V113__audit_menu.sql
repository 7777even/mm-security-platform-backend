-- =============================================================================
-- V113 审计日志菜单（PostgreSQL 方言）
--   系统管理分组（code='system' 且 menu_type='DIR'）下挂载「审计日志」菜单，
--   权限码 system:audit:view，使审计日志管理页在后台管理端侧栏可见。
--   三方言同步：h2/、dameng/ 同文件（仅 INSERT sys_menu，无类型相关 DDL）。
--   与 V111 参数配置菜单同构：审计日志排在参数配置（sort_order 960）之后。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT id, '审计日志', 'system-audit', '/audit-log', 'Document', 970, 1, 0, 'MENU', 'system:audit:view', 1
FROM sys_menu WHERE code = 'system' AND menu_type = 'DIR';
