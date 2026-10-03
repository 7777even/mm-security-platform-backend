-- V102 防火巡查记录台账 CRUD 权限与乐观锁列（达梦 DM8）
-- 注意：fm-fire-patrol-write 已被 V48 占用（巡更执行 fire-alarm:patrol:write），
-- 本迁移改用 fm-fire-patrol-record-write（perm fire:patrol-write）避免 sys_menu.code 唯一约束冲突。

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '防火巡查台账编辑', 'fm-fire-patrol-record-write', NULL, NULL, 125, 1, 0, 'BUTTON', 'fire:patrol-write', 0
FROM sys_menu p WHERE p.code = 'fm-fire';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code = 'fm-fire-patrol-record-write';

ALTER TABLE fac_fire_patrol ADD COLUMN version NUMBER(19) DEFAULT 0;
