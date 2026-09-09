-- V20 消防设施监测大屏真实数据源（fm-fire-facility）。
-- 数据来源：src/screen/lib/data/fireFacilityMonitoringMock.ts 存量硬编码（12 类设施监控卡片 +
-- 监控参数子表、12 条设施台账 + 维保记录子表、13 条故障工单 + 故障时间线子表、设施类型下拉），
-- 种子照抄原值，数值与文案语义保持不变。
-- 命名回避 H2 保留字（value/left/top/type/status/level/count/command/key）：
-- 监控参数值→value_text、设施类型→facility_type、故障等级→fault_level、故障状态→fault_status、
-- 卡片标识→key_code、是否启用→enabled_flag、原因→cause_text。
-- 报警（fireFacilityAlarms）与维修工单（fireFacilityWorkOrders）在 mock 中由 fireFacilityFaults
-- 派生，不在本迁移建表，由 FireFacilityService 按同一规则实时派生。

CREATE TABLE fac_fire_facility_monitor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_code VARCHAR(32) NOT NULL,
    facility_type VARCHAR(64) NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    online_count INT NOT NULL DEFAULT 0,
    offline_count INT NOT NULL DEFAULT 0,
    fault_count INT NOT NULL DEFAULT 0,
    monitor_status VARCHAR(16) NOT NULL,
    last_report_time VARCHAR(32) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_facility_param (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monitor_id BIGINT NOT NULL,
    label VARCHAR(64) NOT NULL,
    value_text VARCHAR(64) NOT NULL,
    tone VARCHAR(16) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_facility_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_code VARCHAR(32) NOT NULL,
    facility_name VARCHAR(128) NOT NULL,
    facility_type VARCHAR(64) NOT NULL,
    location_name VARCHAR(128) NOT NULL,
    device_name VARCHAR(64) NOT NULL,
    maintainer_name VARCHAR(128) NOT NULL,
    maintainer_phone VARCHAR(32) NOT NULL,
    enabled_flag BOOLEAN NOT NULL DEFAULT TRUE,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_facility_maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ledger_id BIGINT NOT NULL,
    record_date VARCHAR(32) NOT NULL,
    content_text VARCHAR(256) NOT NULL,
    report_file VARCHAR(256),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_facility_fault (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fault_code VARCHAR(64) NOT NULL,
    facility_code VARCHAR(32) NOT NULL,
    facility_name VARCHAR(128) NOT NULL,
    facility_type VARCHAR(64) NOT NULL,
    fault_type VARCHAR(32) NOT NULL,
    fault_level VARCHAR(16) NOT NULL,
    discover_time VARCHAR(32) NOT NULL,
    discover_method VARCHAR(32) NOT NULL,
    phenomenon VARCHAR(256) NOT NULL,
    cause_text VARCHAR(256) NOT NULL DEFAULT '',
    fault_status VARCHAR(16) NOT NULL,
    work_order_no VARCHAR(64),
    repair_person VARCHAR(64),
    estimated_finish VARCHAR(32),
    actual_finish VARCHAR(32),
    repair_measures VARCHAR(256),
    acceptance_person VARCHAR(64),
    acceptance_result VARCHAR(64),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_facility_fault_timeline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fault_id BIGINT NOT NULL,
    event_time VARCHAR(32) NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    action_name VARCHAR(64) NOT NULL,
    detail_text VARCHAR(256) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_facility_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kind VARCHAR(32) NOT NULL,
    option_label VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

-- 设施类型下拉（kind=FACILITY_TYPE，14 项：全部类型 + 12 类标准类型 + 维护保养记录）
INSERT INTO fac_fire_facility_option (kind, option_label, sort_no) VALUES
  ('FACILITY_TYPE', '全部类型', 1),
  ('FACILITY_TYPE', '火灾自动报警系统', 2),
  ('FACILITY_TYPE', '消防水源', 3),
  ('FACILITY_TYPE', '室外消火栓系统', 4),
  ('FACILITY_TYPE', '自动喷水灭火系统', 5),
  ('FACILITY_TYPE', '气体灭火系统', 6),
  ('FACILITY_TYPE', '泡沫灭火系统', 7),
  ('FACILITY_TYPE', '干粉灭火系统', 8),
  ('FACILITY_TYPE', '防烟排烟系统', 9),
  ('FACILITY_TYPE', '防火分隔设施', 10),
  ('FACILITY_TYPE', '消防应急广播', 11),
  ('FACILITY_TYPE', '应急照明及疏散指示系统', 12),
  ('FACILITY_TYPE', '消防电源', 13),
  ('FACILITY_TYPE', '维护保养记录', 14);

-- 监控卡片（12 类，sort_no 1..12；后续参数子表按 monitor_id 依次取 1..12）
INSERT INTO fac_fire_facility_monitor (key_code, facility_type, total_count, online_count,
    offline_count, fault_count, monitor_status, last_report_time, sort_no) VALUES
  ('fas', '火灾自动报警系统', 128, 124, 4, 2, '告警', '2026-08-20 10:23:15', 1),
  ('water', '消防水源', 46, 44, 1, 1, '告警', '2026-08-20 10:22:40', 2),
  ('hydrant', '室外消火栓系统', 86, 85, 1, 0, '正常', '2026-08-20 10:21:05', 3),
  ('sprinkler', '自动喷水灭火系统', 64, 62, 1, 1, '告警', '2026-08-20 10:20:12', 4),
  ('gas', '气体灭火系统', 38, 37, 0, 1, '告警', '2026-08-20 10:19:48', 5),
  ('foam', '泡沫灭火系统', 22, 21, 1, 0, '正常', '2026-08-20 10:18:30', 6),
  ('powder', '干粉灭火系统', 16, 16, 0, 0, '正常', '2026-08-20 10:17:22', 7),
  ('smoke', '防烟排烟系统', 52, 50, 1, 1, '告警', '2026-08-20 10:16:58', 8),
  ('partition', '防火分隔设施', 75, 74, 1, 0, '正常', '2026-08-20 10:15:44', 9),
  ('broadcast', '消防应急广播', 40, 39, 1, 0, '正常', '2026-08-20 10:14:20', 10),
  ('lighting', '应急照明及疏散指示系统', 320, 318, 2, 0, '正常', '2026-08-20 10:13:08', 11),
  ('power', '消防电源', 96, 93, 2, 1, '告警', '2026-08-20 10:12:36', 12);

-- 监控参数（29 条）
INSERT INTO fac_fire_facility_param (monitor_id, label, value_text, tone, sort_no) VALUES
  (1, '运行状态', '报警', 'danger', 1),
  (1, '通信状态', '正常', 'normal', 2),
  (1, '探测器在线', '124/128', 'normal', 3),
  (2, '水泵运行', '运行', 'normal', 1),
  (2, '水位', '32%', 'warning', 2),
  (2, '报警阈值', '<30%', 'normal', 3),
  (3, '报警状态', '正常', 'normal', 1),
  (3, '水压状态', '正常', 'normal', 2),
  (4, '阀门状态', '正常', 'normal', 1),
  (4, '最近动作', '2026-08-18 06:12', 'normal', 2),
  (4, '联动状态', '已联动', 'warning', 3),
  (5, '工作模式', '自动', 'normal', 1),
  (5, '阀驱动状态', '正常', 'normal', 2),
  (5, '管网压力', '异常', 'danger', 3),
  (6, '控制盘状态', '自动', 'normal', 1),
  (6, '电动阀状态', '关闭', 'normal', 2),
  (7, '工作模式', '自动', 'normal', 1),
  (7, '管网压力', '正常', 'normal', 2),
  (8, '风机运行', '故障', 'danger', 1),
  (8, '排烟阀状态', '正常', 'normal', 2),
  (9, '控制器状态', '正常', 'normal', 1),
  (9, '当前位态', '常闭', 'normal', 2),
  (10, '广播状态', '停止', 'normal', 1),
  (10, '分路状态', '正常', 'normal', 2),
  (11, '故障状态', '正常', 'normal', 1),
  (11, '应急模式', '未触发', 'normal', 2),
  (12, '工作状态', '欠压', 'warning', 1),
  (12, '备用电源', '正常', 'normal', 2),
  (12, 'UPS/EPS', '异常', 'danger', 3);

-- 设施台账（12 条，sort_no 1..12；后续维保记录子表按 ledger_id 取 1..12）
INSERT INTO fac_fire_facility_ledger (facility_code, facility_name, facility_type, location_name,
    device_name, maintainer_name, maintainer_phone, enabled_flag, sort_no) VALUES
  ('XF-001', '火灾自动报警系统-1#联合装置', '火灾自动报警系统', '炼油一部 1#联合装置', '1#联合装置',
   '茂名石化消防维保公司', '0668-2110001', TRUE, 1),
  ('XF-002', '消防水罐-1#', '消防水源', '消防泵房', '消防泵房',
   '茂名石化消防维保公司', '0668-2110002', TRUE, 2),
  ('XF-003', '室外消火栓系统-东厂区', '室外消火栓系统', '东厂区主干道', '东厂区',
   '茂名石化消防维保公司', '0668-2110003', TRUE, 3),
  ('XF-004', '自动喷水灭火系统-储运罐区', '自动喷水灭火系统', '储运部罐区', '储运部',
   '茂名石化消防维保公司', '0668-2110004', TRUE, 4),
  ('XF-005', '气体灭火系统-中央控制室', '气体灭火系统', '中央控制室', '中央控制室',
   '七氟丙烷维保单位', '0668-2110005', TRUE, 5),
  ('XF-006', '泡沫灭火系统-储罐区', '泡沫灭火系统', '储运部罐区', '储运部',
   '茂名石化消防维保公司', '0668-2110006', TRUE, 6),
  ('XF-007', '干粉灭火系统-装卸区', '干粉灭火系统', '装卸区', '装卸区',
   '茂名石化消防维保公司', '0668-2110007', TRUE, 7),
  ('XF-008', '防烟排烟系统-常减压装置', '防烟排烟系统', '常减压装置', '常减压装置',
   '茂名石化消防维保公司', '0668-2110008', TRUE, 8),
  ('XF-009', '防火分隔设施-全厂', '防火分隔设施', '各装置区', '全厂',
   '茂名石化消防维保公司', '0668-2110009', TRUE, 9),
  ('XF-010', '消防应急广播-全厂', '消防应急广播', '各装置区', '全厂',
   '茂名石化消防维保公司', '0668-2110010', TRUE, 10),
  ('XF-011', '应急照明及疏散指示系统-全厂', '应急照明及疏散指示系统', '各装置区', '全厂',
   '茂名石化消防维保公司', '0668-2110011', TRUE, 11),
  ('XF-012', '消防电源-消防泵房', '消防电源', '消防泵房/泡沫站', '消防泵房',
   '电气维保单位', '0668-2110012', TRUE, 12);

-- 维护保养记录（13 条）
INSERT INTO fac_fire_facility_maintenance (ledger_id, record_date, content_text, report_file, sort_no) VALUES
  (1, '2026-08-05', '季度维保：控制器巡检、探测器抽测，全部正常。', NULL, 1),
  (1, '2026-05-12', '半年检：联动测试、报警点位核对。', NULL, 2),
  (2, '2026-08-01', '月度维保：水位计校验、水泵盘车。', NULL, 1),
  (3, '2026-07-28', '月度维保：栓体、水压抽查。', NULL, 1),
  (4, '2026-07-20', '月度维保：报警阀组、末端试水。', NULL, 1),
  (5, '2026-07-15', '季度维保：钢瓶称重、管网气密性。', NULL, 1),
  (6, '2026-07-10', '季度维保：泡沫液比例、电动阀动作。', NULL, 1),
  (7, '2026-06-30', '季度维保：干粉罐压力、驱动装置。', NULL, 1),
  (8, '2026-06-22', '季度维保：风机、防火阀动作测试。', NULL, 1),
  (9, '2026-06-15', '季度维保：防火门、防火卷帘功能测试。', NULL, 1),
  (10, '2026-06-08', '月度维保：分区广播试音。', NULL, 1),
  (11, '2026-06-01', '季度维保：应急照明切换测试。', NULL, 1),
  (12, '2026-07-25', '月度维保：主备电切换测试、电池巡检。', NULL, 1);

-- 故障工单（13 条，sort_no 1..13；后续时间线子表按 fault_id 取 1..13）
INSERT INTO fac_fire_facility_fault (fault_code, facility_code, facility_name, facility_type,
    fault_type, fault_level, discover_time, discover_method, phenomenon, cause_text, fault_status,
    work_order_no, repair_person, estimated_finish, actual_finish, repair_measures,
    acceptance_person, acceptance_result, sort_no) VALUES
  ('FLT-20260820-001', 'XF-001', '火灾自动报警系统-1#联合装置', '火灾自动报警系统', '硬件故障', '紧急',
   '2026-08-20 10:23:15', '系统告警', '3#装置区感烟探测器报警', '', '待确认',
   NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1),
  ('FLT-20260820-002', 'XF-002', '消防水罐-1#', '消防水源', '通信故障', '重要',
   '2026-08-20 09:56:40', '系统告警', '消防水罐水位低于 30% 报警阈值', '', '待确认',
   NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2),
  ('FLT-20260819-003', 'XF-005', '气体灭火系统-中央控制室', '气体灭火系统', '软件故障', '重要',
   '2026-08-19 16:42:10', '系统告警', '管网压力数据异常', '压力变送器漂移', '已确认',
   '', NULL, NULL, NULL, NULL, NULL, NULL, 3),
  ('FLT-20260819-004', 'XF-008', '防烟排烟系统-常减压装置', '防烟排烟系统', '硬件故障', '紧急',
   '2026-08-19 08:30:05', '系统告警', '1#排烟风机故障停机', '风机电机过载', '已派单',
   'WO-20260819-001', '李维修', '2026-08-20 18:00:00', NULL, NULL, NULL, NULL, 4),
  ('FLT-20260818-005', 'XF-010', '消防应急广播-全厂', '消防应急广播', '通信故障', '一般',
   '2026-08-18 14:12:33', '维保发现', '广播分路 2 故障', '分路模块通讯中断', '已派单',
   'WO-20260818-002', '王维修', '2026-08-20 12:00:00', NULL, NULL, NULL, NULL, 5),
  ('FLT-20260818-006', 'XF-004', '自动喷水灭火系统-储运罐区', '自动喷水灭火系统', '老化', '重要',
   '2026-08-18 09:05:20', '人工巡检', '报警阀组渗漏', '密封圈老化', '维修中',
   'WO-20260818-003', '张维修', '2026-08-21 18:00:00', NULL, NULL, NULL, NULL, 6),
  ('FLT-20260817-007', 'XF-009', '防火分隔设施-全厂', '防火分隔设施', '硬件故障', '一般',
   '2026-08-17 11:26:48', '系统告警', '防火卷帘动作后未复位', '限位开关故障', '维修中',
   'WO-20260817-004', '赵维修', '2026-08-20 16:00:00', NULL, NULL, NULL, NULL, 7),
  ('FLT-20260816-008', 'XF-012', '消防电源-消防泵房', '消防电源', '电源故障', '重要',
   '2026-08-16 07:58:12', '系统告警', 'UPS/EPS 电池欠压', '电池组老化', '待验收',
   'WO-20260816-005', '李维修', '2026-08-18 18:00:00', '2026-08-18 16:40:00',
   '更换 UPS 电池组并完成充放电测试', NULL, NULL, 8),
  ('FLT-20260816-009', 'XF-001', '火灾自动报警系统-1#联合装置', '火灾自动报警系统', '硬件故障', '一般',
   '2026-08-16 06:22:35', '系统告警', '手动报警按钮 5# 线路故障', '线路老化', '待验收',
   'WO-20260816-006', '王维修', '2026-08-17 12:00:00', '2026-08-17 10:20:00',
   '更换 5# 按钮线路', NULL, NULL, 9),
  ('FLT-20260810-010', 'XF-003', '室外消火栓系统-东厂区', '室外消火栓系统', '硬件故障', '紧急',
   '2026-08-10 09:14:50', '系统告警', '室外消火栓水压无压', '管网阀门误关闭', '已闭环',
   'WO-20260810-007', '张维修', '2026-08-10 18:00:00', '2026-08-10 15:30:00',
   '开启管网阀门并恢复水压', '高策', '合格', 10),
  ('FLT-20260808-011', 'XF-011', '应急照明及疏散指示系统-全厂', '应急照明及疏散指示系统', '硬件故障', '一般',
   '2026-08-08 15:42:10', '人工巡检', '疏散指示标志故障', '灯珠损坏', '已闭环',
   'WO-20260808-008', '王维修', '2026-08-09 12:00:00', '2026-08-09 10:00:00',
   '更换疏散指示标志', '杨恒朋', '合格', 11),
  ('FLT-20260805-012', 'XF-006', '泡沫灭火系统-储罐区', '泡沫灭火系统', '软件故障', '重要',
   '2026-08-05 11:20:15', '系统告警', '泡沫电动阀动作反馈异常', '控制盘程序版本问题', '已闭环',
   'WO-20260805-009', '赵维修', '2026-08-06 18:00:00', '2026-08-06 16:20:00',
   '升级控制盘程序并复位', '高策', '合格', 12),
  ('FLT-20260819-010', 'XF-009', '防火分隔设施-全厂', '防火分隔设施', '人为损坏', '一般',
   '2026-08-19 09:40:00', '人工巡检', '常闭防火门未处于正常关闭状态', '门体被挡块撑开', '已派单',
   'WO-20260819-010', '王维修', '2026-08-21 12:00:00', NULL, NULL, NULL, NULL, 13);

-- 故障时间线（49 条）
INSERT INTO fac_fire_facility_fault_timeline (fault_id, event_time, operator_name, action_name,
    detail_text, sort_no) VALUES
  (1, '2026-08-20 10:23:15', '系统', '发现故障', 'FLT-20260820-001 3#装置区感烟探测器报警', 1),
  (2, '2026-08-20 09:56:40', '系统', '发现故障', 'FLT-20260820-002 消防水罐水位低于 30% 报警阈值', 1),
  (3, '2026-08-19 16:42:10', '系统', '发现故障', 'FLT-20260819-003 管网压力数据异常', 1),
  (3, '2026-08-19 16:50:22', '值班员-高策', '确认故障', '确认为重要故障，待派单', 2),
  (4, '2026-08-19 08:30:05', '系统', '发现故障', 'FLT-20260819-004 1#排烟风机故障停机', 1),
  (4, '2026-08-19 08:38:00', '值班员-高策', '确认故障', '确认为紧急故障', 2),
  (4, '2026-08-19 08:42:31', '值班员-高策', '生成工单并派发', '派发至 李维修（电气车间）', 3),
  (5, '2026-08-18 14:12:33', '系统', '发现故障', 'FLT-20260818-005 广播分路 2 故障', 1),
  (5, '2026-08-18 14:20:11', '值班员-杨恒朋', '确认故障', '确认故障并派单', 2),
  (5, '2026-08-18 14:22:46', '值班员-杨恒朋', '生成工单并派发', '派发至 王维修', 3),
  (6, '2026-08-18 09:05:20', '系统', '发现故障', 'FLT-20260818-006 报警阀组渗漏', 1),
  (6, '2026-08-18 09:12:44', '值班员-高策', '确认故障', '确认为重要故障', 2),
  (6, '2026-08-18 09:15:02', '值班员-高策', '生成工单并派发', '派发至 张维修', 3),
  (6, '2026-08-18 14:30:00', '张维修', '开始维修', '现场更换密封圈', 4),
  (7, '2026-08-17 11:26:48', '系统', '发现故障', 'FLT-20260817-007 防火卷帘动作后未复位', 1),
  (7, '2026-08-17 11:35:20', '值班员-杨恒朋', '确认故障', '确认故障', 2),
  (7, '2026-08-17 11:40:09', '值班员-杨恒朋', '生成工单并派发', '派发至 赵维修', 3),
  (7, '2026-08-17 15:10:00', '赵维修', '开始维修', '更换限位开关', 4),
  (8, '2026-08-16 07:58:12', '系统', '发现故障', 'FLT-20260816-008 UPS/EPS 电池欠压', 1),
  (8, '2026-08-16 08:05:40', '值班员-高策', '确认故障', '确认为重要故障', 2),
  (8, '2026-08-16 08:10:22', '值班员-高策', '生成工单并派发', '派发至 李维修', 3),
  (8, '2026-08-16 10:00:00', '李维修', '开始维修', '更换 UPS 电池组', 4),
  (8, '2026-08-18 16:40:00', '李维修', '提交验收', '更换 UPS 电池组并完成充放电测试', 5),
  (9, '2026-08-16 06:22:35', '系统', '发现故障', 'FLT-20260816-009 手动报警按钮 5# 线路故障', 1),
  (9, '2026-08-16 06:30:18', '值班员-杨恒朋', '确认故障', '确认故障', 2),
  (9, '2026-08-16 06:35:02', '值班员-杨恒朋', '生成工单并派发', '派发至 王维修', 3),
  (9, '2026-08-16 09:00:00', '王维修', '开始维修', '排查线路', 4),
  (9, '2026-08-17 10:20:00', '王维修', '提交验收', '更换 5# 按钮线路', 5),
  (10, '2026-08-10 09:14:50', '系统', '发现故障', 'FLT-20260810-010 室外消火栓水压无压', 1),
  (10, '2026-08-10 09:20:12', '值班员-高策', '确认故障', '确认为紧急故障', 2),
  (10, '2026-08-10 09:25:33', '值班员-高策', '生成工单并派发', '派发至 张维修', 3),
  (10, '2026-08-10 10:00:00', '张维修', '开始维修', '现场排查管网', 4),
  (10, '2026-08-10 15:30:00', '张维修', '提交验收', '开启管网阀门并恢复水压', 5),
  (10, '2026-08-10 16:10:22', '高策', '验收合格', '水压恢复正常', 6),
  (11, '2026-08-08 15:42:10', '系统', '发现故障', 'FLT-20260808-011 疏散指示标志故障', 1),
  (11, '2026-08-08 15:50:00', '值班员-杨恒朋', '确认故障', '确认故障', 2),
  (11, '2026-08-08 15:55:22', '值班员-杨恒朋', '生成工单并派发', '派发至 王维修', 3),
  (11, '2026-08-09 08:30:00', '王维修', '开始维修', '更换指示灯', 4),
  (11, '2026-08-09 10:00:00', '王维修', '提交验收', '更换疏散指示标志', 5),
  (11, '2026-08-09 10:30:45', '杨恒朋', '验收合格', '指示灯恢复正常', 6),
  (12, '2026-08-05 11:20:15', '系统', '发现故障', 'FLT-20260805-012 泡沫电动阀动作反馈异常', 1),
  (12, '2026-08-05 11:28:40', '值班员-高策', '确认故障', '确认为重要故障', 2),
  (12, '2026-08-05 11:32:11', '值班员-高策', '生成工单并派发', '派发至 赵维修', 3),
  (12, '2026-08-05 14:00:00', '赵维修', '开始维修', '升级控制盘程序', 4),
  (12, '2026-08-06 16:20:00', '赵维修', '提交验收', '升级控制盘程序并复位', 5),
  (12, '2026-08-06 17:05:30', '高策', '验收合格', '动作反馈恢复正常', 6),
  (13, '2026-08-19 09:40:00', '系统', '发现故障', 'FLT-20260819-010 常闭防火门未处于正常关闭状态', 1),
  (13, '2026-08-19 09:45:20', '值班员-杨恒朋', '确认故障', '确认故障并派单', 2),
  (13, '2026-08-19 09:48:12', '值班员-杨恒朋', '生成工单并派发', '派发至 王维修', 3);
