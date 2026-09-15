-- V52 ④-C 管理台账扩展：6 个生产必需域种子化（达梦 DM8 方言 Oracle 兼容，未实跑验证）
-- 复用 V51 的 mgmt_ledger_meta/row/cell 三表，仅补充数据，无表结构变更。
-- 域：key-location 消防重点部位 / incident-archive 灭火事件档案 / drill-script 演练脚本
--      linkage-unit 后勤联动单位 / emergency-pool 应急雨水监控池 / ef-medium 设备介质
-- 列定义沿用前端 mgmtMenus 原型页约定；ID 在 V51（meta 1-18 / row 1-31 / cell 1-156）之后顺序延续。

-- 元数据
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (19, 'key-location', '消防重点部位管理', '["消防重点部位名称","类别","火灾危险性","耐火等级","所属装置","责任人","启用"]', '[]', 19);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (20, 'incident-archive', '灭火事件档案管理', '["事件编号","事件名称","起火时间","部位数","起火原因","事件等级"]', '[]', 20);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (21, 'drill-script', '演练脚本管理', '["脚本编号","脚本名称","适用场景","步骤数","版本","启用"]', '[]', 21);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (22, 'linkage-unit', '后勤联动单位管理', '["单位名称","联动类型","联系人","电话","启用"]', '[]', 22);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (23, 'emergency-pool', '应急/雨水监控池管理', '["编号","名称","类型","容积(m³)","深度(m)","所属厂区","监测点位"]', '[]', 23);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (24, 'ef-medium', '设备介质管理', '["编号","介质名称","所属设备","物态","危险特性","备注"]', '[]', 24);

-- 数据行
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (32, 'key-location', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (33, 'key-location', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (34, 'key-location', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (35, 'incident-archive', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (36, 'incident-archive', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (37, 'incident-archive', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (38, 'drill-script', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (39, 'drill-script', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (40, 'drill-script', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (41, 'linkage-unit', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (42, 'linkage-unit', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (43, 'linkage-unit', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (44, 'emergency-pool', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (45, 'emergency-pool', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (46, 'emergency-pool', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (47, 'ef-medium', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (48, 'ef-medium', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (49, 'ef-medium', 2, 2);

-- 单元格（col_key 固定 c0..cN；cell_type: ok/warn/bad 渲染状态标签，NULL 为普通文本）
-- key-location
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (157, 32, 0, 'c0', 'T-301 罐区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (158, 32, 1, 'c1', '甲类', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (159, 32, 2, 'c2', '甲', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (160, 32, 3, 'c3', '一级', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (161, 32, 4, 'c4', '储运部', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (162, 32, 5, 'c5', '李主任', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (163, 32, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (164, 33, 0, 'c0', '加氢裂化装置', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (165, 33, 1, 'c1', '甲类', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (166, 33, 2, 'c2', '甲', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (167, 33, 3, 'c3', '二级', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (168, 33, 4, 'c4', '炼油一部', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (169, 33, 5, 'c5', '王工', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (170, 33, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (171, 34, 0, 'c0', '液化烃罐区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (172, 34, 1, 'c1', '甲类', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (173, 34, 2, 'c2', '甲', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (174, 34, 3, 'c3', '一级', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (175, 34, 4, 'c4', '储运部', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (176, 34, 5, 'c5', '赵班长', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (177, 34, 6, 'c6', '否', 'warn');

-- incident-archive
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (178, 35, 0, 'c0', 'EVT-2026-003', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (179, 35, 1, 'c1', '装卸台冒烟处置', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (180, 35, 2, 'c2', '2026-07-12 14:20', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (181, 35, 3, 'c3', '1', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (182, 35, 4, 'c4', '电气过热', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (183, 35, 5, 'c5', '一般', 'warn');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (184, 36, 0, 'c0', 'EVT-2025-011', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (185, 36, 1, 'c1', '罐区阀门泄漏着火', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (186, 36, 2, 'c2', '2025-09-03 08:45', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (187, 36, 3, 'c3', '2', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (188, 36, 4, 'c4', '阀门密封失效', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (189, 36, 5, 'c5', '较大', 'bad');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (190, 37, 0, 'c0', 'EVT-2024-007', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (191, 37, 1, 'c1', '机柜间短路', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (192, 37, 2, 'c2', '2024-11-20 22:10', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (193, 37, 3, 'c3', '1', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (194, 37, 4, 'c4', '电缆老化', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (195, 37, 5, 'c5', '一般', 'warn');

-- drill-script
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (196, 38, 0, 'c0', 'SCR-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (197, 38, 1, 'c1', '储罐火灾处置脚本', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (198, 38, 2, 'c2', '储罐火灾', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (199, 38, 3, 'c3', '12', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (200, 38, 4, 'c4', 'V2.0', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (201, 38, 5, 'c5', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (202, 39, 0, 'c0', 'SCR-02', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (203, 39, 1, 'c1', '装置泄漏处置脚本', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (204, 39, 2, 'c2', '装置泄漏', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (205, 39, 3, 'c3', '9', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (206, 39, 4, 'c4', 'V1.3', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (207, 39, 5, 'c5', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (208, 40, 0, 'c0', 'SCR-03', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (209, 40, 1, 'c1', '人员中毒救援脚本', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (210, 40, 2, 'c2', '受限空间', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (211, 40, 3, 'c3', '7', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (212, 40, 4, 'c4', 'V1.0', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (213, 40, 5, 'c5', '否', 'warn');

-- linkage-unit
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (214, 41, 0, 'c0', '市一医院', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (215, 41, 1, 'c1', '医疗救护', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (216, 41, 2, 'c2', '急诊值班', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (217, 41, 3, 'c3', '120', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (218, 41, 4, 'c4', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (219, 42, 0, 'c0', '消防救援支队', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (220, 42, 1, 'c1', '火灾扑救', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (221, 42, 2, 'c2', '指挥中心', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (222, 42, 3, 'c3', '119', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (223, 42, 4, 'c4', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (224, 43, 0, 'c0', '危废处置公司', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (225, 43, 1, 'c1', '环保处置', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (226, 43, 2, 'c2', '张经理', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (227, 43, 3, 'c3', '138****0021', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (228, 43, 4, 'c4', '否', 'warn');

-- emergency-pool
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (229, 44, 0, 'c0', 'POOL-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (230, 44, 1, 'c1', '雨水监控池 A', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (231, 44, 2, 'c2', '雨水', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (232, 44, 3, 'c3', '2000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (233, 44, 4, 'c4', '3.5', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (234, 44, 5, 'c5', '东区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (235, 44, 6, 'c6', 'LVL-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (236, 45, 0, 'c0', 'POOL-02', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (237, 45, 1, 'c1', '事故应急池', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (238, 45, 2, 'c2', '应急', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (239, 45, 3, 'c3', '5000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (240, 45, 4, 'c4', '4.0', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (241, 45, 5, 'c5', '西区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (242, 45, 6, 'c6', 'LVL-02', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (243, 46, 0, 'c0', 'POOL-03', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (244, 46, 1, 'c1', '消防废水池', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (245, 46, 2, 'c2', '消防', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (246, 46, 3, 'c3', '3000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (247, 46, 4, 'c4', '3.8', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (248, 46, 5, 'c5', '南区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (249, 46, 6, 'c6', 'LVL-03', NULL);

-- ef-medium
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (250, 47, 0, 'c0', 'MD-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (251, 47, 1, 'c1', '循环氢', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (252, 47, 2, 'c2', 'C-101', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (253, 47, 3, 'c3', '气体', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (254, 47, 4, 'c4', '易燃', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (255, 47, 5, 'c5', '—', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (256, 48, 0, 'c0', 'MD-02', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (257, 48, 1, 'c1', '原油', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (258, 48, 2, 'c2', 'TK-301', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (259, 48, 3, 'c3', '液体', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (260, 48, 4, 'c4', '可燃', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (261, 48, 5, 'c5', '—', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (262, 49, 0, 'c0', 'MD-03', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (263, 49, 1, 'c1', '液化石油气', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (264, 49, 2, 'c2', 'TK-210', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (265, 49, 3, 'c3', '液化气', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (266, 49, 4, 'c4', '易燃易爆', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (267, 49, 5, 'c5', '—', NULL);
