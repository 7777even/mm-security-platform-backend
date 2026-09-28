-- =============================================================================
-- V79 工业电视录像截图确认按钮级权限码种子（PostgreSQL 方言，未实跑验证）
--   镜像自 h2/V79。本文件未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '录像截图确认', 'fm-tv-snapshot-ack', NULL, NULL, 132, 1, 0, 'BUTTON', 'video:snapshot:ack', 0
FROM sys_menu p WHERE p.code = 'fm-tv';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code = 'fm-tv-snapshot-ack';
