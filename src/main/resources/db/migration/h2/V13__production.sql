-- V13 生产应急监测大屏真实数据源（fm-production / fm-production-area）。
-- 数据来源：src/screen/lib/data/productionMock.ts、productionAreaMock.ts、productionDeviceMock.ts 存量硬编码
-- （设施与设备分类卡片、统计概览、生产报警、风险预警、人员定位标记、装置区指标与分区、设备清单），
-- 迁移后数值与文案语义保持不变，改为可维护的 DB 数据源。
-- 命名回避数据库保留字（value/left/top/type/status/level/count/command），统一加后缀，便于后期迁移达梦 DM8；
-- 其中 value 采用 value_text，实体属性同步命名为 valueText，避免 MyBatis-Plus 生成 `AS value` 触发 H2 语法错误。
-- 地图覆盖层（productionZoneOverlays）与地图控件（productionMapControls）为前端静态几何，不在此建表。

CREATE TABLE fac_production_facility (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    item_count INT NOT NULL DEFAULT 0,
    image VARCHAR(128),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_device_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    item_count INT NOT NULL DEFAULT 0,
    image VARCHAR(128),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(64) NOT NULL,
    value_text VARCHAR(64) NOT NULL,
    unit VARCHAR(32),
    trend DOUBLE NOT NULL DEFAULT 0,
    trend_up BOOLEAN NOT NULL DEFAULT FALSE,
    icon_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id BIGINT,
    title VARCHAR(64) NOT NULL,
    title_color VARCHAR(16) NOT NULL,
    location VARCHAR(128),
    occurred_at VARCHAR(32),
    description VARCHAR(512),
    status_name VARCHAR(32) NOT NULL,
    icon_index INT NOT NULL DEFAULT 0,
    thumb VARCHAR(128),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_risk_warning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(128) NOT NULL,
    type_name VARCHAR(64) NOT NULL,
    occurred_at VARCHAR(32),
    person VARCHAR(32),
    phone VARCHAR(32),
    level_code VARCHAR(16) NOT NULL,
    level_label VARCHAR(32) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_personnel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    left_ratio VARCHAR(16) NOT NULL,
    top_ratio VARCHAR(16) NOT NULL,
    longitude DOUBLE,
    latitude DOUBLE,
    location VARCHAR(128),
    person_count INT NOT NULL DEFAULT 0,
    marker_icon VARCHAR(128),
    popup_bg VARCHAR(32),
    marker_dot VARCHAR(32),
    marker_line VARCHAR(32),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    type_name VARCHAR(64) NOT NULL,
    area VARCHAR(64),
    status_name VARCHAR(16) NOT NULL,
    longitude DOUBLE,
    latitude DOUBLE,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_area_metric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id BIGINT NOT NULL,
    label VARCHAR(64) NOT NULL,
    value_text VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_production_area_zone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id BIGINT NOT NULL,
    zone_code VARCHAR(8) NOT NULL,
    name VARCHAR(64) NOT NULL,
    alarm_count INT NOT NULL DEFAULT 0,
    zone_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

-- 设施总览卡片：与 productionMock.facilityItems 一致（image 为前端 production 模块静态资源名）
INSERT INTO fac_production_facility (name, item_count, image, sort_no) VALUES
 ('厂区', 596, 'image_0001.png', 1),
 ('生产装置', 596, 'image_0008.png', 2),
 ('仓库', 596, 'image_0006.png', 3),
 ('重大危险源', 596, 'image_0012.png', 4),
 ('储罐', 596, 'image_0007.png', 5);

-- 设备分类总览卡片：与 productionMock.deviceItems 一致（名称同时作为 fac_production_device.category 外键语义）
INSERT INTO fac_production_device_category (name, item_count, image, sort_no) VALUES
 ('卡口/通道', 596, 'image_0002.png', 1),
 ('监测点', 596, 'image_0009.png', 2),
 ('人员定位', 596, 'image_0003.png', 3),
 ('消防设施', 596, 'image_0010.png', 4),
 ('通风设备', 596, 'image_0004.png', 5),
 ('广播', 596, 'image_0011.png', 6),
 ('电话', 596, 'image_0005.png', 7);

-- 统计概览条：与 productionMock.statOverview 一致（valueSuffix 对应契约 unit，无单位的告警计数统一为「起」）
INSERT INTO fac_production_stat (label, value_text, unit, trend, trend_up, icon_index, sort_no) VALUES
 ('报警总数', '36', '起', 12, FALSE, 0, 1),
 ('未处置告警', '12', '起', 8, TRUE, 1, 2),
 ('已处置告警', '24', '起', 5, FALSE, 2, 3),
 ('处置中告警', '6', '起', 3, TRUE, 3, 4),
 ('平均处置时长', '18', '分32秒', 10, FALSE, 4, 5);

-- 生产报警：取自 productionMock.productionAlarms（前 14 条为 mock 原文，后 6 条为同类补充）。
-- facility_id 按 5 个设施均分，保证每个装置区二级页都有报警可展示；不传 facilityId 时返回全部。
INSERT INTO fac_production_alarm (facility_id, title, title_color, location, occurred_at, description, status_name, icon_index, thumb, sort_no) VALUES
 (1, '人员跌倒', 'warning', '化工区乙烯装置东侧', '2026-03-17 14:21:30', 'A装置区域发现人员跌倒。', '未处置', 0, 'person_fall.png', 1),
 (1, '人员违规进入', 'danger', '水东港区第一作业区', '2026-03-17 14:18:05', 'A装置区域发现非注册人员，请核实。', '未处置', 1, 'unauthorized_entry.png', 2),
 (1, '人员聚集', 'orange', '炼油区催化装置北侧', '2026-03-17 14:12:47', 'A装置区域5至30人，超过20人上限50%。', '处置中', 2, 'person_gathering.png', 3),
 (1, '有毒气体超标', 'purple', '化工区罐区南侧', '2026-03-17 13:58:12', 'A装置区有毒气体超标200%，请关注。', '未处置', 3, 'gas_leak.png', 4),
 (2, '人员滞留', 'warning', '炼油区加氢装置二层', '2026-03-17 13:45:09', 'B装置区域同一人员停留超30分钟。', '未处置', 0, 'person_fall.png', 5),
 (2, '非注册人员闯入', 'danger', '化工区苯乙烯罐组', '2026-03-17 13:30:51', '周界报警触发，请核实人员身份。', '已处置', 1, 'unauthorized_entry.png', 6),
 (2, '区域超员', 'orange', '水东港区码头平台', '2026-03-17 13:22:33', '实时人数28人，超出核定20人。', '未处置', 2, 'person_gathering.png', 7),
 (2, '可燃气体报警', 'purple', '炼油区重整装置区', '2026-03-17 12:58:40', '可燃气探测器浓度达LEL 35%。', '未处置', 3, 'gas_leak.png', 8),
 (3, '人员越界', 'warning', '化工区管廊下方', '2026-03-17 12:40:18', '人员进入受限空间警戒区。', '处置中', 0, 'person_fall.png', 9),
 (3, '周界入侵', 'danger', '港区危化品仓库周界', '2026-03-17 12:15:02', '西侧周界触发入侵报警。', '未处置', 1, 'unauthorized_entry.png', 10),
 (3, '人员聚集', 'orange', '炼油区硫磺回收装置', '2026-03-17 11:50:36', '区域瞬时人数22人，接近上限。', '已处置', 2, 'person_gathering.png', 11),
 (3, '有毒气体超标', 'purple', '化工区EOEG装置', '2026-03-17 11:28:54', 'H2S浓度超标120%，已联动通风。', '未处置', 3, 'gas_leak.png', 12),
 (4, '人员跌倒', 'warning', '水东港区罐区巡检通道', '2026-03-17 10:55:21', '巡检人员疑似跌倒，画面需复核。', '未处置', 0, 'person_fall.png', 13),
 (4, '非注册车辆进入', 'danger', '炼油区东门卡口', '2026-03-17 10:30:44', '无权限车辆驶入生产区。', '处置中', 1, 'unauthorized_entry.png', 14),
 (4, '有毒气体超标', 'purple', '储运区液化烃罐组', '2026-03-17 10:12:37', '罐区H2S浓度超标80%，已启动应急通风。', '未处置', 3, 'gas_leak.png', 15),
 (4, '人员违规进入', 'danger', '公用工程区污水提升泵站', '2026-03-17 09:48:20', '受限空间作业票缺失，人员违规进入。', '处置中', 1, 'unauthorized_entry.png', 16),
 (5, '人员聚集', 'orange', '乙烯区裂解炉北侧', '2026-03-17 09:20:11', '检修人员集中17人，接近上限20人。', '未处置', 2, 'person_gathering.png', 17),
 (5, '人员滞留', 'warning', '化工区芳烃抽提装置三层', '2026-03-17 08:55:03', '同一人员停留超45分钟，请复核。', '已处置', 0, 'person_fall.png', 18),
 (5, '火焰检测报警', 'danger', '炼油区常减压装置加热炉', '2026-03-17 08:31:47', '火焰检测器检出明火信号，请立即核实。', '未处置', 1, 'gas_leak.png', 19),
 (5, '区域超员', 'orange', '储罐区T-203罐组围堰内', '2026-03-17 08:05:29', '围堰内实时人数19人，超出核定15人。', '未处置', 2, 'person_gathering.png', 20);

-- 风险预警：取自 productionMock.riskWarnings；level_code 为红/橙/黄三级，总览的 riskSummary 由本表聚合得出
INSERT INTO fac_production_risk_warning (location, type_name, occurred_at, person, phone, level_code, level_label, sort_no) VALUES
 ('乙烯装置区（二）', '高温预警', '2026-03-17 02:00:46', '王立军', '1380255****', 'red', '红色', 1),
 ('重整装置区', '压力异常', '2026-03-17 03:12:18', '李建国', '1390288****', 'red', '红色', 2),
 ('罐区（三）', '泄漏预警', '2026-03-17 04:25:09', '陈志强', '1370900****', 'orange', '橙色', 3),
 ('催化装置区', '温度偏高', '2026-03-17 05:40:33', '赵伟', '1350666****', 'orange', '橙色', 4),
 ('水东港区作业区', '风速超限', '2026-03-17 06:18:55', '黄国强', '1360200****', 'orange', '橙色', 5),
 ('苯乙烯装置区', '液位预警', '2026-03-17 07:02:11', '周敏', '1880255****', 'yellow', '黄色', 6),
 ('港区储罐区', '温度预警', '2026-03-17 07:45:02', '吴涛', '1890288****', 'yellow', '黄色', 7);

-- 人员定位标记：left/top 为舞台百分比（版面定位），经纬度由 map.pgw + 设计稿贴图区换算得到的 WGS84 坐标。
-- 契约中 popup_bg/marker_dot/marker_line 为颜色串（前端按契约着色），故不沿用 mock 的图片资源名。
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES
 ('54.2%', '25.3%', 110.8836, 21.6838, '炼化厂区丙侧', 365, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 1),
 ('33.8%', '28.1%', 110.8753, 21.6831, '炼化厂区丙侧', 365, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 2),
 ('46.5%', '46.4%', 110.8805, 21.6787, '炼化厂区丙侧', 365, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 3);

-- 设备清单：7 个分类 × 5 台 = 35 台，名称与类型沿用 productionDeviceMock 的命名规则（前缀 + 序号#），
-- 状态按 正常/正常/正常/离线/故障 循环，区域按 炼油区/化工区/储运区/公用工程区/乙烯区 循环。
INSERT INTO fac_production_device (category, name, type_name, area, status_name, longitude, latitude, sort_no) VALUES
 ('卡口/通道', '东门卡口1#', '门禁闸机', '炼油区', '正常', 110.8770, 21.6827, 1),
 ('卡口/通道', '西门卡口2#', '车辆通道', '化工区', '正常', 110.8787, 21.6818, 2),
 ('卡口/通道', '南门通道3#', '人行通道', '储运区', '正常', 110.8804, 21.6833, 3),
 ('卡口/通道', '北门通道4#', '门禁闸机', '公用工程区', '离线', 110.8821, 21.6814, 4),
 ('卡口/通道', '罐区通道5#', '车辆通道', '乙烯区', '故障', 110.8838, 21.6824, 5),
 ('监测点', '催化裂解监测1#', '气体监测', '炼油区', '正常', 110.8855, 21.6805, 6),
 ('监测点', '乙烯装置监测2#', '压力监测', '化工区', '正常', 110.8782, 21.6795, 7),
 ('监测点', '罐区监测3#', '温度监测', '储运区', '正常', 110.8816, 21.6786, 8),
 ('监测点', '管廊监测4#', '气体监测', '公用工程区', '离线', 110.8849, 21.6791, 9),
 ('监测点', '催化裂解监测5#', '压力监测', '乙烯区', '故障', 110.8800, 21.6777, 10),
 ('人员定位', '炼油区定位1#', '定位基站', '炼油区', '正常', 110.8834, 21.6773, 11),
 ('人员定位', '化工区定位2#', '定位标签网关', '化工区', '正常', 110.8867, 21.6783, 12),
 ('人员定位', '罐区定位3#', '定位基站', '储运区', '正常', 110.8769, 21.6812, 13),
 ('人员定位', '炼油区定位4#', '定位标签网关', '公用工程区', '离线', 110.8876, 21.6822, 14),
 ('人员定位', '化工区定位5#', '定位基站', '乙烯区', '故障', 110.8808, 21.6803, 15),
 ('消防设施', '催化裂解装置自动喷淋1#', '自动喷水设备', '炼油区', '正常', 110.8779, 21.6832, 16),
 ('消防设施', '罐区泡沫炮2#', '泡沫灭火设备', '化工区', '正常', 110.8796, 21.6823, 17),
 ('消防设施', '装置气体灭火3#', '气体灭火设备', '储运区', '正常', 110.8813, 21.6838, 18),
 ('消防设施', '催化裂解装置自动喷淋4#', '自动喷水设备', '公用工程区', '离线', 110.8830, 21.6819, 19),
 ('消防设施', '罐区泡沫炮5#', '泡沫灭火设备', '乙烯区', '故障', 110.8847, 21.6829, 20),
 ('通风设备', '泵房通风1#', '自动通风设备', '炼油区', '正常', 110.8864, 21.6810, 21),
 ('通风设备', '配电室通风2#', '强制排风设备', '化工区', '正常', 110.8791, 21.6801, 22),
 ('通风设备', '装置排风3#', '自动通风设备', '储运区', '正常', 110.8825, 21.6792, 23),
 ('通风设备', '泵房通风4#', '强制排风设备', '公用工程区', '离线', 110.8858, 21.6797, 24),
 ('通风设备', '配电室通风5#', '自动通风设备', '乙烯区', '故障', 110.8809, 21.6783, 25),
 ('广播', '厂区广播点1#', '厂区广播', '炼油区', '正常', 110.8843, 21.6778, 26),
 ('广播', '装置广播2#', '应急广播', '化工区', '正常', 110.8876, 21.6788, 27),
 ('广播', '罐区广播3#', '厂区广播', '储运区', '正常', 110.8778, 21.6817, 28),
 ('广播', '厂区广播点4#', '应急广播', '公用工程区', '离线', 110.8885, 21.6827, 29),
 ('广播', '装置广播5#', '厂区广播', '乙烯区', '故障', 110.8817, 21.6809, 30),
 ('电话', '值班电话1#', '固定电话', '炼油区', '正常', 110.8788, 21.6838, 31),
 ('电话', '应急电话2#', '应急电话', '化工区', '正常', 110.8805, 21.6829, 32),
 ('电话', '装置电话3#', '固定电话', '储运区', '正常', 110.8822, 21.6843, 33),
 ('电话', '值班电话4#', '应急电话', '公用工程区', '离线', 110.8839, 21.6824, 34),
 ('电话', '应急电话5#', '固定电话', '乙烯区', '故障', 110.8856, 21.6834, 35);

-- 装置区指标卡：13 个标签 × 5 个设施；取值沿用 productionAreaMock.buildMetrics 的
-- 520 + ((facilityId * 17 + index * 13) % 90) 规则，保证同一设施多次打开数值稳定。
INSERT INTO fac_production_area_metric (facility_id, label, value_text, sort_no) VALUES
 (1, '重大危险源', '537', 1), (1, '生产装置', '550', 2), (1, '门禁闸机', '563', 3), (1, '视频监控', '576', 4),
 (1, '厂播', '589', 5), (1, '电话', '602', 6), (1, '仓库', '525', 7), (1, '储罐', '538', 8),
 (1, '监测点位', '551', 9), (1, '通风设备', '564', 10), (1, '灭火设施', '577', 11), (1, '无线通讯', '590', 12), (1, '可燃气体检测仪', '603', 13),
 (2, '重大危险源', '554', 1), (2, '生产装置', '567', 2), (2, '门禁闸机', '580', 3), (2, '视频监控', '593', 4),
 (2, '厂播', '606', 5), (2, '电话', '529', 6), (2, '仓库', '542', 7), (2, '储罐', '555', 8),
 (2, '监测点位', '568', 9), (2, '通风设备', '581', 10), (2, '灭火设施', '594', 11), (2, '无线通讯', '607', 12), (2, '可燃气体检测仪', '530', 13),
 (3, '重大危险源', '571', 1), (3, '生产装置', '584', 2), (3, '门禁闸机', '597', 3), (3, '视频监控', '520', 4),
 (3, '厂播', '533', 5), (3, '电话', '546', 6), (3, '仓库', '559', 7), (3, '储罐', '572', 8),
 (3, '监测点位', '585', 9), (3, '通风设备', '598', 10), (3, '灭火设施', '521', 11), (3, '无线通讯', '534', 12), (3, '可燃气体检测仪', '547', 13),
 (4, '重大危险源', '588', 1), (4, '生产装置', '601', 2), (4, '门禁闸机', '524', 3), (4, '视频监控', '537', 4),
 (4, '厂播', '550', 5), (4, '电话', '563', 6), (4, '仓库', '576', 7), (4, '储罐', '589', 8),
 (4, '监测点位', '602', 9), (4, '通风设备', '525', 10), (4, '灭火设施', '538', 11), (4, '无线通讯', '551', 12), (4, '可燃气体检测仪', '564', 13),
 (5, '重大危险源', '605', 1), (5, '生产装置', '528', 2), (5, '门禁闸机', '541', 3), (5, '视频监控', '554', 4),
 (5, '厂播', '567', 5), (5, '电话', '580', 6), (5, '仓库', '593', 7), (5, '储罐', '606', 8),
 (5, '监测点位', '529', 9), (5, '通风设备', '542', 10), (5, '灭火设施', '555', 11), (5, '无线通讯', '568', 12), (5, '可燃气体检测仪', '581', 13);

-- 装置区分区：沿用 productionAreaMock.buildZones，名称 = 分区字母 + 设施名，报警数 A/B/C 依次为 2/1/0
INSERT INTO fac_production_area_zone (facility_id, zone_code, name, alarm_count, zone_index, sort_no) VALUES
 (1, 'a', 'A厂区', 2, 0, 1), (1, 'b', 'B厂区', 1, 1, 2), (1, 'c', 'C厂区', 0, 2, 3),
 (2, 'a', 'A生产装置', 2, 0, 1), (2, 'b', 'B生产装置', 1, 1, 2), (2, 'c', 'C生产装置', 0, 2, 3),
 (3, 'a', 'A仓库', 2, 0, 1), (3, 'b', 'B仓库', 1, 1, 2), (3, 'c', 'C仓库', 0, 2, 3),
 (4, 'a', 'A重大危险源', 2, 0, 1), (4, 'b', 'B重大危险源', 1, 1, 2), (4, 'c', 'C重大危险源', 0, 2, 3),
 (5, 'a', 'A储罐', 2, 0, 1), (5, 'b', 'B储罐', 1, 1, 2), (5, 'c', 'C储罐', 0, 2, 3);
