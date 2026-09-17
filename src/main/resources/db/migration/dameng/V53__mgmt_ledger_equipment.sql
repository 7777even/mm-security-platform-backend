
SET IDENTITY_INSERT mgmt_ledger_meta ON;
-- V53 ④-B 收尾：设备设施管理域「设备」(ef-equipment) 真后端化（达梦 DM8 方言 Oracle 兼容，未实跑验证）
-- 复用 V51 的 mgmt_ledger_meta/row/cell 三表，仅补充数据，无表结构变更。
-- 列定义沿用前端 mgmtMenus 原型页约定（设备设施管理组 → 设备 叶子）。
-- ID 在 V52（meta 19-24 / row 32-49 / cell 157-267）之后顺序延续：meta 25 / row 50-52 / cell 268-285。
-- 注：示例数据为合理默认结构，待产品校准实际设备清册（位号/类别体系）后替换。

-- 元数据
INSERT INTO mgmt_ledger_meta (id, domain_code, title, columns_json, filter_json, sort_no) VALUES (25, 'ef-equipment', '设备管理', '["编号","设备名称","类别","所属装置","位号","状态"]', '[]', 25);
SET IDENTITY_INSERT mgmt_ledger_meta OFF;
SET IDENTITY_INSERT mgmt_ledger_row ON;


-- 数据行
INSERT INTO mgmt_ledger_row (id, domain_code, row_no, sort_no) VALUES (50, 'ef-equipment', 0, 0);
INSERT INTO mgmt_ledger_row (id, domain_code, row_no, sort_no) VALUES (51, 'ef-equipment', 1, 1);
INSERT INTO mgmt_ledger_row (id, domain_code, row_no, sort_no) VALUES (52, 'ef-equipment', 2, 2);
SET IDENTITY_INSERT mgmt_ledger_row OFF;
SET IDENTITY_INSERT mgmt_ledger_cell ON;


-- 单元格（col_key 固定 c0..c5；cell_type: ok/warn/bad 渲染状态标签，NULL 为普通文本）
-- 行 1：循环氢压缩机
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (268, 50, 0, 'c0', 'EQ-118', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (269, 50, 1, 'c1', '循环氢压缩机', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (270, 50, 2, 'c2', '动设备', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (271, 50, 3, 'c3', '加氢裂化', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (272, 50, 4, 'c4', 'C-101', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (273, 50, 5, 'c5', '运行', 'ok');
-- 行 2：进料泵
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (274, 51, 0, 'c0', 'EQ-205', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (275, 51, 1, 'c1', '进料泵', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (276, 51, 2, 'c2', '动设备', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (277, 51, 3, 'c3', '常减压蒸馏', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (278, 51, 4, 'c4', 'P-205', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (279, 51, 5, 'c5', '运行', 'ok');
-- 行 3：塔顶冷凝器（检修中）
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (280, 52, 0, 'c0', 'EQ-330', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (281, 52, 1, 'c1', '塔顶冷凝器', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (282, 52, 2, 'c2', '静设备', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (283, 52, 3, 'c3', '催化裂化', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (284, 52, 4, 'c4', 'E-330', NULL);
INSERT INTO mgmt_ledger_cell (id, row_id, col_index, col_key, cell_text, cell_type) VALUES (285, 52, 5, 'c5', '检修', 'warn');

SET IDENTITY_INSERT mgmt_ledger_cell OFF;
