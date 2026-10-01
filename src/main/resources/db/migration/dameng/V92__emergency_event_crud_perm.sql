-- =============================================================================
-- V92 应急事件编辑/删除按钮级权限码种子（达梦 DM8 方言）
--   1) 在 fm-emergency 下登记按钮级菜单 emergency:event:write
--   2) 授权 ADMIN 及指挥/调度岗（对齐 V48/V91 授权口径）
--   说明：V33「ADMIN 全量授权」在迁移时固化，新增按钮须显式补 sys_role_menu，
--        否则即便 ADMIN 也不持有该权限码，@RequireAuth(perm=...) 会 403。
--   口径：create/report/start-response 保持「仅登录态」自助写入（沿用既有口径），
--        仅 update/delete 收权限码——避免大屏值守岗自助上报被权限码挡住。
-- =============================================================================
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '应急事件维护', 'fm-emergency-event-write', NULL, NULL, 130, 1, 0, 'BUTTON', 'emergency:event:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER')
  AND m.code = 'fm-emergency-event-write';
