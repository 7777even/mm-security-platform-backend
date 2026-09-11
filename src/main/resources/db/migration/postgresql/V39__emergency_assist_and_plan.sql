-- =============================================================================
-- V39 应急域数据源（PostgreSQL方言）—— 大屏「前端数据全后端化」P0
--   目的同 h2/V39；本文件未经 PG 实例实跑验证（本地无 PG 实例），按标准 PG 语法编写。
-- =============================================================================

CREATE TABLE fac_emergency_assist_stat (
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(32) NOT NULL,
    stat_value INT NOT NULL DEFAULT 0,
    unit VARCHAR(8) NOT NULL,
    tone VARCHAR(16) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_emergency_plan_catalog (
    id BIGSERIAL PRIMARY KEY,
    plan_code VARCHAR(32) NOT NULL,
    label VARCHAR(32) NOT NULL,
    plan_name VARCHAR(64) NOT NULL,
    can_switch SMALLINT NOT NULL DEFAULT 0,
    is_current SMALLINT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_emergency_plan_detail (
    id BIGSERIAL PRIMARY KEY,
    section_title VARCHAR(32) NOT NULL,
    field_label VARCHAR(32) NOT NULL,
    field_value VARCHAR(128) NOT NULL,
    section_sort INT NOT NULL DEFAULT 0,
    field_sort INT NOT NULL DEFAULT 0
);

INSERT INTO fac_emergency_assist_stat (label, stat_value, unit, tone, sort_no) VALUES
('应急预案', 15, '套', 'blue', 1),
('现场处置卡', 32, '张', 'cyan', 2),
('应急联络人', 18, '人', 'green', 3),
('可用消防水源', 306, '处', 'orange', 4);

INSERT INTO fac_emergency_plan_catalog (plan_code, label, plan_name, can_switch, is_current, sort_no) VALUES
('superior', '上级单位预案', '未启动', 0, 0, 1),
('company', '公司级预案', '茂名石化应急预案', 1, 1, 2),
('branch', '消防救援预案', '乙烯装置消防救援处置方案', 1, 0, 3),
('site', '现场处置方案', '重油加氢装置高危处置方案', 1, 0, 4);

INSERT INTO fac_emergency_plan_detail (section_title, field_label, field_value, section_sort, field_sort) VALUES
('基础信息', '所属组织', '茂名石化应急指挥中心', 1, 1),
('基础信息', '预案编号', 'MM-EPP-2026-001', 1, 2),
('基础信息', '预案名称', '茂名石化综合应急预案', 1, 3),
('基础信息', '预案类别', '综合应急预案', 1, 4),
('基础信息', '预案级别', '公司级', 1, 5),
('基础信息', '风控是否告知周边单位', '是', 1, 6),
('评审信息', '预案评审日期', '2026-03-15', 2, 1),
('评审信息', '预案评审意见', '通过，建议强化夜间联动机制。', 2, 2),
('备案信息', '初次备案日期', '2025-05-06', 3, 1),
('备案信息', '最近备案日期', '2026-03-20', 3, 2),
('备案信息', '备案部门', '市应急管理局', 3, 3),
('备案信息', '备案部门性质', '政府监管部门', 3, 4),
('公布信息', '初次公布日期', '2025-05-20', 4, 1),
('公布信息', '最近公布日期', '2026-03-22', 4, 2),
('评估信息', '是否修订', '未修订', 5, 1),
('评估信息', '最近评估日期', '2026-03-10', 5, 2),
('评估信息', '评估周期', '每6个月', 5, 3),
('评估信息', '评估意见', '整体有效，建议完善跨装置协同演练。', 5, 4);
