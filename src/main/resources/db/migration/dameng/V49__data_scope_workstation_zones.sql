-- =============================================================================
-- V49 数据权限 · 工作站域防区词补齐（达梦 DM8 方言）—— 与 h2/V49 语义完全一致
--   方言翻译：多行 VALUES 拆逐条 INSERT。
--   未实跑验证：本机无 DM8 实例、无 docker，待 DM8 环境激活后 flyway 校验。
-- =============================================================================

INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('ALL_PLANT', '全厂范围',   13, 1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('CHEM',      '化工区',     14, 1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('STORAGE',   '储运区',     15, 1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('UTILITY',   '公用工程区', 16, 1);
