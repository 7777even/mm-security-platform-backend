-- V61 应急力量明细参考表（达梦 DM8 方言，Oracle 兼容，未实跑验证，镜像 h2/V61）。
-- 为「应急场所 / 医疗机构」两类无真实台账的类别提供运营可维护名单。
CREATE TABLE sys_emergency_strength_item (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    kind VARCHAR2(32 CHAR) NOT NULL,
    name VARCHAR2(128 CHAR) NOT NULL,
    meta VARCHAR2(256 CHAR),
    sort_no NUMBER(9) NOT NULL DEFAULT 0
);

INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('应急场所', '中心控制室前应急集结点', '中心控制室广场 · 可容纳 200 人', 1);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('应急场所', '东门停车场应急集结点', '厂东区 · 可容纳 150 人', 2);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('应急场所', '炼油区紧急疏散集合点', '炼油装置区南侧', 3);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('应急场所', '乙烯区紧急疏散集合点', '乙烯装置区东侧', 4);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('应急场所', '码头区紧急疏散集合点', '液体化工码头后方', 5);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('应急场所', '综合仓库应急物资库', '仓储中心 · 24h 值守', 6);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('医疗机构', '厂区医务室', '综合办公楼 1 层 · 全科门诊', 1);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('医疗机构', '茂名市人民医院（协议）', '厂外约 8km · 三甲 · 绿色通道', 2);
INSERT INTO sys_emergency_strength_item (kind, name, meta, sort_no) VALUES ('医疗机构', '滨海新区人民医院（协议）', '厂外约 12km · 二甲 · 绿色通道', 3);
