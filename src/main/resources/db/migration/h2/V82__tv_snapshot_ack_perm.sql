-- =============================================================================
-- V79 工业电视录像截图确认按钮级权限码种子（H2 方言）
--   1) 在 fm-tv 下登记按钮级菜单 video:snapshot:ack（前端截图确认操作使用）
--   2) 授权 ADMIN 及五类岗位角色（对齐 V70/V76 的 security:* 授权口径）
--   说明：V33 的「ADMIN 全量授权」在迁移时已固化，新增按钮须显式补 sys_role_menu，
--        否则即便 ADMIN 也不持有该权限码，@RequireAuth(perm=...) 会 403。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '录像截图确认', 'fm-tv-snapshot-ack', NULL, NULL, 132, 1, 0, 'BUTTON', 'video:snapshot:ack', 0
FROM sys_menu p WHERE p.code = 'fm-tv';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code = 'fm-tv-snapshot-ack';
