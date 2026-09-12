-- =============================================================================
-- V46 数据权限：补全 fac_device 中文防区词至 sys_zone 权威表（H2 方言）
--   背景：A1 把 data_scope 行级 ABAC 从救援队伍域扩展到设备域。既定约定
--        （见 V34）是 zone_codes 存中文 zone_name，qw.in(zone, zones) 直接字符串命中，
--        不经英文 zone_code 翻译。fac_device.zone 实际取值为
--        {危化仓库, 罐区A, 罐区B, 装卸区, 装置C}，此前均不在 sys_zone.zone_name，
--        导致无法为设备域分配可见防区（非 ALL 角色 IN(...) 恒空）。
--   处理：仅新增权威防区行（zone_name 严格对齐 fac_device.zone），不改既有行；
--        zone_code 为业务键（过滤实际按 zone_name 命中），取与中文对应的可读编码。
--   H2 保留字规避：沿用 zone_code/zone_name/sort_order/status，无裸 type/value/order。
-- =============================================================================

INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES
  ('CHEM_WH',   '危化仓库', 8,  1),
  ('TANK_A',    '罐区A',    9,  1),
  ('TANK_B',    '罐区B',    10, 1),
  ('LOAD_ZONE', '装卸区',   11, 1),
  ('UNIT_C',    '装置C',    12, 1);
