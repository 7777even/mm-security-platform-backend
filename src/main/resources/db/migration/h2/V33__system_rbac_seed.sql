-- =============================================================================
-- V33 系统管理域 RBAC 种子（H2 方言）
--   1) 6 条角色种子（ADMIN 内置 + 五类岗位角色，对齐前端 rbac-permission spec）
--   2) 既有 5 条 fm-* 模块补权限码；新增 system 菜单权限子树
--   3) 角色-菜单授权：ADMIN 全量；其余角色保留既有 fm-* 导航可见性
--   说明：admin 用户 → ADMIN 角色的绑定由 RbacBootstrapService 幂等落库
--        （admin 用户是运行时代码种子，非 SQL 种子），故此处不涉及 sys_user。
--   权限码口径（重要）：业务域**沿用前端既有权限码**（不重命名），
--        与 src/router/menu.ts#MENU_ROUTE_SPECS、src/router/index.ts#SECONDARY_ROUTES
--        的 meta.perm 逐一对齐，否则前端路由守卫会因取不到权限码而全量跳 404。
-- =============================================================================

INSERT INTO sys_role (role_code, role_name, description, data_scope, status, built_in, sort_order) VALUES
  ('ADMIN',       '系统管理员', '内置超级管理员，拥有全部菜单与权限', 'ALL',  1, 1, 0),
  ('COMMANDER',   '总指挥',     '园区应急总指挥',                     'ALL',  1, 0, 10),
  ('SCHEDULER',   '值班调度',   '值班调度岗',                         'DEPT', 1, 0, 20),
  ('TEAM_LEADER', '属地班长',   '属地运行部班长',                     'DEPT', 1, 0, 30),
  ('INNER_OPER',  '内操',       '装置内操',                           'SELF', 1, 0, 40),
  ('OUTER_OPER',  '外操',       '装置外操',                           'SELF', 1, 0, 50);

-- 既有 5 条 fm-* 顶层模块补权限码（menu_type 已在 V32 置 DIR）
UPDATE sys_menu SET perm_code = 'dashboard:view'   WHERE code = 'fm-emergency';
UPDATE sys_menu SET perm_code = 'fire-alarm:view'  WHERE code = 'fm-fire';
UPDATE sys_menu SET perm_code = 'security:view'    WHERE code = 'fm-security';
UPDATE sys_menu SET perm_code = 'video:view'       WHERE code = 'fm-tv';
UPDATE sys_menu SET perm_code = 'ops:view'         WHERE code = 'fm-production';

-- fm-fire 下的按钮级权限（前端 v-permission 既有用法）
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '消防报警确认', 'fm-fire-ack', NULL, NULL, 110, 1, 0, 'BUTTON', 'fire-alarm:ack', 0
FROM sys_menu p WHERE p.code = 'fm-fire';

-- 移动端（不进顶部导航：menus() 仅返回 fm-* 前缀节点）
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT 0, '移动端', 'mobile', '/mobile', 'Cellphone', 800, 1, 0, 'DIR', NULL, 0;
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '现场采集回传', 'mobile-field-report', '/mobile/field-report', NULL, 801, 1, 0, 'MENU', 'mobile:field-report:view', 1
FROM sys_menu p WHERE p.code = 'mobile';

-- 系统管理：一级目录
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT 0, '系统管理', 'system', '/system', 'Setting', 900, 1, 0, 'DIR', NULL, 1;

-- 系统管理：二级菜单
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '用户管理',   'system-user',        '/system/users',       NULL, 901, 1, 0, 'MENU', 'system:user:view',        1 FROM sys_menu p WHERE p.code = 'system';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '角色管理',   'system-role',        '/system/roles',       NULL, 902, 1, 0, 'MENU', 'system:role:view',        1 FROM sys_menu p WHERE p.code = 'system';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '菜单权限',   'system-menu',        '/system/menus',       NULL, 903, 1, 0, 'MENU', 'system:menu:view',        1 FROM sys_menu p WHERE p.code = 'system';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '数据字典',   'system-dict',        '/system/dicts',       NULL, 904, 1, 0, 'MENU', 'system:dict:view',        1 FROM sys_menu p WHERE p.code = 'system';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '设备编码解析', 'system-device-code', '/system/device-code', NULL, 905, 1, 0, 'MENU', 'system:device-code:view', 1 FROM sys_menu p WHERE p.code = 'system';

-- 系统管理：按钮级权限（不参与导航，仅供角色授权与权限码下发）
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '新增用户',   'system-user-create',    NULL, NULL, 9101, 1, 0, 'BUTTON', 'system:user:create',     0 FROM sys_menu p WHERE p.code = 'system-user';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '编辑用户',   'system-user-edit',      NULL, NULL, 9102, 1, 0, 'BUTTON', 'system:user:edit',       0 FROM sys_menu p WHERE p.code = 'system-user';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '删除用户',   'system-user-delete',    NULL, NULL, 9103, 1, 0, 'BUTTON', 'system:user:delete',     0 FROM sys_menu p WHERE p.code = 'system-user';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '重置密码',   'system-user-reset',     NULL, NULL, 9104, 1, 0, 'BUTTON', 'system:user:reset-pwd',  0 FROM sys_menu p WHERE p.code = 'system-user';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '分配角色',   'system-user-assign',    NULL, NULL, 9105, 1, 0, 'BUTTON', 'system:user:assign-role', 0 FROM sys_menu p WHERE p.code = 'system-user';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '新增角色',   'system-role-create',    NULL, NULL, 9201, 1, 0, 'BUTTON', 'system:role:create',     0 FROM sys_menu p WHERE p.code = 'system-role';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '编辑角色',   'system-role-edit',      NULL, NULL, 9202, 1, 0, 'BUTTON', 'system:role:edit',       0 FROM sys_menu p WHERE p.code = 'system-role';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '删除角色',   'system-role-delete',    NULL, NULL, 9203, 1, 0, 'BUTTON', 'system:role:delete',     0 FROM sys_menu p WHERE p.code = 'system-role';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '角色授权',   'system-role-grant',     NULL, NULL, 9204, 1, 0, 'BUTTON', 'system:role:grant',      0 FROM sys_menu p WHERE p.code = 'system-role';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '新增菜单',   'system-menu-create',    NULL, NULL, 9301, 1, 0, 'BUTTON', 'system:menu:create',     0 FROM sys_menu p WHERE p.code = 'system-menu';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '编辑菜单',   'system-menu-edit',      NULL, NULL, 9302, 1, 0, 'BUTTON', 'system:menu:edit',       0 FROM sys_menu p WHERE p.code = 'system-menu';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '删除菜单',   'system-menu-delete',    NULL, NULL, 9303, 1, 0, 'BUTTON', 'system:menu:delete',     0 FROM sys_menu p WHERE p.code = 'system-menu';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '新增字典',   'system-dict-create',    NULL, NULL, 9401, 1, 0, 'BUTTON', 'system:dict:create',     0 FROM sys_menu p WHERE p.code = 'system-dict';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '编辑字典',   'system-dict-edit',      NULL, NULL, 9402, 1, 0, 'BUTTON', 'system:dict:edit',       0 FROM sys_menu p WHERE p.code = 'system-dict';
INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '删除字典',   'system-dict-delete',    NULL, NULL, 9403, 1, 0, 'BUTTON', 'system:dict:delete',     0 FROM sys_menu p WHERE p.code = 'system-dict';

-- 角色-菜单授权：ADMIN 拥有全部启用节点
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code = 'ADMIN' AND m.deleted = 0 AND m.status = 1;

-- 角色-菜单授权：其余角色保留既有 fm-* 顶层导航可见性（对齐迁移前 allowed_roles='ADMIN,USER' 的实际效果）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code LIKE 'fm-%' AND m.deleted = 0 AND m.status = 1;
