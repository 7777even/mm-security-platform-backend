-- =============================================================================
-- V48 业务写侧权限码种子（达梦 DM8 方言）—— 与 h2/V48 语义完全一致
--   方言翻译：INSERT...SELECT 为标准 SQL，达梦原生支持，逐语句照搬；
--   本文件无多行 VALUES，均为单条 INSERT...SELECT（逗号连接 join 达梦支持）。
--   未实跑验证：本机无 DM8 实例、无 docker，待 DM8 环境激活后 flyway 校验。
-- =============================================================================

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '应急指令下发', 'fm-emergency-command-write', NULL, NULL, 120, 1, 0, 'BUTTON', 'emergency:command:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '值班签到', 'fm-emergency-duty-write', NULL, NULL, 121, 1, 0, 'BUTTON', 'emergency:duty:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '台风资源调度', 'fm-emergency-typhoon-dispatch', NULL, NULL, 122, 1, 0, 'BUTTON', 'typhoon:dispatch:write', 0
FROM sys_menu p WHERE p.code = 'fm-emergency';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '巡更执行上报', 'fm-fire-patrol-write', NULL, NULL, 123, 1, 0, 'BUTTON', 'fire-alarm:patrol:write', 0
FROM sys_menu p WHERE p.code = 'fm-fire';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code = 'ADMIN'
  AND m.code IN ('fm-emergency-command-write', 'fm-emergency-duty-write',
                 'fm-emergency-typhoon-dispatch', 'fm-fire-patrol-write')
  AND m.deleted = 0 AND m.status = 1;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('COMMANDER', 'SCHEDULER')
  AND m.code IN ('fm-emergency-command-write', 'fm-emergency-typhoon-dispatch')
  AND m.deleted = 0 AND m.status = 1;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('SCHEDULER', 'TEAM_LEADER')
  AND m.code = 'fm-emergency-duty-write'
  AND m.deleted = 0 AND m.status = 1;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('TEAM_LEADER', 'OUTER_OPER')
  AND m.code = 'fm-fire-patrol-write'
  AND m.deleted = 0 AND m.status = 1;
