-- =============================================================================
-- V44 大屏数据富集 —— 达梦 DM8 方言（Oracle 兼容，未实跑验证）
--   镜像自 h2/V44__screen_dataset_enrich.sql。本文件未经达梦实例实跑验证，按 DM8 语法编写，需上环境复核。
-- =============================================================================

-- =============================================================================
-- V44 大屏端数据集二次加厚（H2 方言）
--   背景：gap 文档 §2 列出 9 个稀疏端点，其中 6 个已由 V35__screen_remaining_dataset
--         （值班/闭环案例/知识库/监测告警/系统消息/通讯设备）加厚；本迁移补齐 V35 未覆盖的 2 个：
--          - fac_workstation（/dashboard/workstations，V2 仅 3 行 → 加至 13）
--          - fac_production_personnel（/production/personnel，V13 仅 3 行且同区 → 加至 13）
--   救援资源（fac_rescue_*）装备 35 / 人员 52 / 车辆 12，中队 8 个，已丰满，不重复加厚。
--   纪律：V1–V43 已进共享环境，禁改禁删；本期只新增 INSERT。
--   H2 保留字规避：两表均无裸 value/command/type/time/mode/seq 列。
-- =============================================================================

-- 1) 工位主数据加厚（原 3 行，全厂各区域补充，共 +10 行）
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-04', '炼油中控室工位',     '炼油区',     1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-05', '化工区值班室工位',   '化工区',     1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-06', '乙烯中控室工位',     '乙烯区',     1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-07', '储运区值班室工位',   '储运区',     1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-08', '公用工程值班室工位', '公用工程区', 1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-09', '码头调度室工位',     '码头区',     1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-10', '消防指挥中心工位',   '全厂范围',   1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-11', '安全环保值班室工位', '全厂范围',   0)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-12', '储运区装车台工位',   '储运区',     1)
INSERT INTO fac_workstation (workstation_id, name, zone, online) VALUES ('WS-13', '化工区巡检室工位',   '化工区',     0)


-- 2) 生产人员定位撒点加厚（原 3 行均在「炼化厂区丙侧」，补充多区域多坐标，共 +10 行）
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('12.5%', '18.2%', 110.8765, 21.6841, '炼油区中控室',     412, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 4)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('22.1%', '33.7%', 110.8789, 21.6798, '化工区装置区',     308, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 5)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('67.4%', '21.9%', 110.8871, 21.6835, '乙烯区裂解装置',   276, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 6)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('41.8%', '52.3%', 110.8812, 21.6779, '储运区罐组',       198, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 7)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('78.6%', '44.1%', 110.8889, 21.6788, '公用工程区',       145, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 8)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('9.3%',  '61.5%', 110.8751, 21.6772, '码头装卸区',       167, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 9)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('55.2%', '63.8%', 110.8847, 21.6765, '芳烃联合装置',     233, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 10)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('31.7%', '71.4%', 110.8798, 21.6761, '加氢装置区',       254, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 11)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('83.1%', '28.6%', 110.8892, 21.6821, '全厂管廊巡检',     121, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 12)
INSERT INTO fac_production_personnel (left_ratio, top_ratio, longitude, latitude, location, person_count, marker_icon, popup_bg, marker_dot, marker_line, sort_no) VALUES ('48.9%', '12.7%', 110.8823, 21.6852, '乙烯区中央控制室', 318, 'person_cluster.png', '#0b2a4a', '#3ec6ff', '#3ec6ff', 13)
