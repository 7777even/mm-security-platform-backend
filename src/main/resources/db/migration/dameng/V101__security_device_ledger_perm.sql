-- V101 安防设备台账 CRUD 权限与乐观锁列（达梦 DM8）

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '道闸台账', 'fm-security-gate-write', NULL, NULL, 135, 1, 0, 'BUTTON', 'security:gate-write', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '升降柱台账', 'fm-security-bollard-write', NULL, NULL, 136, 1, 0, 'BUTTON', 'security:bollard-write', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code IN ('fm-security-gate-write', 'fm-security-bollard-write');

ALTER TABLE fac_gate_control ADD version NUMBER(19) DEFAULT 0;
ALTER TABLE fac_bollard ADD version NUMBER(19) DEFAULT 0;
