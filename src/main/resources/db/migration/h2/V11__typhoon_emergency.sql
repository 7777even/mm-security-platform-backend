-- V11 台风应急大屏真实数据源：台风应急事件聚合 + 防汛排涝力量清单。
-- 数据来源：src/screen/lib/data/typhoonEmergencyMock.ts 存量硬编码（defaultTyphoonIncident +
-- typhoonDispatchResources），迁移后数值语义保持不变，改为可维护的 DB 数据源。
-- 命名回避数据库保留字（value/status/type/time/label），统一加后缀，便于后期迁移达梦 DM8。
-- 序列型数据（降雨 / 风速 / 水位）统一存入 fac_typhoon_series，以 series_key 区分、sort_no 定序。

CREATE TABLE fac_typhoon_incident (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    location VARCHAR(128) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    started_at VARCHAR(32) NOT NULL,
    ended_at VARCHAR(32),
    status_name VARCHAR(32) NOT NULL,
    meteorology_summary VARCHAR(1024),
    water_level_warn DOUBLE,
    water_level_danger DOUBLE,
    typhoon_api_code VARCHAR(16),
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE fac_typhoon_monitor_object (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    obj_code VARCHAR(32) NOT NULL,
    obj_name VARCHAR(64) NOT NULL,
    obj_value VARCHAR(32) NOT NULL,
    unit VARCHAR(16),
    status_name VARCHAR(16) NOT NULL,
    status_text VARCHAR(64),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_risk_warning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    warn_code VARCHAR(32) NOT NULL,
    warn_time VARCHAR(16) NOT NULL,
    warn_type VARCHAR(32) NOT NULL,
    content VARCHAR(512) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_live_video (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    video_code VARCHAR(32) NOT NULL,
    video_label VARCHAR(128) NOT NULL,
    scene_index INT NOT NULL DEFAULT 0,
    angle VARCHAR(64),
    status_name VARCHAR(16) NOT NULL,
    device_code VARCHAR(32),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_map_risk_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    point_code VARCHAR(32) NOT NULL,
    point_name VARCHAR(128) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    status_name VARCHAR(16) NOT NULL,
    status_text VARCHAR(64),
    responsible_unit VARCHAR(64),
    predeployed BOOLEAN NOT NULL DEFAULT FALSE,
    deployment VARCHAR(256),
    label_offset_x INT,
    label_offset_y INT,
    cluster_count INT,
    kind VARCHAR(16),
    video_ids VARCHAR(255),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_series (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    series_key VARCHAR(32) NOT NULL,
    point_label VARCHAR(32),
    point_value DOUBLE NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_event_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    field_label VARCHAR(64) NOT NULL,
    field_value VARCHAR(512),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_aux_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    line1 VARCHAR(64) NOT NULL,
    line2 VARCHAR(64),
    item_count INT NOT NULL DEFAULT 0,
    count_tone VARCHAR(16),
    icon_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_dispatch_resource (
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

INSERT INTO fac_typhoon_incident (event_id, title, location, longitude, latitude, started_at, ended_at, status_name, meteorology_summary, water_level_warn, water_level_danger, typhoon_api_code, is_default) VALUES
 (100, '台风沙迦防台防汛工作', '全厂范围', 110.92, 21.67, '2026-06-25T08:12:00', NULL, 'processing', '受台风"沙迦"外围云系影响，厂区将出现中到大雨，局部暴雨，伴有6-8级阵风。请各单位加强低洼区域巡查，确保排涝设施正常运行。', 0.6, 0.8, '202518', TRUE);
INSERT INTO fac_typhoon_monitor_object (incident_id, obj_code, obj_name, obj_value, unit, status_name, status_text, sort_no) VALUES
 (1, 'outlet', '总排口', '0.8', 'm', 'normal', '水位正常', 1),
 (1, 'pool-a', '6#路地磅北地沟', '0.3', 'm', 'warning', '接近预警线', 2),
 (1, 'pool-b', '西化学水泵房', '0.5', 'm', 'critical', '超过警戒水位', 3),
 (1, 'pump', '雨水泵站', '2', '台运行', 'normal', '设备运行正常', 4);
INSERT INTO fac_typhoon_risk_warning (incident_id, warn_code, warn_time, warn_type, content, sort_no) VALUES
 (1, '1', '08:05', '内涝预警', '西化学水泵房水位持续上升，建议启动一车一泵应急抽排', 1),
 (1, '2', '07:42', '大风预警', '炼油区西侧风速超过6m/s，请加固高端碳装置周边临时设施', 2),
 (1, '3', '07:18', '暴雨预警', '未来2小时降雨量预计达35mm，注意低洼积水', 3);
INSERT INTO fac_typhoon_live_video (incident_id, video_code, video_label, scene_index, angle, status_name, device_code, sort_no) VALUES
 (1, 'r1-east', '高端碳装置雨水池—东侧全景', 0, '东侧全景', 'online', 'FX-R1-01', 1),
 (1, 'r1-outlet', '高端碳装置污水池—排水口', 1, '排水口近景', 'online', 'FX-R1-02', 2),
 (1, 'r1-road', '高端碳装置雨水池—道路侧', 2, '道路侧俯视', 'offline', 'FX-R1-03', 3),
 (1, 'r2-wide', '西化学水泵房—全景', 1, '泵房全景', 'online', 'FX-R2-01', 4),
 (1, 'r2-inlet', '西化学水泵房—进水渠', 3, '进水渠水位', 'online', 'FX-R2-02', 5),
 (1, 'r2-pump', '西化学水泵房—泵组', 4, '泵组运行区', 'online', 'FX-R2-03', 6),
 (1, 'r3-south', '新鲜水泵房—南侧全景', 2, '南侧全景', 'online', 'FX-R3-01', 7),
 (1, 'r3-drain', '新鲜水泵房—排水沟', 5, '排水沟近景', 'online', 'FX-R3-02', 8),
 (1, 'r4-weigh', '6#路地磅北地沟—地磅侧', 4, '地磅侧全景', 'online', 'FX-R4-01', 9),
 (1, 'r4-ditch', '6#路地磅北地沟—沟渠', 3, '沟渠水位', 'offline', 'FX-R4-02', 10),
 (1, 'r5-west', '11#路西—道路全景', 5, '道路西向', 'online', 'FX-R5-01', 11),
 (1, 'r5-drain', '11#路西—应急抽排点', 0, '抽排作业区', 'online', 'FX-R5-02', 12),
 (1, 'r6-east', '11#路东—道路全景', 0, '道路东向', 'online', 'FX-R6-01', 13),
 (1, 'r6-outlet', '11#路东—排水口', 5, '排水口近景', 'online', 'FX-R6-02', 14),
 (1, 'r7-wide', '储运部中间罐区泵房—全景', 3, '泵房全景', 'online', 'FX-R7-01', 15),
 (1, 'r7-pump', '储运部中间罐区泵房—泵组', 1, '泵组运行区', 'online', 'FX-R7-02', 16),
 (1, 'r7-inlet', '储运部中间罐区泵房—进水侧', 4, '进水侧水位', 'online', 'FX-R7-03', 17),
 (1, 'r8-wide', '中间罐区9#提升池—全景', 2, '提升池全景', 'online', 'FX-R8-01', 18),
 (1, 'r8-level', '中间罐区9#提升池—液位侧', 4, '液位监测侧', 'online', 'FX-R8-02', 19),
 (1, 'r8-outlet', '中间罐区9#提升池—出水侧', 5, '出水侧近景', 'offline', 'FX-R8-03', 20);
INSERT INTO fac_typhoon_map_risk_point (incident_id, point_code, point_name, longitude, latitude, status_name, status_text, responsible_unit, predeployed, deployment, label_offset_x, label_offset_y, cluster_count, kind, video_ids, sort_no) VALUES
 (1, 'r1', '高端碳装置雨水池、污水池', 110.87264, 21.6846, 'warning', '重点巡查', '炼油/高端碳', TRUE, '龙吸水排涝车（炼油布置、高端碳操作）', NULL, NULL, NULL, NULL, 'r1-east,r1-outlet,r1-road', 1),
 (1, 'r2', '西化学水泵房', 110.8768, 21.67999, 'critical', '积水超限', '炼油中队', TRUE, '一车一泵', NULL, NULL, NULL, NULL, 'r2-wide,r2-inlet,r2-pump', 2),
 (1, 'r3', '新鲜水泵房', 110.8838, 21.67158, 'critical', '强制抽排', '炼油中队', TRUE, '大水牛排涝机器人、一车', -54, 10, NULL, NULL, 'r3-south,r3-drain', 3),
 (1, 'r4', '6#路地磅北地沟', 110.8828, 21.67301, 'warning', '水位上涨', '特勤中队', TRUE, '大功率泵浦车在301事故池排水', -36, -26, NULL, NULL, 'r4-weigh,r4-ditch', 4),
 (1, 'r5', '11#路西', 110.88565, 21.67216, 'warning', '持续监测', '特勤/高端碳中队', TRUE, '各一车', NULL, NULL, NULL, NULL, 'r5-west,r5-drain', 5),
 (1, 'r6', '11#路东', 110.88814, 21.67189, 'normal', '排水正常', '机动安排', FALSE, '一车', NULL, NULL, NULL, NULL, 'r6-east,r6-outlet', 6),
 (1, 'r7', '储运部中间罐区泵房', 110.88775, 21.68086, 'normal', '泵组正常', '炼油/高端碳/金塘中队', TRUE, '储运部1泵', NULL, NULL, NULL, NULL, 'r7-wide,r7-pump,r7-inlet', 7),
 (1, 'r8', '储运部中间罐区9#提升池', 110.8858, 21.67803, 'normal', '提升正常', '炼油/高端碳/金塘中队', TRUE, '储运部4泵', NULL, NULL, NULL, NULL, 'r8-wide,r8-level,r8-outlet', 8);
INSERT INTO fac_typhoon_series (incident_id, series_key, point_label, point_value, sort_no) VALUES
 (1, 'precipitation', '02', 2, 1),
 (1, 'precipitation', '04', 4, 2),
 (1, 'precipitation', '06', 8, 3),
 (1, 'precipitation', '08', 14, 4),
 (1, 'precipitation', '10', 22, 5),
 (1, 'precipitation', '12', 28, 6),
 (1, 'precipitation', '14', 35, 7),
 (1, 'precipitation', '16', 30, 8),
 (1, 'precipitation', '18', 18, 9),
 (1, 'precipitation', '20', 10, 10),
 (1, 'precipitation', '22', 6, 11),
 (1, 'precipitation', '24', 3, 12),
 (1, 'wind', '02', 1.2, 1),
 (1, 'wind', '04', 1.8, 2),
 (1, 'wind', '06', 2.1, 3),
 (1, 'wind', '08', 2.4, 4),
 (1, 'wind', '10', 3.2, 5),
 (1, 'wind', '12', 4.5, 6),
 (1, 'wind', '14', 5.8, 7),
 (1, 'wind', '16', 6.2, 8),
 (1, 'wind', '18', 4.8, 9),
 (1, 'wind', '20', 3.6, 10),
 (1, 'wind', '22', 2.8, 11),
 (1, 'wind', '24', 2.2, 12),
 (1, 'waterLevel', '06:00', 0.42, 1),
 (1, 'waterLevel', '08:00', 0.48, 2),
 (1, 'waterLevel', '10:00', 0.55, 3),
 (1, 'waterLevel', '12:00', 0.62, 4),
 (1, 'waterLevel', '14:00', 0.68, 5),
 (1, 'waterLevel', '16:00', 0.72, 6),
 (1, 'waterLevel', '18:00', 0.75, 7);
INSERT INTO fac_typhoon_event_info (incident_id, field_label, field_value, sort_no) VALUES
 (1, '事件名称', '台风沙迦防台防汛工作', 1),
 (1, '事件分类', '极端天气', 2),
 (1, '响应等级', '防台防汛Ⅱ级响应', 3),
 (1, '启动时间', '2026-06-25 08:12:00', 4),
 (1, '影响范围', '全厂范围', 5),
 (1, '指挥部门', '应急管理部', 6),
 (1, '当前措施', '加强低洼区域巡查，雨水泵站双机运行，重点点位视频监控轮巡', 7);
INSERT INTO fac_typhoon_aux_item (incident_id, line1, line2, item_count, count_tone, icon_index, sort_no) VALUES
 (1, '应急预案', '', 15, 'cyan', 0, 1),
 (1, '危险化学品', '知识库', 158, 'cyan', 1, 2),
 (1, '生产区域', '疏散路线图', 12, 'lime', 2, 3),
 (1, '装置区', '专项预案', 8, 'cyan', 3, 4);
INSERT INTO fac_typhoon_dispatch_resource (resource_code, resource_type, resource_name, code, organization, area, status_name, distance_km, eta_minutes, capacity, contact, phone, longitude, latitude, sort_no) VALUES
 ('flood-team-01', '救援队伍', '炼油防汛抢险一组', 'TEAM-FX-01', '炼油分部应急中心', '炼油区', '可调度', 0.7, 4, '12人 · 排涝与警戒', '高策', '18300556146', 110.8769, 21.6804, 1),
 ('flood-vehicle-01', '应急车辆', '龙吸水排涝车', '粤K·PL018', '消防救援中心', '炼油区', '可调度', 0.9, 5, '排水能力 3000m³/h', '陈伟', '18300556148', 110.8791, 21.6787, 2),
 ('flood-vehicle-02', '应急车辆', '大水牛排涝机器人', 'ROBOT-PL-02', '特勤中队', '炼油区', '任务中', 1.4, 8, '远程排涝 · 复杂水域作业', '王磊', '18300556145', 110.8831, 21.6742, 3),
 ('flood-material-01', '应急物资', '移动式大功率排水泵', 'MAT-PUMP-031', '炼油防汛物资库', '炼油区', '可调度', 0.8, 5, '库存 8台 · 可用 6台', '赵敏', '18300556150', 110.8819, 21.681, 4),
 ('flood-material-02', '应急物资', '防汛沙袋与挡水板', 'MAT-FX-016', '炼油防汛物资库', '炼油区', '可调度', 1.1, 6, '沙袋 1200只 · 挡水板 80m', '赵敏', '18300556150', 110.884, 21.6802, 5),
 ('flood-expert-01', '应急专家', '梁海', 'EXP-FX-006', '茂名石化防汛专家组', '炼油区', '可调度', 2.6, 12, '厂区排水系统与防台研判', '梁海', '13802556012', 110.8738, 21.6833, 6);

-- 值班人员沿用 V8 已建 sys_duty_member（应急值班表），知识库沿用 sys_knowledge_item，此处不重复建表。