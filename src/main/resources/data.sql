-- dev 初始数据：仅菜单；默认 admin 账号由 AuthService.ensureAdmin() 写入（密码 admin@2026，BCrypt 同步校验）
MERGE INTO sys_menu(id, parent_id, name, code, path, sort_order) KEY(id) VALUES
(1, 0, '首页', 'dashboard', '/dashboard', 1),
(2, 0, '设施管理', 'facility', '/facility', 2),
(3, 0, '告警中心', 'alarm', '/alarm', 3),
(4, 0, '设备台账', 'device', '/device', 4),
(5, 0, '应急指挥', 'emergency', '/emergency', 5);
