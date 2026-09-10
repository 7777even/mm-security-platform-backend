-- V30 应急流程「节点联动配置」真实数据源（fm-accident-rescue 应急指挥 · 节点联动配置弹窗）。
-- 数据来源：src/screen/lib/data/nodeConfigData.ts 的 DEFAULT_NODE_PHASE_CONFIGS（9 个流程节点：
-- 接警研判/1min/3min/5min/装置区/全厂/政府/完成处置/总结与恢复），迁移后数值与文案语义保持不变。
-- 列表字段（锚点优先级 / 右侧隐藏页签 / 左侧隐藏面板）以逗号连接的字符串落库，空串表示空集合，
-- 由服务端在读写时拆装为数组；custom_center 以 "lon,lat" 文本存储，空表示未配自定义坐标。
-- 命名回避数据库保留字（value/type/mode/time/seq）。

CREATE TABLE fac_node_phase_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_id VARCHAR(32) NOT NULL,
    node_name VARCHAR(64) NOT NULL,
    camera_anchors VARCHAR(256) NOT NULL DEFAULT '',
    custom_center VARCHAR(64),
    buffer_radius_meters INT NOT NULL DEFAULT 260,
    right_hidden_tabs VARCHAR(256) NOT NULL DEFAULT '',
    left_hidden_panels VARCHAR(256) NOT NULL DEFAULT '',
    duty_auto_roster BOOLEAN NOT NULL DEFAULT FALSE,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_node_phase_config
    (node_id, node_name, camera_anchors, custom_center, buffer_radius_meters, right_hidden_tabs, left_hidden_panels, duty_auto_roster, sort_no)
VALUES
    ('alarmJudgement', '1. 接警研判', 'alarm_phone_location,event_device,alarm_phone_zone,factory_center', NULL, 260, '', '', TRUE, 1),
    ('1min', '2. 一分钟能量隔离', 'event_device,alarm_phone_zone,factory_center', NULL, 220, '', '', TRUE, 2),
    ('3min', '3. 三分钟退守稳态', 'event_device,factory_center', NULL, 320, 'dynamics', 'info', TRUE, 3),
    ('5min', '4. 五分钟消气防控险', 'event_device,first_responder_gps,factory_center', NULL, 380, 'auxiliary', 'info', TRUE, 4),
    ('plantArea', '5. 装置区应急', 'event_device,alarm_phone_zone,factory_center', NULL, 460, '', 'info', FALSE, 5),
    ('companyLevel', '6. 全厂应急', 'factory_center,event_device', NULL, 900, 'auxiliary', 'info', FALSE, 6),
    ('govLevel', '7. 政府应急', 'factory_center', NULL, 1400, 'auxiliary,dynamics', 'info,plan', FALSE, 7),
    ('handling', '8. 完成处置', 'event_device,factory_center', NULL, 300, '', 'info', TRUE, 8),
    ('archive', '9. 总结与恢复', 'factory_center', NULL, 1200, 'auxiliary,dynamics', 'info,plan', TRUE, 9);
