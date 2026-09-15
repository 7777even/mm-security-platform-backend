-- V51 管理台账通用能力（PostgreSQL 方言，未实跑验证）
CREATE TABLE IF NOT EXISTS mgmt_ledger_meta (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(64) NOT NULL,
    title VARCHAR(128) NOT NULL,
    columns_json VARCHAR(4000),
    filter_json VARCHAR(2000),
    sort_no INT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS mgmt_ledger_row (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(64) NOT NULL,
    row_no INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS mgmt_ledger_cell (
    id BIGSERIAL PRIMARY KEY,
    row_id BIGINT NOT NULL,
    col_index INT NOT NULL DEFAULT 0,
    col_key VARCHAR(64),
    cell_text VARCHAR(1024),
    cell_type VARCHAR(8)
);

-- 元数据
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (1, 'alarm-config', '报警规则配置', '["编号","规则名称","报警类型","级别","通知方式","联动动作","启用"]', '[]', 1);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (2, 'chemsafe-db', '危险化学品数据库管理', '["编号","中文名称","英文名称","CAS号","分子式","危险性分类"]', '[]', 2);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (3, 'drill-mgmt', '演练管理', '["编号","计划名称","事件分类","事件类型","内容类型","形式类型","计划时间","状态","报警事件"]', '[]', 3);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (4, 'drill-evaluation', '演练评估管理', '["演练编号","演练名称","评估日期","评估方式","评估方法","综合得分","等级","状态"]', '[]', 4);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (5, 'ef-tank', '储罐', '["编号","储罐名称","类型","容积(m³)","储存介质","所属罐区","状态"]', '[]', 5);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (6, 'ef-tankfarm', '罐区', '["编号","罐区名称","储罐数量","总容积(m³)","主要介质","责任人","启用"]', '[]', 6);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (7, 'ef-unit', '装置', '["编号","装置名称","类型","占地面积(m²)","主要产品","启用"]', '[]', 7);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (8, 'ef-warehouse', '仓库', '["编号","仓库名称","类型","所属库区","存储物品","消防配置","启用"]', '[]', 8);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (9, 'ef-warehouse-zone', '库区', '["编号","库区名称","仓库数","主要存储","责任人","启用"]', '[]', 9);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (10, 'enterprise-basic', '企业基本信息管理', '["项目","内容"]', '[]', 10);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (11, 'fire-rescue-plan', '消防救援预案管理', '["预案编号","预案名称","适用部位","编制单位","版本","启用"]', '[]', 11);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (12, 'flood-point', '厂区易涝点管理', '["编号","名称","所属区域","具体位置","风险等级","状态","责任人"]', '[]', 12);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (13, 'media-fire-params', '介质消防参数管理', '["介质名称","闪点(℃)","燃点(℃)","爆炸下限(%)","灭火剂推荐","备注"]', '[]', 13);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (14, 'org-mgmt', '组织管理', '["组织名称","编码","上级组织","类型","启用"]', '[]', 14);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (15, 'prod-emergency', '生产应急资料管理', '["资料名称","类别","所属装置","上传人","上传时间","状态"]', '[]', 15);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (16, 'training-mgmt', '消防培训与学习管理', '["课程名称","类别","学时","参训人数","最近开课","状态"]', '[]', 16);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (17, 'water-system', '消防水系统管理', '["编号","水源名称","类型","容量(m³)","位置","水位状态","启用"]', '[]', 17);
INSERT INTO mgmt_ledger_meta (id, domain, title, columns_json, filter_json, sort_no) VALUES (18, 'broadcast-template', '广播模板管理', '["模板名称","内容类型","适用场景","内容摘要"]', '[]', 18);

-- 数据行
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (1, 'alarm-config', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (2, 'alarm-config', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (3, 'alarm-config', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (4, 'chemsafe-db', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (5, 'drill-mgmt', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (6, 'drill-evaluation', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (7, 'ef-tank', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (8, 'ef-tankfarm', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (9, 'ef-unit', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (10, 'ef-warehouse', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (11, 'ef-warehouse-zone', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (12, 'fire-rescue-plan', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (13, 'flood-point', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (14, 'media-fire-params', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (15, 'org-mgmt', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (16, 'org-mgmt', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (17, 'prod-emergency', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (18, 'training-mgmt', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (19, 'water-system', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (20, 'water-system', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (21, 'broadcast-template', 0, 0);

-- 单元格
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (1, 1, 0, 'c0', 'R-001', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (2, 1, 1, 'c1', 'FAS 一级火警', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (3, 1, 2, 'c2', '消防报警', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (4, 1, 3, 'c3', '一级', 'bad');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (5, 1, 4, 'c4', 'APP+短信', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (6, 1, 5, 'c5', '推送+派单', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (7, 1, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (8, 2, 0, 'c0', 'R-002', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (9, 2, 1, 'c1', 'GDS 高报', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (10, 2, 2, 'c2', '气体报警', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (11, 2, 3, 'c3', '一级', 'bad');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (12, 2, 4, 'c4', 'APP+电话', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (13, 2, 5, 'c5', '推送+视频联动', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (14, 2, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (15, 3, 0, 'c0', 'R-003', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (16, 3, 1, 'c1', '周界入侵', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (17, 3, 2, 'c2', '安防报警', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (18, 3, 3, 'c3', '二级', 'warn');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (19, 3, 4, 'c4', 'APP', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (20, 3, 5, 'c5', '推送+弹窗', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (21, 3, 6, 'c6', '否', 'warn');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (22, 4, 0, 'c0', 'HX-001', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (23, 4, 1, 'c1', '汽油', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (24, 4, 2, 'c2', 'Gasoline', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (25, 4, 3, 'c3', '86290-81-5', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (26, 4, 4, 'c4', 'C5-C12', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (27, 4, 5, 'c5', '易燃液体', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (28, 5, 0, 'c0', 'DL-2026-08', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (29, 5, 1, 'c1', '罐区泡沫联锁演练', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (30, 5, 2, 'c2', '火灾', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (31, 5, 3, 'c3', '储罐火灾', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (32, 5, 4, 'c4', '综合演练', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (33, 5, 5, 'c5', '实战', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (34, 5, 6, 'c6', '2026-08-28', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (35, 5, 7, 'c7', '待执行', 'warn');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (36, 5, 8, 'c8', '—', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (37, 6, 0, 'c0', 'DL-2026-06', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (38, 6, 1, 'c1', '全流程应急演练', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (39, 6, 2, 'c2', '2026-06-20', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (40, 6, 3, 'c3', '现场评估', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (41, 6, 4, 'c4', '评分表', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (42, 6, 5, 'c5', '86', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (43, 6, 6, 'c6', '良好', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (44, 6, 7, 'c7', '已归档', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (45, 7, 0, 'c0', 'TK-301', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (46, 7, 1, 'c1', 'T-301', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (47, 7, 2, 'c2', '外浮顶', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (48, 7, 3, 'c3', '50000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (49, 7, 4, 'c4', '原油', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (50, 7, 5, 'c5', '原油罐区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (51, 7, 6, 'c6', '在用', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (52, 8, 0, 'c0', 'GQ-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (53, 8, 1, 'c1', '原油罐区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (54, 8, 2, 'c2', '8', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (55, 8, 3, 'c3', '120000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (56, 8, 4, 'c4', '原油', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (57, 8, 5, 'c5', '李主任', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (58, 8, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (59, 9, 0, 'c0', 'ZZ-02', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (60, 9, 1, 'c1', '加氢裂化装置', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (61, 9, 2, 'c2', '生产装置', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (62, 9, 3, 'c3', '42000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (63, 9, 4, 'c4', '柴油/航煤', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (64, 9, 5, 'c5', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (65, 10, 0, 'c0', 'CK-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (66, 10, 1, 'c1', '1#危化品库', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (67, 10, 2, 'c2', '甲类', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (68, 10, 3, 'c3', '危化品库区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (69, 10, 4, 'c4', '溶剂', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (70, 10, 5, 'c5', '喷淋+气体', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (71, 10, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (72, 11, 0, 'c0', 'KQ-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (73, 11, 1, 'c1', '危化品库区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (74, 11, 2, 'c2', '4', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (75, 11, 3, 'c3', '溶剂/添加剂', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (76, 11, 4, 'c4', '钱工', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (77, 11, 5, 'c5', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (78, 12, 0, 'c0', 'FR-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (79, 12, 1, 'c1', 'T-301 罐区灭火救援预案', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (80, 12, 2, 'c2', 'T-301', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (81, 12, 3, 'c3', '消防支队', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (82, 12, 4, 'c4', 'V1.4', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (83, 12, 5, 'c5', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (84, 13, 0, 'c0', 'FLD-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (85, 13, 1, 'c1', '北门低洼段', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (86, 13, 2, 'c2', '北门区域', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (87, 13, 3, 'c3', '北门内侧排水沟', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (88, 13, 4, 'c4', '中', 'warn');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (89, 13, 5, 'c5', '正常', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (90, 13, 6, 'c6', '安保班', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (91, 14, 0, 'c0', '石脑油', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (92, 14, 1, 'c1', '-7', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (93, 14, 2, 'c2', '260', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (94, 14, 3, 'c3', '1.2', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (95, 14, 4, 'c4', '泡沫/干粉', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (96, 14, 5, 'c5', '—', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (97, 15, 0, 'c0', '茂名石化', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (98, 15, 1, 'c1', 'MM', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (99, 15, 2, 'c2', '—', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (100, 15, 3, 'c3', '公司', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (101, 15, 4, 'c4', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (102, 16, 0, 'c0', '储运部', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (103, 16, 1, 'c1', 'CY', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (104, 16, 2, 'c2', '茂名石化', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (105, 16, 3, 'c3', '运行部', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (106, 16, 4, 'c4', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (107, 17, 0, 'c0', '乙烯装置应急处置卡', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (108, 17, 1, 'c1', '处置卡', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (109, 17, 2, 'c2', '乙烯装置', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (110, 17, 3, 'c3', '张工', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (111, 17, 4, 'c4', '2026-08-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (112, 17, 5, 'c5', '有效', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (113, 18, 0, 'c0', '消火栓操作实操', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (114, 18, 1, 'c1', '实操培训', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (115, 18, 2, 'c2', '4', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (116, 18, 3, 'c3', '36', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (117, 18, 4, 'c4', '2026-08-10', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (118, 18, 5, 'c5', '已完成', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (119, 19, 0, 'c0', 'WS-01', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (120, 19, 1, 'c1', '西区消防水池', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (121, 19, 2, 'c2', '消防水池', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (122, 19, 3, 'c3', '5000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (123, 19, 4, 'c4', '西区泵房', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (124, 19, 5, 'c5', '正常', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (125, 19, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (126, 20, 0, 'c0', 'WS-02', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (127, 20, 1, 'c1', '东区消防水罐', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (128, 20, 2, 'c2', '高位水罐', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (129, 20, 3, 'c3', '800', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (130, 20, 4, 'c4', '东区', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (131, 20, 5, 'c5', '偏低', 'warn');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (132, 20, 6, 'c6', '是', 'ok');
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (133, 21, 0, 'c0', '火警疏散提示', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (134, 21, 1, 'c1', '文本', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (135, 21, 2, 'c2', '火灾报警', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (136, 21, 3, 'c3', '请沿疏散指示撤离…', NULL);

-- 企业基本信息（原 form 单记录页 → 键值台账：项目/内容）
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (22, 'enterprise-basic', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (23, 'enterprise-basic', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (24, 'enterprise-basic', 2, 2);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (25, 'enterprise-basic', 3, 3);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (26, 'enterprise-basic', 4, 4);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (27, 'enterprise-basic', 5, 5);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (28, 'enterprise-basic', 6, 6);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (29, 'enterprise-basic', 7, 7);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (30, 'enterprise-basic', 8, 8);
INSERT INTO mgmt_ledger_row (id, domain, row_no, sort_no) VALUES (31, 'enterprise-basic', 9, 9);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (137, 22, 0, 'c0', '单位名称', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (138, 22, 1, 'c1', '中国石油化工股份有限公司茂名分公司', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (139, 23, 0, 'c0', '单位编号', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (140, 23, 1, 'c1', 'MM-SH-001', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (141, 24, 0, 'c0', '单位类别', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (142, 24, 1, 'c1', '生产', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (143, 25, 0, 'c0', '单位地址', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (144, 25, 1, 'c1', '广东省茂名市', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (145, 26, 0, 'c0', '联系方式', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (146, 26, 1, 'c1', '0668-******', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (147, 27, 0, 'c0', '邮政编码', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (148, 27, 1, 'c1', '525000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (149, 28, 0, 'c0', '消防控制室电话', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (150, 28, 1, 'c1', '0668-****24', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (151, 29, 0, 'c0', '职工人数', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (152, 29, 1, 'c1', '12000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (153, 30, 0, 'c0', '占地面积（㎡）', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (154, 30, 1, 'c1', '12800000', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (155, 31, 0, 'c0', '总建筑面积（㎡）', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (156, 31, 1, 'c1', '3200000', NULL);
