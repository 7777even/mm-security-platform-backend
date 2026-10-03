-- =============================================================================
-- V99 消防设施台账新增/编辑/删除按钮级权限码种子（达梦 DM8 方言）
--   1) 在 fm-fire 下登记按钮级菜单 fire-facility:ledger:write（覆盖新增/编辑/删除）
--   2) 授权 ADMIN 及五类岗位角色（对齐 V33/V68/V90/V91 授权口径）
--   说明：V33「ADMIN 全量授权」在迁移时固化，新增按钮须显式补 sys_role_menu，
--        否则即便 ADMIN 也不持有该权限码，@RequireAuth(perm=...) 会 403。
-- =============================================================================
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '消防设施台账编辑', 'fm-fire-ledger-write', NULL, NULL, 124, 1, 0, 'BUTTON', 'fire-facility:ledger:write', 0
FROM sys_menu p WHERE p.code = 'fm-fire';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code IN ('fm-fire-ledger-write');
