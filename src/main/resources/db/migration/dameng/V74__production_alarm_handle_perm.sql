-- V74 生产报警处置按钮级权限码种子（达梦 DM8 方言）
-- 与 h2/V74 同义；sys_menu / sys_role / sys_role_menu 三表在达梦中同名同结构。
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '生产报警处置', 'fm-production-alarm-ack', NULL, NULL, 130, 1, 0, 'BUTTON', 'production:ack', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code = 'fm-production-alarm-ack';
