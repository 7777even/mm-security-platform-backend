-- =============================================================================
-- V95 应急通讯录按钮级权限码种子（达梦方言）
--   1) 在 fm-emergency 下登记按钮级菜单：emergency:phone:write
--   2) 授权 ADMIN 及指挥/调度岗（对齐 V48/V92/V93/V94 授权口径）
--   说明：V33「ADMIN 全量授权」在迁移时固化，新增按钮须显式补 sys_role_menu，
--        否则即便 ADMIN 也不持有该权限码，@RequireAuth(perm=...) 会 403。
-- =============================================================================
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '通讯录维护', 'fm-emergency-phone-write', NULL, NULL, 136, 1, 0, 'BUTTON', 'emergency:phone:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER')
  AND m.code = 'fm-emergency-phone-write';
