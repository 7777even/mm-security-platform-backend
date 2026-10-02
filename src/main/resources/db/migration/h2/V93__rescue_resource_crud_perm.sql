-- =============================================================================
-- V93 救援资源四台账按钮级权限码种子（H2 方言）
--   1) 在 fm-emergency 下登记 4 个按钮级菜单：
--      rescue:personnel:write / rescue:brigade:write / rescue:vehicle:write / rescue:equipment:write
--   2) 授权 ADMIN 及指挥/调度岗（对齐 V48/V92 授权口径）
--   说明：V33「ADMIN 全量授权」在迁移时固化，新增按钮须显式补 sys_role_menu，
--        否则即便 ADMIN 也不持有该权限码，@RequireAuth(perm=...) 会 403。
--   口径：四个台账同属「应急救援资源」域，按资源分码而非合并成一个——
--        便于后续只放开某类台账的维护权限（如让装备管理员维护物资但不能改队伍编制）。
-- =============================================================================
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '救援人员维护', 'fm-emergency-rescue-personnel-write', NULL, NULL, 131, 1, 0, 'BUTTON', 'rescue:personnel:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '消防队伍维护', 'fm-emergency-rescue-brigade-write', NULL, NULL, 132, 1, 0, 'BUTTON', 'rescue:brigade:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '救援车辆维护', 'fm-emergency-rescue-vehicle-write', NULL, NULL, 133, 1, 0, 'BUTTON', 'rescue:vehicle:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '救援装备维护', 'fm-emergency-rescue-equipment-write', NULL, NULL, 134, 1, 0, 'BUTTON', 'rescue:equipment:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER')
  AND m.code IN ('fm-emergency-rescue-personnel-write', 'fm-emergency-rescue-brigade-write',
                 'fm-emergency-rescue-vehicle-write', 'fm-emergency-rescue-equipment-write');
