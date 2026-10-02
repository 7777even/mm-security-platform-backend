-- V100 安防检索 CRUD 权限与乐观锁列（PostgreSQL）

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '人员登记', 'fm-security-person-write', NULL, NULL, 132, 1, 0, 'BUTTON', 'security:person-write', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '车辆登记', 'fm-security-vehicle-write', NULL, NULL, 133, 1, 0, 'BUTTON', 'security:vehicle-write', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '周界删除', 'fm-security-perimeter-delete', NULL, NULL, 134, 1, 0, 'BUTTON', 'security:perimeter-delete', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code IN ('fm-security-person-write', 'fm-security-vehicle-write', 'fm-security-perimeter-delete');

ALTER TABLE fac_person_search ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE fac_vehicle_search ADD COLUMN version BIGINT DEFAULT 0;
