-- V107 通用管理台账写端点权限码（达梦 DM8 / Oracle 兼容）
-- 背景：MgmtLedgerController 的 createRow / updateRow / deleteRow 原统一用
--       @RequireAuth(role = "ADMIN") 临时授权。本迁移补上正式权限码 mgmt-ledger:write，
--       使写能力可按角色授权而非只对 ADMIN 开放（权限粒度）。
--
-- 父菜单：新增隐藏目录 fm-mgmt（后台管理台账），仅作 RBAC 授权载体，不进菜单树（visible=0）。
-- 按钮 mgmt-ledger:write 同样隐藏（visible=0）。
--
-- 授权角色：ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER（指挥与值班调度等管理岗）；
--           不含 INNER_OPER / OUTER_OPER（装置内外操），通用台账为后台管理控制台，不对一线操作员开放。
--           若后续需放开，到「角色管理」勾选 mgmt-ledger:write 即可，无需改代码。

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT 0, '后台管理台账', 'fm-mgmt', NULL, NULL, 850, 1, 0, 'DIR', NULL, 0 FROM dual;

INSERT INTO sys_menu (parent_id, name, code, path, icon, sort_order, status, deleted, menu_type, perm_code, visible)
SELECT p.id, '通用台账编辑', 'fm-mgmt-ledger-write', NULL, NULL, 851, 1, 0, 'BUTTON', 'mgmt-ledger:write', 0
FROM sys_menu p WHERE p.code = 'fm-mgmt';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.role_code IN ('ADMIN', 'COMMANDER', 'SCHEDULER', 'TEAM_LEADER')
  AND m.code = 'fm-mgmt-ledger-write';
