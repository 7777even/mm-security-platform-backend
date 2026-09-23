-- =============================================================================
-- V70 周界入侵告警处置按钮级权限码种子 —— 达梦 DM8 方言（Oracle 兼容）
--   与 H2 V70 对齐。DM8 无官方 Flyway 模块，本文件为迁移资产，须到达梦实例上复核执行。
--   说明：INSERT...SELECT 引用 sys_menu p，无需 FROM dual（常量 SELECT 才需 dual）。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '周界告警处置', 'fm-security-perimeter-ack', NULL, NULL, 130, 1, 0, 'BUTTON', 'security:perimeter-ack', 0
FROM sys_menu p WHERE p.code = 'fm-security';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code = 'fm-security-perimeter-ack';
