-- V24 大屏面板遗留硬编码数据落地（消防设备分类 / 系统消息条 / 工业电视地图撒点 / 视频监控档案）。
-- 数据来源：src/screen/lib/data/mock.ts（fireEquipment、systemMessages）与
-- src/screen/lib/data/tvMock.ts（tvVideoMapPoints、tvVideoMonitorDetails / defaultVideoMonitorDetail），
-- 迁移后文案与数值语义保持不变；地图撒点经纬度由设计舞台百分比按 map.pgw 换算后固化入库。
-- 命名回避数据库保留字（value/command/type/status/level/count）：计数列 equip_count，
-- 高度列 point_height / height_text，类型列 msg_type / monitor_type / point_group。

CREATE TABLE fac_fire_equipment_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(64) NOT NULL,
    equip_count INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_system_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    msg_type VARCHAR(16) NOT NULL,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(512) NOT NULL,
    occurred_at VARCHAR(32) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_tv_map_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    point_code VARCHAR(32) NOT NULL,
    point_label VARCHAR(64) NOT NULL,
    point_group VARCHAR(16) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    point_height INT NOT NULL DEFAULT 0,
    online BOOLEAN NOT NULL DEFAULT TRUE,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_tv_monitor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monitor_code VARCHAR(32) NOT NULL,
    monitor_name VARCHAR(64) NOT NULL,
    online BOOLEAN NOT NULL DEFAULT TRUE,
    integrity VARCHAR(16) NOT NULL,
    monitor_type VARCHAR(32) NOT NULL,
    department VARCHAR(64) NOT NULL,
    location_desc VARCHAR(64) NOT NULL,
    height_text VARCHAR(32) NOT NULL,
    angle_text VARCHAR(32) NOT NULL
);

-- 消防设备分类（12 项，与 mock.ts fireEquipmentCategories 顺序与文案一致）
INSERT INTO fac_fire_equipment_category (category_name, equip_count, sort_no) VALUES
  ('火灾自动报警系统', 665, 1),
  ('消防水源', 665, 2),
  ('室外消火栓系统', 665, 3),
  ('自动喷水灭火系统', 665, 4),
  ('气体灭火系统', 665, 5),
  ('泡沫灭火系统', 665, 6),
  ('干粉灭火系统', 665, 7),
  ('防烟排烟系统', 665, 8),
  ('防火分隔设施', 665, 9),
  ('消防应急广播', 665, 10),
  ('应急照明及疏散指示系统', 665, 11),
  ('消防电源', 665, 12);

-- 大屏底部系统消息滚动条（与 mock.ts systemMessages 一致）
INSERT INTO fac_system_message (msg_type, title, content, occurred_at, sort_no) VALUES
  ('danger', '人员违规进入', 'A装置区域发现非注册人员，请核实。', '2026-03-17 14:21:30', 1),
  ('warning', '有毒气体超标', 'A装置区域有毒气体浓度超过标准值200%，请相关人员立即撤离。', '2026-03-17 14:21:30', 2);

-- 工业电视地图撒点（15 项，经纬度由设计舞台百分比换算后固化）
INSERT INTO fac_tv_map_point (point_code, point_label, point_group, longitude, latitude, point_height, online, sort_no) VALUES
  ('ar-01', '高空AR-01', 'high-ar', 110.881979, 21.685692, 74, TRUE, 1),
  ('ar-02', '高空AR-02', 'high-ar', 110.886725, 21.684000, 74, TRUE, 2),
  ('ar-03', '高空AR-03', 'high-ar', 110.879237, 21.682237, 74, TRUE, 3),
  ('focus-01', '储罐区东侧球机', 'focus', 110.880997, 21.681415, 74, TRUE, 4),
  ('focus-02', '乙烯装置入口', 'focus', 110.883984, 21.682816, 74, TRUE, 5),
  ('focus-03', '管廊巡检点A', 'focus', 110.885457, 21.679965, 74, TRUE, 6),
  ('focus-04', '装卸区监控', 'focus', 110.878705, 21.679168, 74, FALSE, 7),
  ('hazard-01', '储罐区B-3', 'hazard', 110.881774, 21.683348, 74, TRUE, 8),
  ('hazard-02', '催化裂化装置', 'hazard', 110.883084, 21.680569, 74, TRUE, 9),
  ('hazard-03', '加氢装置区', 'hazard', 110.880424, 21.684170, 74, TRUE, 10),
  ('hazard-04', '液化烃罐区', 'hazard', 110.885907, 21.681560, 74, TRUE, 11),
  ('boundary-01', '厂区西门', 'boundary', 110.878010, 21.682382, 74, TRUE, 12),
  ('boundary-02', '厂区北门', 'boundary', 110.882634, 21.686102, 74, TRUE, 13),
  ('boundary-03', '厂界东侧', 'boundary', 110.887339, 21.682309, 74, TRUE, 14),
  ('boundary-04', '厂界南侧', 'boundary', 110.882961, 21.677646, 74, FALSE, 15);

-- 视频监控档案（与地图撒点一一对应，默认档案项取自 tvMock defaultVideoMonitorDetail）
INSERT INTO fac_tv_monitor (monitor_code, monitor_name, online, integrity, monitor_type, department, location_desc, height_text, angle_text) VALUES
  ('ar-01', '高空AR-01', TRUE, '良好', '球机', '安环部', '110.881979, 21.685692', '24m', '56°'),
  ('ar-02', '高空AR-02', TRUE, '良好', '球机', '安环部', '110.886725, 21.684000', '24m', '56°'),
  ('ar-03', '高空AR-03', TRUE, '良好', '球机', '安环部', '110.879237, 21.682237', '24m', '56°'),
  ('focus-01', '储罐区东侧球机', TRUE, '良好', '球机', '安环部', '110.880997, 21.681415', '15m', '56°'),
  ('focus-02', '乙烯装置入口', FALSE, '一般', '枪机', '安环部', '110.883984, 21.682816', '15m', '56°'),
  ('focus-03', '管廊巡检点A', TRUE, '良好', '球机', '安环部', '110.885457, 21.679965', '15m', '56°'),
  ('focus-04', '装卸区监控', FALSE, '一般', '枪机', '储运部', '110.878705, 21.679168', '15m', '56°'),
  ('hazard-01', '储罐区B-3', TRUE, '良好', '球机', '安环部', '110.881774, 21.683348', '15m', '56°'),
  ('hazard-02', '催化裂化装置', TRUE, '良好', '枪机', '炼油运行一部', '110.883084, 21.680569', '15m', '56°'),
  ('hazard-03', '加氢装置区', TRUE, '良好', '球机', '炼油运行二部', '110.880424, 21.684170', '15m', '56°'),
  ('hazard-04', '液化烃罐区', TRUE, '良好', '球机', '储运部', '110.885907, 21.681560', '15m', '56°'),
  ('boundary-01', '厂区西门', TRUE, '良好', '枪机', '安环部', '110.878010, 21.682382', '15m', '56°'),
  ('boundary-02', '厂区北门', TRUE, '良好', '枪机', '安环部', '110.882634, 21.686102', '15m', '56°'),
  ('boundary-03', '厂界东侧', TRUE, '良好', '球机', '安环部', '110.887339, 21.682309', '15m', '56°'),
  ('boundary-04', '厂界南侧', FALSE, '一般', '枪机', '安环部', '110.882961, 21.677646', '15m', '56°');
