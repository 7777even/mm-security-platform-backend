-- =============================================================================
-- V46 数据权限：补全 fac_device 中文防区词至 sys_zone 权威表（达梦 DM8 方言）
--   与 h2/V46 语义完全一致，仅翻译方言：达梦 Oracle 兼容语法下多行 VALUES 拆为
--   逐条 INSERT；列类型沿用现有 sys_zone（VARCHAR2 / INT）。
--   未实跑验证：本机无 DM8 实例、无 docker，待 DM8 环境激活后 flyway 校验。
-- =============================================================================

INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('CHEM_WH',   '危化仓库', 8,  1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('TANK_A',    '罐区A',    9,  1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('TANK_B',    '罐区B',    10, 1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('LOAD_ZONE', '装卸区',   11, 1);
INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES ('UNIT_C',    '装置C',    12, 1);
