-- =============================================================================
-- V41 台风应急域「响应板」横幅/指令数据源（h2方言）—— 大屏「前端数据全后端化」P1
--   目的：把 TyphoonLeftPanel 的 weatherAlertBanners(3) + weatherCommands(5)/
--         temporaryCommands(2) 迁到后端，由 GET /api/v1/typhoon/response-board 提供。
--   口径：与前端原硬编码逐位对齐。
--   坑位：level/status/time 为 H2 保留字风险列名 → warn_level / cmd_status / cmd_time。
-- =============================================================================

CREATE TABLE fac_typhoon_alert_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warn_level VARCHAR(16) NOT NULL,
    title VARCHAR(64) NOT NULL,
    detail VARCHAR(128) NOT NULL,
    tone VARCHAR(16) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_typhoon_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cmd_code VARCHAR(16) NOT NULL,
    command_kind VARCHAR(16) NOT NULL,
    group_label VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    target VARCHAR(64) NOT NULL,
    cmd_status VARCHAR(16) NOT NULL,
    cmd_time VARCHAR(8) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

INSERT INTO fac_typhoon_alert_banner (warn_level, title, detail, tone, sort_no) VALUES
('橙色预警', '防台防汛Ⅱ级响应', '暴雨预警触发 · 持续监测中', 'orange', 1),
('黄色预警', '雷电天气防御', '雷电预警生效 · 强对流持续关注', 'yellow', 2),
('蓝色预警', '大风天气防御', '阵风风险上升 · 高处作业已管控', 'blue', 3);

INSERT INTO fac_typhoon_command (cmd_code, command_kind, group_label, name, target, cmd_status, cmd_time, detail, sort_no) VALUES
('w1', 'plan', '预警与启动', '发布防台防汛预警', '各生产单位、承包商', '已完成', '08:13', '发布橙色预警，要求停止露天高处及吊装作业。', 1),
('w2', 'plan', '重点点位管控', '易涝点巡查与水位上报', '炼油防汛抢险一组', '执行中', '08:18', '每15分钟通过APP反馈8个易涝点水位及现场影像。', 2),
('w3', 'plan', '排涝力量部署', '预置排涝车辆和移动泵', '消防救援中心、特勤中队', '待执行', '08:20', '龙吸水排涝车前置至西化学水泵房，大功率泵浦车进驻301事故池。', 3),
('w4', 'plan', '生产运行保障', '核查雨排系统与关键电源', '机动部、电仪中心', '执行中', '08:24', '确认雨水泵双机热备，检查低洼区域配电设施防水措施。', 4),
('w5', 'plan', '人员安全', '限制涉水区域通行', '安全环保部、保卫部', '待执行', '08:27', '设置警戒线并引导车辆绕行，防止人员进入深水区域。', 5),
('t1', 'temporary', '现场加派', '增派2台移动排水泵', '炼油防汛物资库', '待执行', '08:31', '支援6#路地磅北地沟，完成后反馈泵组运行电流。', 1),
('t2', 'temporary', '气象会商', '组织短临天气会商', '应急管理部、气象服务单位', '执行中', '08:35', '研判未来3小时强降雨落区及厂区影响。', 2);
