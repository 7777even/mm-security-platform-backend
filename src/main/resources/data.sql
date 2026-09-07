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

-- 告警种子数据：替代原 AlarmController 的 devFallbackList 模拟数据，字段已对齐前端 AlarmItem。
-- status 用后端 int（0=ACTIVE/1=ACKED/2=DISPATCHED/3=CLOSED），由 AlarmAssembler 映射为 string 枚举；
-- type 用前端枚举（FIRE/GAS/TEMP/CCTV/SOS）；alarm_id/location/category/warned/plan_id 均为新列。
INSERT INTO fac_alarm (alarm_id, device_code, level, type, title, content, status, occurred_at, location, category, warned, plan_id) VALUES
('AE-2026-001', 'FAC2026FIREA00000001', 2, 'FIRE',  '罐区A消防探头-F01 温度越限', '感温探测器触发，温度超阈值',   0, '2026-09-01 08:12:00', '罐区A',    'FIRE_PHONE', false, NULL),
('AE-2026-002', 'FAC2026GASA00000001',  3, 'GAS',   '装置C气体检测仪-G01 泄漏',   '可燃气浓度超二级报警',        1, '2026-09-02 14:30:00', '装置C',    'OTHER',      false, NULL),
('AE-2026-003', NULL,                    1, 'TEMP',  '危化仓库液位监测-L01 高液位', '储罐液位达 95% 上限',         0, '2026-09-03 21:05:00', '危化仓库',  'OTHER',      false, NULL),
('AE-2026-004', 'FAC2026CCTVA00000001', 2, 'CCTV',  '罐区B监控-C01 画面丢失',      '视频流中断超过 60s',          2, '2026-09-04 09:48:00', '罐区B',    'OTHER',      false, NULL),
('AE-2026-005', 'FAC2026FIREB00000001', 4, 'FIRE',  '装置C消防探头-F03 误报',      '现场复核为误报',              3, '2026-09-05 11:20:00', '装置C',    'FIRE_PHONE', false, NULL),
('AE-2026-006', 'FAC2026GASB00000001',  2, 'GAS',   '装卸区气体检测仪-G02 报警',   '装卸臂接口疑似泄漏',          1, '2026-09-06 16:42:00', '装卸区',    'OTHER',      false, NULL),
('AE-2026-007', NULL,                    3, 'SOS',   '现场应急呼叫',                '巡检人员触发 SOS 按钮',       0, '2026-09-07 07:15:00', '全厂范围',  'WEATHER',    true,  NULL),
('AE-2026-008', NULL,                    2, 'TEMP',  '装置C温度异常',               '反应器温度梯度异常',          0, '2026-09-07 08:30:00', '装置C',    'OTHER',      false, NULL);

