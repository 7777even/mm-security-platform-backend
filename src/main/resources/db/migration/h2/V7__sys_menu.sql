-- V7 顶部导航菜单收敛为 fm-* 主模块（与前端 MENU_ROUTE_SPECS 对齐），并引入 RBAC 角色列。
-- V1 已建 sys_menu（code/path/sort_order 等列），V2 曾种子旧码（dashboard/facility/alarm/device/emergency）；
-- 现统一为 fm-* 顶部主模块，旧种子由本迁移清理，改由本迁移维护，避免与前端路由 key 不一致。
-- 本迁移新增 allowed_roles 列承载 RBAC：menus() 按当前登录角色过滤可见菜单。
ALTER TABLE sys_menu ADD COLUMN allowed_roles VARCHAR(256) NOT NULL DEFAULT 'ADMIN,USER';

DELETE FROM sys_menu;

INSERT INTO sys_menu (parent_id, name, code, path, sort_order, allowed_roles) VALUES
 (0, '应急指挥', 'fm-emergency', '/emergency', 1, 'ADMIN,USER'),
 (0, '消防报警', 'fm-fire', '/fire', 2, 'ADMIN,USER'),
 (0, '治安防恐', 'fm-security', '/security', 3, 'ADMIN,USER'),
 (0, '工业电视', 'fm-tv', '/tv', 4, 'ADMIN,USER'),
 (0, '生产应急', 'fm-production', '/production', 5, 'ADMIN,USER');
