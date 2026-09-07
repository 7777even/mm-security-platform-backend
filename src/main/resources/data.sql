-- dev 初始数据：仅菜单；默认 admin 账号由 AuthService.ensureAdmin() 写入（密码 admin@2026，BCrypt 同步校验）
MERGE INTO sys_menu(id, parent_id, name, code, path, sort_order) KEY(id) VALUES
(1, 0, '首页', 'dashboard', '/dashboard', 1),
(2, 0, '设施管理', 'facility', '/facility', 2),
(3, 0, '告警中心', 'alarm', '/alarm', 3),
(4, 0, '设备台账', 'device', '/device', 4),
(5, 0, '应急指挥', 'emergency', '/emergency', 5);

-- 设备种子数据：替代原 DeviceController 的 devFallbackList 模拟数据，使空库联调也能看到真实列表。
-- device_code 为 20 位 MDM 物理主键（与 byCode/page 的 20 位校验一致）。
INSERT INTO fac_device (device_code, device_name, device_type, zone, status, lat, lon) VALUES
('FAC2026FIREA00000001', '罐区A消防探头-F01', 'FIRE', '罐区A', 1, 21.5123, 110.4123),
('FAC2026FIREA00000002', '罐区A消防探头-F02', 'FIRE', '罐区A', 0, 21.5130, 110.4130),
('FAC2026GASA00000001',  '装置C气体检测仪-G01', 'GAS',  '装置C', 1, 21.5201, 110.4201),
('FAC2026GASB00000001',  '装卸区气体检测仪-G02', 'GAS',  '装卸区', 2, 21.5011, 110.4011),
('FAC2026FLOOD00000001', '危化仓库液位监测-L01', 'FLOOD', '危化仓库', 1, 21.5005, 110.4005),
('FAC2026CCTVA00000001', '罐区B监控-C01',       'CCTV', '罐区B', 1, 21.5150, 110.4150),
('FAC2026FIREB00000001', '装置C消防探头-F03',   'FIRE', '装置C', 0, 21.5210, 110.4210),
('FAC2026GASC00000001',  '罐区A气体检测仪-G03', 'GAS',  '罐区A', 1, 21.5118, 110.4118);
