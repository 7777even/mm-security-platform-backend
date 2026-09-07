-- =============================================================================
-- V2 种子数据（dev 联调 / 生产初始化可复用）—— PostgreSQL 方言
--   用 ON CONFLICT DO NOTHING 保证幂等（重跑 V2 不会因主键/唯一约束报错）。
--   admin 账号由 AuthService.ensureAdmin() 写入，不在此落种子。
--   说明：未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。
-- =============================================================================

INSERT INTO sys_menu (id, parent_id, name, code, path, sort_order) VALUES
(1, 0, '首页', 'dashboard', '/dashboard', 1),
(2, 0, '设施管理', 'facility', '/facility', 2),
(3, 0, '告警中心', 'alarm', '/alarm', 3),
(4, 0, '设备台账', 'device', '/device', 4),
(5, 0, '应急指挥', 'emergency', '/emergency', 5)
ON CONFLICT (id) DO NOTHING;

INSERT INTO fac_device (device_code, device_name, device_type, zone, status, lat, lon) VALUES
('FAC2026FIREA00000001', '罐区A消防探头-F01', 'FIRE', '罐区A', 1, 21.5123, 110.4123),
('FAC2026FIREA00000002', '罐区A消防探头-F02', 'FIRE', '罐区A', 0, 21.5130, 110.4130),
('FAC2026GASA00000001',  '装置C气体检测仪-G01', 'GAS',  '装置C', 1, 21.5201, 110.4201),
('FAC2026GASB00000001',  '装卸区气体检测仪-G02', 'GAS',  '装卸区', 2, 21.5011, 110.4011),
('FAC2026FLOOD00000001', '危化仓库液位监测-L01', 'FLOOD', '危化仓库', 1, 21.5005, 110.4005),
('FAC2026CCTVA00000001', '罐区B监控-C01',       'CCTV', '罐区B', 1, 21.5150, 110.4150),
('FAC2026FIREB00000001', '装置C消防探头-F03',   'FIRE', '装置C', 0, 21.5210, 110.4210),
('FAC2026GASC00000001',  '罐区A气体检测仪-G03', 'GAS',  '罐区A', 1, 21.5118, 110.4118)
ON CONFLICT (device_code) DO NOTHING;

INSERT INTO fac_alarm (alarm_id, device_code, level, type, title, content, status, occurred_at, location, category, warned, plan_id) VALUES
('AE-2026-001', 'FAC2026FIREA00000001', 2, 'FIRE',  '罐区A消防探头-F01 温度越限', '感温探测器触发，温度超阈值',   0, '2026-09-01 08:12:00', '罐区A',    'FIRE_PHONE', false, NULL),
('AE-2026-002', 'FAC2026GASA00000001',  3, 'GAS',   '装置C气体检测仪-G01 泄漏',   '可燃气浓度超二级报警',        1, '2026-09-02 14:30:00', '装置C',    'OTHER',      false, NULL),
('AE-2026-003', NULL,                    1, 'TEMP',  '危化仓库液位监测-L01 高液位', '储罐液位达 95% 上限',         0, '2026-09-03 21:05:00', '危化仓库',  'OTHER',      false, NULL),
('AE-2026-004', 'FAC2026CCTVA00000001', 2, 'CCTV',  '罐区B监控-C01 画面丢失',      '视频流中断超过 60s',          2, '2026-09-04 09:48:00', '罐区B',    'OTHER',      false, NULL),
('AE-2026-005', 'FAC2026FIREB00000001', 4, 'FIRE',  '装置C消防探头-F03 误报',      '现场复核为误报',              3, '2026-09-05 11:20:00', '装置C',    'FIRE_PHONE', false, NULL),
('AE-2026-006', 'FAC2026GASB00000001',  2, 'GAS',   '装卸区气体检测仪-G02 报警',   '装卸臂接口疑似泄漏',          1, '2026-09-06 16:42:00', '装卸区',    'OTHER',      false, NULL),
('AE-2026-007', NULL,                    3, 'SOS',   '现场应急呼叫',                '巡检人员触发 SOS 按钮',       0, '2026-09-07 07:15:00', '全厂范围',  'WEATHER',    true,  NULL),
('AE-2026-008', NULL,                    2, 'TEMP',  '装置C温度异常',               '反应器温度梯度异常',          0, '2026-09-07 08:30:00', '装置C',    'OTHER',      false, NULL)
ON CONFLICT (alarm_id) DO NOTHING;

INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES
('WS-01', '中控室工位-01',   '罐区A',    TRUE),
('WS-02', '罐区值班室工位',  '罐区A',    TRUE),
('WS-03', '应急指挥中心工位', '全厂范围', FALSE)
ON CONFLICT (workstation_id) DO NOTHING;
