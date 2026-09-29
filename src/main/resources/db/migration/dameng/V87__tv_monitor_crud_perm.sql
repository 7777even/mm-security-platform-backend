-- =============================================================================
-- V87 工业电视监控点位管理按钮级权限码种子（达梦 DM8 方言）
--   1) 在 fm-tv 下登记按钮级菜单 tv:monitor:create / tv:monitor:update / tv:monitor:delete
--      （前端设备/防区管理页的新增/编辑/删除操作使用）
--   2) 授权 ADMIN 及五类岗位角色（对齐 V80/V83 的授权口径）
--   说明：V33 的「ADMIN 全量授权」在迁移时已固化，新增按钮须显式补 sys_role_menu，
--        否则即便 ADMIN 也不持有该权限码，@RequireAuth(perm=...) 会 403。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '监控点位新增', 'fm-tv-monitor-create', NULL, NULL, 141, 1, 0, 'BUTTON', 'tv:monitor:create', 0
FROM sys_menu p WHERE p.code = 'fm-tv';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '监控点位编辑', 'fm-tv-monitor-update', NULL, NULL, 142, 1, 0, 'BUTTON', 'tv:monitor:update', 0
FROM sys_menu p WHERE p.code = 'fm-tv';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '监控点位删除', 'fm-tv-monitor-delete', NULL, NULL, 143, 1, 0, 'BUTTON', 'tv:monitor:delete', 0
FROM sys_menu p WHERE p.code = 'fm-tv';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code IN ('fm-tv-monitor-create', 'fm-tv-monitor-update', 'fm-tv-monitor-delete');
