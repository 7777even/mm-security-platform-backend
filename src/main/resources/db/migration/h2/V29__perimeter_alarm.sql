-- V29 周界入侵告警（替代前端 SecurityStatusPanel 的 demo 常量 resolveDemoAlarmDetailById('demo-intrusion-1')）。
-- 1) 周界入侵告警主表：字段对齐前端 AlarmDetailItem 视图模型（services/map-data/alarmDetailMock.ts）。
CREATE TABLE fac_perimeter_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alarm_code VARCHAR(32),
    title VARCHAR(64),
    alarm_type VARCHAR(16),
    source VARCHAR(32),
    level_code VARCHAR(8),
    status VARCHAR(16),
    false_alarm VARCHAR(8),
    alarm_time VARCHAR(32),
    object_type VARCHAR(32),
    object_name VARCHAR(64),
    location VARCHAR(128),
    description VARCHAR(255),
    device_type VARCHAR(32),
    device_id VARCHAR(32),
    point VARCHAR(64),
    intrusion_position VARCHAR(64),
    intrusion_method VARCHAR(32),
    related_camera VARCHAR(32),
    longitude DOUBLE,
    latitude DOUBLE,
    dispatch_personnel VARCHAR(128),
    notify_app BOOLEAN,
    notify_sms BOOLEAN,
    handle_result VARCHAR(255),
    handle_time VARCHAR(32),
    rescue_event_id BIGINT,
    monitor_id VARCHAR(32),
    monitor_label VARCHAR(64),
    work_order_no VARCHAR(32),
    snapshot_bytes BLOB
);

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
    NULL, TRUE, FALSE, '', '', 7, 'cam-peri-07', '南门西侧周界监控', NULL
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
    '王成,赵五', TRUE, FALSE, '经核实为检修人员临时跨越通道，已现场纠正并封闭临时开口。',
    '2026-08-19 21:42:10', 2, 'cam-peri-03', '西门北侧周界监控', 'WO202608190012'
);
