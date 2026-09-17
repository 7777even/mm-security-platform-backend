-- =============================================================================
-- V29 周界报警 —— 达梦 DM8 方言（Oracle 兼容，未实跑验证）
--   镜像自 h2/V29__perimeter_alarm.sql。本文件未经达梦实例实跑验证，按 DM8 语法编写，需上环境复核。
-- =============================================================================

-- V29 周界入侵告警（替代前端 SecurityStatusPanel 的 demo 常量 resolveDemoAlarmDetailById('demo-intrusion-1')）。
-- 1) 周界入侵告警主表：字段对齐前端 AlarmDetailItem 视图模型（services/map-data/alarmDetailMock.ts）。
CREATE TABLE fac_perimeter_alarm (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    alarm_code VARCHAR2(32 CHAR),
    title VARCHAR2(64 CHAR),
    alarm_type VARCHAR2(16 CHAR),
    source VARCHAR2(32 CHAR),
    level_code VARCHAR2(8 CHAR),
    status VARCHAR2(16 CHAR),
    false_alarm VARCHAR2(8 CHAR),
    alarm_time VARCHAR2(32 CHAR),
    object_type VARCHAR2(32 CHAR),
    object_name VARCHAR2(64 CHAR),
    location VARCHAR2(128 CHAR),
    description VARCHAR2(255 CHAR),
    device_type VARCHAR2(32 CHAR),
    device_id VARCHAR2(32 CHAR),
    point VARCHAR2(64 CHAR),
    intrusion_position VARCHAR2(64 CHAR),
    intrusion_method VARCHAR2(32 CHAR),
    related_camera VARCHAR2(32 CHAR),
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    dispatch_personnel VARCHAR2(128 CHAR),
    notify_app NUMBER(1),
    notify_sms NUMBER(1),
    handle_result VARCHAR2(255 CHAR),
    handle_time VARCHAR2(32 CHAR),
    rescue_event_id NUMBER(19),
    monitor_id VARCHAR2(32 CHAR),
    monitor_label VARCHAR2(64 CHAR),
    work_order_no VARCHAR2(32 CHAR),
    snapshot_bytes BLOB
);
SET IDENTITY_INSERT fac_perimeter_alarm ON;



-- 2) 种子：当前待处置告警（南门西侧周界，对应原 demo-intrusion-1）
INSERT INTO fac_perimeter_alarm (
    id, alarm_code, title, alarm_type, source, level_code, status, false_alarm, alarm_time,
    object_type, object_name, location, description, device_type, device_id, point,
    intrusion_position, intrusion_method, related_camera, longitude, latitude,
    dispatch_personnel, notify_app, notify_sms, handle_result, handle_time,
    rescue_event_id, monitor_id, monitor_label, work_order_no
) VALUES (
    1, 'AL-20260820-007', '周界入侵告警', '周界', '周界防范', '一级', '未确认', '未核实',
    '2026-08-20 03:22:48', '区域', '南门西侧周界', '厂区南门西侧 200 米',
    '非授权人员翻越周界进入厂区，请立即核实。', '周界摄像机', 'CAM-PERI-07', '南门西侧 200 米',
    '南门西侧 200 米', '翻越围栏', 'CAM-PERI-07', 110.8872, 21.6709,
    NULL, 1, 0, '', '', 7, 'cam-peri-07', '南门西侧周界监控', NULL
);


-- 3) 种子：历史已处置告警（西门北侧周界，用于详情/列表闭环演示）
INSERT INTO fac_perimeter_alarm (
    id, alarm_code, title, alarm_type, source, level_code, status, false_alarm, alarm_time,
    object_type, object_name, location, description, device_type, device_id, point,
    intrusion_position, intrusion_method, related_camera, longitude, latitude,
    dispatch_personnel, notify_app, notify_sms, handle_result, handle_time,
    rescue_event_id, monitor_id, monitor_label, work_order_no
) VALUES (
    2, 'AL-20260819-003', '周界入侵告警', '周界', '周界防范', '二级', '已处理', '否',
    '2026-08-19 21:10:05', '区域', '西门北侧周界', '厂区西门北侧 120 米',
    '周界振动光纤触发告警，疑似人员靠近围栏。', '周界摄像机', 'CAM-PERI-03', '西门北侧 120 米',
    '西门北侧 120 米', '翻越围栏', 'CAM-PERI-03', 110.8768, 21.6814,
    '王成,赵五', 1, 0, '经核实为检修人员临时跨越通道，已现场纠正并封闭临时开口。',
    '2026-08-19 21:42:10', 2, 'cam-peri-03', '西门北侧周界监控', 'WO202608190012'
);

SET IDENTITY_INSERT fac_perimeter_alarm OFF;
