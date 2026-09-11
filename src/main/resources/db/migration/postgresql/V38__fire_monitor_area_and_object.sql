-- =============================================================================
-- V38 消防态势大屏数据源（PostgreSQL方言）—— 大屏「前端数据全后端化」P0
--   目的同 h2/V38；本文件未经 PG 实例实跑验证（本地无 PG 实例），按标准 PG 语法编写。
-- =============================================================================

CREATE TABLE fac_fire_monitor_area (
    id BIGSERIAL PRIMARY KEY,
    area_code VARCHAR(32) NOT NULL,
    scope VARCHAR(16) NOT NULL,
    area_name VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    status_label VARCHAR(64) NOT NULL,
    equipment INT NOT NULL DEFAULT 0,
    cameras INT NOT NULL DEFAULT 0,
    personnel INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_monitored_object (
    id BIGSERIAL PRIMARY KEY,
    obj_name VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    detail VARCHAR(128) NOT NULL,
    tone VARCHAR(16) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_fire_monitor_area (area_code, scope, area_name, status, status_label, equipment, cameras, personnel, sort_no) VALUES
('refinery-1', 'refinery', '炼油一部装置区', 'normal', '运行正常', 128, 24, 16, 1),
('refinery-2', 'refinery', '储运罐区', 'attention', '2台设备离线', 96, 18, 9, 2),
('refinery-3', 'refinery', '芳烃联合装置', 'normal', '运行正常', 112, 21, 14, 3),
('refinery-4', 'refinery', '催化裂化装置区', 'normal', '运行正常', 118, 22, 15, 4),
('refinery-5', 'refinery', '加氢裂化装置区', 'normal', '运行正常', 104, 20, 12, 5),
('refinery-6', 'refinery', '西化学水泵房', 'attention', '1项巡检待办', 68, 12, 7, 6),
('chemical-1', 'chemical', '化工装置区', 'normal', '运行正常', 143, 31, 22, 7),
('chemical-2', 'chemical', '聚烯烃装置区', 'normal', '运行正常', 105, 19, 13, 8),
('chemical-3', 'chemical', '乙烯联合装置区', 'normal', '运行正常', 126, 25, 18, 9),
('chemical-4', 'chemical', '公用工程装置区', 'attention', '1台设备离线', 91, 17, 10, 10),
('port-1', 'port', '水东港储运区', 'normal', '运行正常', 87, 16, 11, 11),
('port-2', 'port', '博贺新港作业区', 'attention', '1项巡检待办', 74, 14, 8, 12),
('port-3', 'port', '输油管廊区', 'normal', '运行正常', 82, 19, 9, 13),
('port-4', 'port', '成品油码头区', 'normal', '运行正常', 65, 13, 8, 14);

INSERT INTO fac_fire_monitored_object (obj_name, status, detail, tone, sort_no) VALUES
('A装置区', '告警', '1起火灾告警处置中', 'danger', 1),
('储运罐区', '预警', '1项特级动火作业', 'warning', 2),
('芳烃联合装置', '正常', '128个感知点在线', 'normal', 3),
('西化学水泵房', '正常', '36个感知点在线', 'normal', 4);
