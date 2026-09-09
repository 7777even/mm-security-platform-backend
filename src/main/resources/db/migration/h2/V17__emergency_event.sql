-- V17 应急事件大屏真实数据源（应急指挥 / 先期处置 / 疏散人员）。
-- 数据来源：frontend-scaffold/src/screen/lib/data/fireEmergencyMock.ts（FIRE 场景事件与演练分组）、
-- preliminaryMock.ts（PRELIMINARY 场景 emergencyEventGroups 及 EmergencyEventItem 字段定义）、
-- drillRescueMock.ts（演练事件，kind=DRILL，事件本体沿用 fireEmergencyMock 的 drill 分组）、
-- evacuationPeopleMock.ts（疏散人员 name/org/job 词表），迁移后数值与文案语义保持不变。
-- 命名回避数据库保留字（value / command / left / top / level）：
-- 计数与百分比列 left_percent / top_percent，经纬度列为 longitude / latitude。
-- 疏散人员经纬度由 mulberry32 伪随机沿路线生成，属前端演示几何，不落库；
-- 仅落 name / org / job 与 route_progress（(i+1)/(count+1) 等分值，count=20）。
-- 事件经纬度由设计稿舞台百分比经 map.pgw 换算得到（与 V15 工业电视同一换算口径）。

CREATE TABLE fac_emergency_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene VARCHAR(16) NOT NULL,
    group_code VARCHAR(32) NOT NULL,
    group_label VARCHAR(64) NOT NULL,
    kind VARCHAR(16) NOT NULL,
    title VARCHAR(128) NOT NULL,
    location VARCHAR(128) NOT NULL,
    description VARCHAR(512) NOT NULL,
    event_time VARCHAR(32) NOT NULL,
    reported BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(16) NOT NULL,
    status_label VARCHAR(64) NOT NULL,
    left_percent VARCHAR(16) NOT NULL,
    top_percent VARCHAR(16) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    area_code VARCHAR(32),
    event_category VARCHAR(32) NOT NULL DEFAULT 'default',
    hazard_source_level VARCHAR(16),
    ended_at VARCHAR(32),
    weather_type VARCHAR(64),
    warning_level VARCHAR(32),
    affected_area VARCHAR(128),
    monitoring_period VARCHAR(64),
    weather_source VARCHAR(64),
    measures VARCHAR(512),
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_evacuation_person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_name VARCHAR(64) NOT NULL,
    org_name VARCHAR(64) NOT NULL,
    job_title VARCHAR(64) NOT NULL,
    route_progress DOUBLE NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

-- FIRE 场景：消防电话报警（phone，3 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'phone', '消防电话报警', 'EVENT', '乙烯裂解炉炉管泄漏着火', '化工区乙烯裂解装置东北侧', '现场报告裂解炉炉管疑似泄漏并伴有明火，已启动装置紧急停车', '2026-03-17 14:21:54', TRUE, 'processing', '泄漏着火', '47.1%', '22.6%', 110.880710, 21.684459, 'chemical', 'default', '二级', NULL, 1),
  ('FIRE', 'phone', '消防电话报警', 'EVENT', '液体化工码头装卸臂泄漏着火', '港区液体化工码头2号泊位', '装卸臂连接法兰发生介质泄漏并出现明火，现场已停止装卸作业', '2026-03-17 13:58:12', TRUE, 'pending', '泄漏着火', '54.2%', '25.3%', 110.883616, 21.683807, 'port', 'default', '三级', NULL, 2),
  ('FIRE', 'phone', '消防电话报警', 'EVENT', '芳烃联合装置泵区物料泄漏', '炼油区芳烃联合装置泵区', '巡检人员发现机泵密封处物料持续泄漏并触发现场手动报警', '2026-03-17 13:12:08', FALSE, 'processing', '物料泄漏', '33.8%', '28.1%', 110.875268, 21.683131, 'refinery', 'default', '四级', NULL, 3);

-- FIRE 场景：储罐消防报警（tank，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'tank', '储罐消防报警', 'EVENT', '乙烯球罐区储罐呼吸阀异常超压', '化工区乙烯球罐组B-3', '储罐压力持续升高，初步判断呼吸阀堵塞，已完成泄压处置', '2026-03-17 12:45:33', TRUE, 'done', '已处置', '48.5%', '29.6%', 110.881283, 21.682768, 'chemical', 'default', '一级', '2026-03-17 14:10:22', 4),
  ('FIRE', 'tank', '储罐消防报警', 'EVENT', '成品油罐区泡沫灭火系统失效', '港区成品油罐组A-2', '泡沫比例混合装置通讯及联动测试失败，罐组消防保护能力受限', '2026-03-17 11:30:21', TRUE, 'pending', '消防系统失效', '36.0%', '33.5%', 110.876168, 21.681826, 'port', 'default', '二级', NULL, 5);

-- FIRE 场景：消防设施异常（facility，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'facility', '消防设施异常', 'EVENT', '稳高压消防水管网压力骤降', '炼油区1号消防泵站', '消防水管网压力短时跌破运行下限，已启动备用消防泵恢复供水', '2026-03-17 10:55:47', TRUE, 'done', '已处置', '46.5%', '46.4%', 110.880465, 21.678709, 'refinery', 'default', '三级', '2026-03-17 12:05:18', 6),
  ('FIRE', 'facility', '消防设施异常', 'EVENT', '化工原料罐区消防栓无法启用', '化工区原料罐组西侧', '现场检查发现消防栓阀门卡涩，无法正常开启，正在组织抢修', '2026-03-17 09:18:06', FALSE, 'pending', '消防设施故障', '52.3%', '42.8%', 110.882838, 21.679579, 'chemical', 'default', '四级', NULL, 7);

-- FIRE 场景：视频烟火联动（video，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'video', '视频烟火联动', 'EVENT', '港区输油管廊疑似火情', '港区码头输油管廊北段', '视频智能分析识别输油管廊出现异常烟火特征，已通知码头岗位人员核查', '2026-03-17 08:42:19', TRUE, 'processing', '疑似火情', '41.2%', '52.6%', 110.878296, 21.677211, 'port', 'default', '一级', NULL, 8),
  ('FIRE', 'video', '视频烟火联动', 'EVENT', '汽柴油装车栈台鹤管泄漏升温', '炼油区汽柴油装车栈台', '红外热成像发现装车鹤管法兰局部温度异常，疑似介质泄漏引发升温', '2026-03-17 07:26:55', TRUE, 'processing', '泄漏升温', '58.6%', '48.2%', 110.885416, 21.678274, 'refinery', 'default', '二级', NULL, 9);

-- FIRE 场景：极端天气（extreme-weather，1 条；weatherMeta 各列留空，供新增极端天气事件时填充）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'extreme-weather', '极端天气', 'EVENT', '台风沙迦防台防汛工作', '全厂范围', '台风"沙迦"逼近，启动防台防汛Ⅱ级响应，重点监测内涝与排涝设施', '2026-06-25 08:12:00', TRUE, 'processing', '防台防汛', '42.0%', '38.5%', 110.878623, 21.680618, 'refinery', 'extremeWeather', NULL, NULL, 10);

-- FIRE 场景：计划演练（drill-plan，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'drill-plan', '计划演练', 'DRILL', '储罐区消防演练', '储罐区B-1', '储罐火灾应急处置联合演练', '2026-03-16 10:00:00', TRUE, 'processing', '演练进行中', '44.0%', '35.2%', 110.879442, 21.681415, 'refinery', 'default', NULL, NULL, 11),
  ('FIRE', 'drill-plan', '计划演练', 'DRILL', '装置区疏散演练', '东厂区-A装置', '装置区人员紧急疏散演练', '2026-03-15 14:30:00', TRUE, 'done', '演练结束', '50.5%', '27.8%', 110.882102, 21.683203, 'chemical', 'default', NULL, '2026-03-15 16:00:00', 12);

-- FIRE 场景：专项演练（drill-special，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'drill-special', '专项演练', 'DRILL', '危化品泄漏演练', '装卸区东侧', '危险化学品泄漏应急处置演练', '2026-03-14 09:00:00', FALSE, 'pending', '待演练', '55.8%', '44.5%', 110.884270, 21.679168, 'port', 'default', NULL, NULL, 13),
  ('FIRE', 'drill-special', '专项演练', 'DRILL', '夜间消防联动演练', '消防泵房北侧', '夜间消防力量联动响应演练', '2026-03-13 20:00:00', TRUE, 'processing', '演练进行中', '38.5%', '40.0%', 110.877191, 21.680255, 'refinery', 'default', NULL, NULL, 14);

-- FIRE 场景：综合演练（drill-comprehensive，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('FIRE', 'drill-comprehensive', '综合演练', 'DRILL', '危化品事故综合演练', '化工区中央大道', '多部门协同的危化品事故综合应急处置演练', '2026-03-12 10:30:00', TRUE, 'done', '演练结束', '47.2%', '31.5%', 110.880751, 21.682309, 'chemical', 'default', NULL, '2026-03-12 12:00:00', 15),
  ('FIRE', 'drill-comprehensive', '综合演练', 'DRILL', '港区溢油应急演练', '水东港区码头', '码头油品泄漏溢油围控回收应急演练', '2026-03-11 15:00:00', FALSE, 'pending', '待演练', '58.0%', '47.0%', 110.885171, 21.678564, 'port', 'default', NULL, NULL, 16);

-- PRELIMINARY 场景：装置异常报警（device，3 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('PRELIMINARY', 'device', '装置异常报警', 'EVENT', '乙烯装置火灾', '东厂区-A装置东北角', 'A装置操作员-张三接听异常报警', '2026-03-17 14:21:54', TRUE, 'processing', '压力报警', '47.1%', '22.6%', 110.880710, 21.684459, NULL, 'default', NULL, NULL, 1),
  ('PRELIMINARY', 'device', '装置异常报警', 'EVENT', 'B装置温度超限', '东厂区-B装置南侧', '反应器出口温度超过联锁阈值', '2026-03-17 13:58:12', TRUE, 'pending', '温度报警', '54.2%', '25.3%', 110.883616, 21.683807, NULL, 'default', NULL, NULL, 2),
  ('PRELIMINARY', 'device', '装置异常报警', 'EVENT', 'C装置泄漏预警', '东厂区-C装置西北角', '可燃气体检测浓度异常升高', '2026-03-17 13:12:08', FALSE, 'processing', '泄漏报警', '33.8%', '28.1%', 110.875268, 21.683131, NULL, 'default', NULL, NULL, 3);

-- PRELIMINARY 场景：储罐压力报警（tank，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('PRELIMINARY', 'tank', '储罐压力报警', 'EVENT', '储罐区B-3压力异常', '储罐区B-3', '储罐顶部压力表读数持续偏高', '2026-03-17 12:45:33', TRUE, 'processing', '压力报警', '44.9%', '31.6%', 110.879810, 21.682285, NULL, 'default', NULL, NULL, 4),
  ('PRELIMINARY', 'tank', '储罐压力报警', 'EVENT', '储罐区A-2液位波动', '储罐区A-2', '液位计短时大幅波动需复核', '2026-03-17 11:30:21', TRUE, 'pending', '液位报警', '36.0%', '33.5%', 110.876168, 21.681826, NULL, 'default', NULL, NULL, 5);

-- PRELIMINARY 场景：日常巡检发现（patrol，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('PRELIMINARY', 'patrol', '日常巡检发现', 'EVENT', '催化区异味上报', '催化裂化装置区', '巡检员上报装置区存在轻微异味', '2026-03-17 10:55:47', TRUE, 'done', '已处置', '46.5%', '46.4%', 110.880465, 21.678709, NULL, 'default', NULL, NULL, 6),
  ('PRELIMINARY', 'patrol', '日常巡检发现', 'EVENT', '泵房振动异常', '原料泵房-2#', '机泵振动值超过日常巡检标准', '2026-03-17 09:18:06', FALSE, 'pending', '设备异常', '52.3%', '42.8%', 110.882838, 21.679579, NULL, 'default', NULL, NULL, 7);

-- PRELIMINARY 场景：视频联动报警（video，2 条）
INSERT INTO fac_emergency_event (scene, group_code, group_label, kind, title, location, description, event_time, reported, status, status_label, left_percent, top_percent, longitude, latitude, area_code, event_category, hazard_source_level, ended_at, sort_no) VALUES
  ('PRELIMINARY', 'video', '视频联动报警', 'EVENT', '厂界人员闯入', '厂区南门西侧', '视频监控识别非授权人员进入限制区域', '2026-03-17 08:42:19', TRUE, 'processing', '区域入侵', '41.2%', '52.6%', 110.878296, 21.677211, NULL, 'default', NULL, NULL, 8),
  ('PRELIMINARY', 'video', '视频联动报警', 'EVENT', '装卸区烟火检测', '液体装卸站台', 'AI视频分析识别疑似烟火特征', '2026-03-17 07:26:55', TRUE, 'processing', '烟火检测', '58.6%', '48.2%', 110.885416, 21.678274, NULL, 'default', NULL, NULL, 9);

-- 疏散人员名册（20 条；route_progress = (i+1)/(20+1) 等分值）
INSERT INTO fac_evacuation_person (person_name, org_name, job_title, route_progress, sort_no) VALUES
  ('张建', '生产管理部', '班长', 0.047619, 1),
  ('李明', '装置运行一班', '主操', 0.095238, 2),
  ('王强', '装置运行二班', '外操', 0.142857, 3),
  ('陈伟', '应急抢险组', '安全员', 0.190476, 4),
  ('刘洋', '现场指挥组', '调度', 0.238095, 5),
  ('赵磊', '装置运行一班', '主操', 0.285714, 6),
  ('黄军', '生产管理部', '调度', 0.333333, 7),
  ('周鹏', '应急抢险组', '安全员', 0.380952, 8),
  ('吴涛', '装置运行二班', '外操', 0.428571, 9),
  ('郑凯', '现场指挥组', '班长', 0.476190, 10),
  ('张建', '装置运行一班', '外操', 0.523810, 11),
  ('李明', '应急抢险组', '安全员', 0.571429, 12),
  ('王强', '生产管理部', '调度', 0.619048, 13),
  ('陈伟', '装置运行二班', '主操', 0.666667, 14),
  ('刘洋', '现场指挥组', '班长', 0.714286, 15),
  ('赵磊', '应急抢险组', '外操', 0.761905, 16),
  ('黄军', '装置运行一班', '主操', 0.809524, 17),
  ('周鹏', '生产管理部', '调度', 0.857143, 18),
  ('吴涛', '现场指挥组', '安全员', 0.904762, 19),
  ('郑凯', '装置运行二班', '外操', 0.952381, 20);
