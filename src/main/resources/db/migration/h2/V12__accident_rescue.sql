-- V12 事故救援应急大屏真实数据源：事件聚合 + 详情字段 + 调度资源 + 值班人员 + 辅助统计 + 动态快讯。
-- 数据来源：src/screen/lib/data/accidentRescueMock.ts 与 fireEmergencyEventsStore 存量硬编码（event id 4 默认事件
-- 及其详情字段、调度资源、值班、辅助统计、四类动态快讯），迁移后数值语义保持不变，改为可维护的 DB 数据源。
-- 命名回避数据库保留字（value/status/type/time/label），统一加后缀，便于后期迁移达梦 DM8。
-- 地图标记与路线为前端按事件坐标推算的几何，不在此建表。

CREATE TABLE fac_accident_incident (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    location VARCHAR(128) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    hazard_source_level VARCHAR(32),
    map_status VARCHAR(32) NOT NULL,
    started_at VARCHAR(32),
    ended_at VARCHAR(32),
    status_name VARCHAR(16) NOT NULL,
    reported BOOLEAN NOT NULL DEFAULT FALSE,
    facility_name VARCHAR(128) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE fac_accident_detail_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    field_label VARCHAR(64) NOT NULL,
    field_value VARCHAR(512),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_accident_dispatch_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_code VARCHAR(32) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_name VARCHAR(128) NOT NULL,
    code VARCHAR(32) NOT NULL,
    organization VARCHAR(128),
    area VARCHAR(64),
    status_name VARCHAR(32) NOT NULL,
    distance_km DOUBLE,
    eta_minutes INT,
    capacity VARCHAR(128),
    contact VARCHAR(32),
    phone VARCHAR(32),
    longitude DOUBLE,
    latitude DOUBLE,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_accident_duty_person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    role VARCHAR(32),
    phone VARCHAR(32),
    avatar_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_accident_aux_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(64) NOT NULL,
    value_name INT NOT NULL DEFAULT 0,
    icon_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_accident_dynamic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(16) NOT NULL,
    title VARCHAR(64),
    tag VARCHAR(32),
    time VARCHAR(32),
    command_text VARCHAR(1024),
    responder VARCHAR(32),
    reply VARCHAR(1024),
    stage_label VARCHAR(64),
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (4, '乙烯裂解装置区火灾', '乙烯裂解装置区', 110.8781, 21.6812, '一级', '主力扑救', '2026-04-27 14:54:49', NULL, 'processing', TRUE, '乙烯裂解装置', TRUE);

INSERT INTO fac_accident_detail_field (incident_id, field_label, field_value, sort_no) VALUES
 (1, '事故时间', '2026-04-27 14:54:49', 1),
 (1, '事件分类', '突发应急事件', 2),
 (1, '事件小类', '事故灾难-工矿商贸等企业的安全事故', 3),
 (1, '事件描述', '模拟检修储运10/1-11/#区液氨储罐T-001泄漏引发火灾', 4),
 (1, '报警人', '张三', 5),
 (1, '报警电话', '111', 6),
 (1, '接警人', '李四', 7),
 (1, '接警时间', '2026-04-27 14:55:24', 8),
 (1, '涉及企业', '中海壳牌石油化工有限公司', 9),
 (1, '涉及装置', '液氨储罐ST1201', 10),
 (1, '涉及危险化学品', '液氨', 11),
 (1, '死亡人数', '0人', 12),
 (1, '重伤人数', '0人', 13),
 (1, '轻伤人数', '0人', 14),
 (1, '外部救援', '已启动预案', 15),
 (1, '已采取措施', '现场已启动疏散撤离，打开附近消防泡沫管道喷淋降温，同时确认无关人员已疏散', 16),
 (1, '附件信息', '—', 17);

INSERT INTO fac_accident_dispatch_resource (resource_code, resource_type, resource_name, code, organization, area, status_name, distance_km, eta_minutes, capacity, contact, phone, longitude, latitude, sort_no) VALUES
 ('team-01', '救援队伍', '炼油消防一中队', 'TEAM-RY-01', '消防救援中心', '炼油区', '可调度', 1.2, 6, '18人 · 泡沫灭火', '王钰', '18300556145', 110.8781, 21.6812, 1),
 ('team-02', '救援队伍', '炼油工艺处置组', 'TEAM-RY-03', '炼油分部', '炼油区', '可调度', 0.8, 4, '12人 · 切料堵漏', '高策', '18300556146', 110.8802, 21.6798, 2),
 ('vehicle-01', '应急车辆', '重型泡沫消防车', '粤K·XF119', '消防救援中心', '炼油区', '可调度', 1.5, 7, '泡沫液 6t · 水 12t', '陈伟', '18300556148', 110.8769, 21.6779, 3),
 ('vehicle-02', '应急车辆', '防化洗消车', '粤K·YJ026', '消防救援中心', '炼油区', '任务中', 2.4, 12, '洗消剂 2t · 6人', '李强', '18300556149', 110.8841, 21.6754, 4),
 ('material-01', '应急物资', '抗溶性水成膜泡沫液', 'MAT-PM-0031', '炼油应急物资库', '炼油区', '可调度', 0.9, 5, '库存 12t · 可用 10t', '赵敏', '18300556150', 110.8822, 21.6815, 5),
 ('material-02', '应急物资', '重型防化服', 'MAT-FH-0018', '炼油应急物资库', '炼油区', '可调度', 0.9, 5, '库存 36套 · 可用 28套', '赵敏', '18300556150', 110.8824, 21.6812, 6),
 ('expert-01', '应急专家', '周建国', 'EXP-HG-008', '茂名石化专家组', '炼油区', '可调度', 3.1, 15, '危化品泄漏与火灾处置', '周建国', '13802556008', 110.8738, 21.6833, 7),
 ('team-03', '救援队伍', '港区危化品抢险队', 'TEAM-GQ-02', '港区作业部', '港区', '离线', 18.6, 35, '15人 · 海上围控', '梁海', '13802556012', 110.9462, 21.5837, 8);

INSERT INTO fac_accident_duty_person (name, role, phone, avatar_index, sort_no) VALUES
 ('杨恒朋', '值班领导', '13792536966', 0, 1),
 ('高策', '值班员', '18300556145', 1, 2),
 ('高颖', '值班员', '18300556145', 2, 3),
 ('王磊', '值班员', '18300556145', 3, 4);

INSERT INTO fac_accident_aux_stat (label, value_name, icon_index, sort_no) VALUES
 ('应急专家', 47, 0, 1),
 ('应急物资', 3510, 1, 2),
 ('救援队伍', 10, 2, 3),
 ('装备车辆', 55, 3, 4),
 ('应急场所', 62, 4, 5),
 ('医疗机构', 80, 5, 6),
 ('应急车辆', 33, 6, 7),
 ('消防设施', 11, 7, 8);

INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no) VALUES
 ('rescue', '应急救援', '【固定指令】', '2026-04-03 12:15:45', '指令内容：请消防一队立即赶赴储罐区B-3，开展主力扑救并同步上报现场情况，注意保持安全距离。', '王钰', '【已回复】：已接收指令，车辆已出发，预计8分钟到达现场。', NULL, 1),
 ('rescue', '应急救援', '【固定指令】', '2026-04-03 12:08:20', '指令内容：请厂区西门警戒组引导救援车辆进入，确保通道畅通。', '赵敏', '【已回复】：西门通道已清理完毕，正在引导救援车辆通行。', NULL, 2),
 ('rescue', '应急救援', '【固定指令】', '2026-04-03 11:55:10', '指令内容：请医疗组在集结点设立临时救护站，做好伤员分类救治准备。', '李娜', '【已回复】：救护站已搭建，医疗物资到位，可接收伤员。', NULL, 3),
 ('rescue', '应急救援', '【固定指令】', '2026-04-03 11:40:25', '指令内容：请环保监测组对事故区域下风向开展连续大气监测并每15分钟上报。', '陈强', '【已回复】：监测点位已布设，数据实时回传中。', NULL, 4),
 ('rescue', '应急救援', '【固定指令】', '2026-04-03 11:28:50', '指令内容：请工艺处置组切断事故装置上下游物料，执行紧急停车。', '周斌', '【已回复】：上下游阀门已关闭，装置进入安全退守状态。', NULL, 5),
 ('rescue', '应急救援', '【固定指令】', '2026-04-03 11:15:05', '指令内容：请通信保障组建立现场应急通信专网，确保指挥链路畅通。', '吴磊', '【已回复】：应急专网已开通，各小组信道分配完毕。', NULL, 6),
 ('command', '应急疏散', '【固定指令】', '2026-04-27 14:58:12', '指令内容：请立即组织装置区无关人员按疏散路线撤离至安全区域。', '高策', '【已回复】：疏散路线已确认，人员正在有序撤离。', '1. 接警研判', 7),
 ('command', '应急前置处置', '【固定指令】', '2026-04-27 15:01:36', '指令内容：请消防一队开展冷却抑爆，工艺组同步切断物料来源。', '王钰', '【已回复】：冷却抑爆已开展，工艺切断正在执行。', '2. 一分钟应急响应', 8),
 ('command', '现场警戒', '【固定指令】', '2026-04-27 15:03:08', '指令内容：请警戒组封锁事故区域出入口，引导救援车辆通行。', '赵敏', '【已回复】：警戒点位已布设，通道清理中。', '3. 三分钟退守稳态', 9),
 ('brief', '现场简报', '【快讯】', '2026-04-27 15:00:00', '火势受控，暂无人员伤亡，周边装置运行平稳。', '应急指挥部', '【已发布】', NULL, 10),
 ('brief', '环境监测', '【快讯】', '2026-04-27 15:08:10', '事故区域下风向恶臭气体浓度达标，未检出剧毒组分。', '环境监测组', '【已发布】', NULL, 11),
 ('brief', '交通管制', '【快讯】', '2026-04-27 15:15:40', '厂区南门已实施交通管制，社会车辆请绕行。', '治安管理组', '【已发布】', NULL, 12),
 ('brief', '医疗救护', '【快讯】', '2026-04-27 15:22:05', '医疗救护车3辆已就位，开通绿色救治通道。', '医疗保障组', '【已发布】', NULL, 13),
 ('brief', '气象通报', '【快讯】', '2026-04-27 15:30:18', '当前东南风3级，能见度良好，利于烟气扩散。', '气象保障组', '【已发布】', NULL, 14),
 ('awareness', '态势感知', '【快报】', '2026-04-27 15:02:30', '监测数据显示事故区域温度下降，可燃气体浓度趋于稳定。', '监测中心', '【已同步】', NULL, 15),
 ('awareness', '视频监控', '【快报】', '2026-04-27 15:05:12', '主监控画面显示明火已熄灭，现场烟雾明显减少。', '视频监控中心', '【已同步】', NULL, 16),
 ('awareness', '人员定位', '【快报】', '2026-04-27 15:12:33', '现场作业人员已全部撤离至安全集合点，无失联。', '人员定位系统', '【已同步】', NULL, 17),
 ('awareness', '气体监测', '【快报】', '2026-04-27 15:19:50', '便携式检测仪显示O2浓度正常，CO浓度持续下降。', '气体检测组', '【已同步】', NULL, 18),
 ('awareness', '消防设施', '【快报】', '2026-04-27 15:27:21', '固定消防炮运行正常，管网压力维持在额定区间。', '消防设施组', '【已同步】', NULL, 19);
