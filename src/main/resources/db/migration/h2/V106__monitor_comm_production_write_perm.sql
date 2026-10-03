-- V106 监测 / 通信 / 生产七域写端点权限码（H2）
-- 背景：第 1/2/3 批为该七域补了 POST/PUT/DELETE 写端点，当时统一用 @RequireAuth(role = "ADMIN")
-- 临时授权。本迁移补上正式权限码，使写能力可按角色授权而非只对 ADMIN 开放。
--
-- 父菜单归属：本批 7 个按钮均为 visible=0 的隐藏按钮（仅作 RBAC 授权载体，不进菜单树）。
-- sys_menu 现有一级目录为 fm-emergency / fm-fire / fm-production / fm-security / fm-tv，
-- 尚无「设备管理」「通讯通知管理」独立节点；为避免为隐藏按钮新建可见菜单节点，
-- 统一挂到既有 fm-production 目录。若后续需要精确菜单树归属，另行补节点后迁移 parent_id。

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '摄像头台账', 'fm-video-camera-write', NULL, NULL, 137, 1, 0, 'BUTTON', 'video:camera-write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '通讯设备台账', 'fm-communication-device-write', NULL, NULL, 138, 1, 0, 'BUTTON', 'communication:device-write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '通讯通知记录', 'fm-communication-record-write', NULL, NULL, 139, 1, 0, 'BUTTON', 'communication:record-write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '设备台账', 'fm-device-write', NULL, NULL, 140, 1, 0, 'BUTTON', 'device:write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '重大危险源台账', 'fm-hazard-write', NULL, NULL, 141, 1, 0, 'BUTTON', 'hazard:write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '监测点位台账', 'fm-hazard-point-write', NULL, NULL, 142, 1, 0, 'BUTTON', 'hazard:point-write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '特殊作业票', 'fm-special-operation-write', NULL, NULL, 143, 1, 0, 'BUTTON', 'special-operation:write', 0
FROM sys_menu p WHERE p.code = 'fm-production';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER', 'INNER_OPER', 'OUTER_OPER')
  AND m.code IN (
    'fm-video-camera-write',
    'fm-communication-device-write',
    'fm-communication-record-write',
    'fm-device-write',
    'fm-hazard-write',
    'fm-hazard-point-write',
    'fm-special-operation-write'
  );
