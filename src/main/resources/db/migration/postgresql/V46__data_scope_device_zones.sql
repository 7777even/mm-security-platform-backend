-- =============================================================================
-- V46 数据权限：补全 fac_device 中文防区词至 sys_zone 权威表（postgresql 方言）
--   与 h2/V46 语义完全一致；PG 原生支持多行 VALUES。
--   未实跑验证：本机无 PG 实例、无 docker，待 PG 环境激活后 flyway 校验。
-- =============================================================================

INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES
  ('CHEM_WH',   '危化仓库', 8,  1),
  ('TANK_A',    '罐区A',    9,  1),
  ('TANK_B',    '罐区B',    10, 1),
  ('LOAD_ZONE', '装卸区',   11, 1),
  ('UNIT_C',    '装置C',    12, 1);
