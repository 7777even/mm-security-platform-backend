-- =============================================================================
-- V68 消防设施故障处置按钮级权限码种子 —— PostgreSQL 方言（生产 profile）
--   与 H2 V68 逐行对齐。本文件未经 PG 实例实跑验证，按标准 PG 语法编写，需上环境复核。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '消防设施故障处置', 'fm-fire-facility-handle', NULL, NULL, 120, 1, 0, 'BUTTON', 'fire-facility:handle', 0
FROM sys_menu p WHERE p.code = 'fm-fire';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code = 'fm-fire-facility-handle';
