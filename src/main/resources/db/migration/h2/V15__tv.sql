-- V15 工业电视大屏真实数据源（fm-tv）。
-- 数据来源：src/screen/lib/data/tvMock.ts 存量硬编码（视频概览、运行统计、维保工单、
-- 事件分析、入厂巡检车辆/人员记录），迁移后数值与文案语义保持不变。
-- 命名回避数据库保留字（value/left/top/type/status/level/count/command）：计数列 item_count/total_count。
-- 地图撒点（tvVideoMapPoints）、地图标签（tvMapPins）、巡检圆与扫描点位为前端静态几何/交互配置，
-- 依赖设计舞台坐标换算，不在此建表（与 V13 生产域地图覆盖层同一先例）。

CREATE TABLE fac_tv_stat_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_category VARCHAR(16) NOT NULL,
    label VARCHAR(64) NOT NULL,
    item_count INT NOT NULL DEFAULT 0,
    color VARCHAR(16),
    tone VARCHAR(16),
    icon_index INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_tv_operation_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_count INT NOT NULL DEFAULT 0,
    offline_count INT NOT NULL DEFAULT 0,
    fault_count INT NOT NULL DEFAULT 0,
    integrity_rate INT NOT NULL DEFAULT 0,
    online_rate INT NOT NULL DEFAULT 0,
    event_total INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_tv_inspection_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_kind VARCHAR(16) NOT NULL,
    area_code VARCHAR(32) NOT NULL,
    subject_name VARCHAR(64) NOT NULL,
    badge VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    gate_name VARCHAR(64) NOT NULL,
    record_time VARCHAR(32) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

-- 视频概览卡片（OVERVIEW，6 项）
INSERT INTO fac_tv_stat_item (item_category, label, item_count, color, tone, icon_index, sort_no) VALUES
  ('OVERVIEW', '重大危险源', 665, NULL, NULL, 0, 1),
  ('OVERVIEW', '生产设施', 56, NULL, NULL, 1, 2),
  ('OVERVIEW', '厂界', 56, NULL, NULL, 2, 3),
  ('OVERVIEW', '封闭入口', 55, NULL, NULL, 3, 4),
  ('OVERVIEW', '其他入口', 66, NULL, NULL, 4, 5),
  ('OVERVIEW', '其它', 6, NULL, NULL, 5, 6);

-- 维保工单（MAINTENANCE，3 项）
INSERT INTO fac_tv_stat_item (item_category, label, item_count, color, tone, icon_index, sort_no) VALUES
  ('MAINTENANCE', '未接单', 12, NULL, 'grey', 0, 1),
  ('MAINTENANCE', '处理中', 25, NULL, 'blue', 0, 2),
  ('MAINTENANCE', '已超时', 8, NULL, 'red', 0, 3);

-- 事件分析（EVENT，6 项；event_total 单列于运行统计行）
INSERT INTO fac_tv_stat_item (item_category, label, item_count, color, tone, icon_index, sort_no) VALUES
  ('EVENT', '人员闯入', 150, '#5b8cff', NULL, 0, 1),
  ('EVENT', '烟火检测', 100, '#6a8fd8', NULL, 0, 2),
  ('EVENT', '未戴安全帽', 150, '#3dd68c', NULL, 0, 3),
  ('EVENT', '区域入侵', 152, '#f0b429', NULL, 0, 4),
  ('EVENT', '设备异常', 120, '#ff6b6b', NULL, 0, 5),
  ('EVENT', '其他', 48, '#b07aff', NULL, 0, 6);

INSERT INTO fac_tv_operation_stat (total_count, offline_count, fault_count, integrity_rate, online_rate, event_total)
VALUES (1233, 23, 23, 98, 98, 110);

-- 入厂巡检车辆（VEHICLE，8 条）
INSERT INTO fac_tv_inspection_record (record_kind, area_code, subject_name, badge, department, gate_name, record_time, sort_no) VALUES
  ('VEHICLE', 'refinery', '粤KAA543', '入厂', NULL, '3#门-入', '2026-03-17 10:22:23', 1),
  ('VEHICLE', 'refinery', '粤K·D8621', '出厂', NULL, '2#门-出', '2026-03-17 10:19:46', 2),
  ('VEHICLE', 'refinery', '粤K·B3310', '入厂', NULL, '南门-入', '2026-03-17 10:16:08', 3),
  ('VEHICLE', 'refinery', '粤K·A8821', '出厂', NULL, '东门-出', '2026-03-17 10:11:35', 4),
  ('VEHICLE', 'refinery', '粤K·F2076', '入厂', NULL, '1#门-入', '2026-03-17 10:07:12', 5),
  ('VEHICLE', 'chemical', '粤K·C5198', '出厂', NULL, '3#门-出', '2026-03-17 10:02:54', 6),
  ('VEHICLE', 'chemical', '粤K·E7603', '入厂', NULL, '北门-入', '2026-03-17 09:58:31', 7),
  ('VEHICLE', 'port', '粤K·H1265', '出厂', NULL, '2#门-出', '2026-03-17 09:53:17', 8);

-- 入厂巡检人员（PERSON，6 条）
INSERT INTO fac_tv_inspection_record (record_kind, area_code, subject_name, badge, department, gate_name, record_time, sort_no) VALUES
  ('PERSON', 'refinery', '陈志强', '员工', '炼油运行一部', '3#门-入', '10:21:18', 1),
  ('PERSON', 'refinery', '李明辉', '承包商', '广东安建', '2#门-入', '10:18:42', 2),
  ('PERSON', 'refinery', '王晓峰', '访客', '设备厂商', '东门-入', '10:14:09', 3),
  ('PERSON', 'refinery', '周宇鹏', '员工', '储运部', '南门-出', '10:09:56', 4),
  ('PERSON', 'chemical', '黄建军', '承包商', '石化检修', '1#门-入', '10:05:23', 5),
  ('PERSON', 'port', '张伟东', '员工', '安全环保部', '3#门-出', '09:59:47', 6);
