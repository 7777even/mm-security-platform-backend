-- =============================================================================
-- V48 业务写侧权限码种子（H2 方言）—— A2「D 类写侧」落地之 D3 独立权限码
--   背景：V47 已建 4 张写侧记录表；本迁移为对应 4 个写端点登记 BUTTON 级权限码，
--        并做角色授权，供 @RequireAuth(perm = "...") 在服务端逐请求解析
--        （经 RoleAuthorityService 按 sys_role_menu 解析，写时失效、即时生效）。
--   权限码口径（沿用 V33 约定）：业务域权限码与前端 meta.perm 同源，
--        但本批 4 个为 **按钮级写权限**（menu_type=BUTTON、visible=0），
--        不进顶部导航（AuthService.menus() 已过滤 BUTTON）、也不参与路由守卫，
--        故不会因「路由 meta.perm 取不到」而被前端守卫跳 404。
--   审计（D3）：落库动作另由 SystemAuditHelper 写 fac_audit_log（尽力而为）。
--   方言：仅 H2（与 V46 / V47 一致）；达梦 / PG 镜像待两库激活时补，见 R5。
-- =============================================================================

-- 1) 4 个按钮级写权限码
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

-- 2) 角色-菜单授权：ADMIN 全量
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code = 'ADMIN'
  AND m.code IN ('fm-emergency-command-write', 'fm-emergency-duty-write',
                 'fm-emergency-typhoon-dispatch', 'fm-fire-patrol-write')
  AND m.deleted = 0 AND m.status = 1;

-- 3) 指挥与调度岗：应急指令 / 台风调度（指挥链条）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('COMMANDER', 'SCHEDULER')
  AND m.code IN ('fm-emergency-command-write', 'fm-emergency-typhoon-dispatch')
  AND m.deleted = 0 AND m.status = 1;

-- 4) 值班调度 / 属地班长：值班签到
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('SCHEDULER', 'TEAM_LEADER')
  AND m.code = 'fm-emergency-duty-write'
  AND m.deleted = 0 AND m.status = 1;

-- 5) 属地班长 / 外操：巡更执行上报（现场执行岗）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('TEAM_LEADER', 'OUTER_OPER')
  AND m.code = 'fm-fire-patrol-write'
  AND m.deleted = 0 AND m.status = 1;
