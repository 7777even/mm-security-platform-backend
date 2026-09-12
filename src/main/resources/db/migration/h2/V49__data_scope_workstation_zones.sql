-- =============================================================================
-- V49 数据权限 · 工作站域防区词补齐（H2 方言）—— A1 剩余项 ①
--   背景（沿用 V34/V46 既定约定）：sys_user.zone_codes 存**中文 zone_name**，
--         DataScopeHelper 用 qw.in(zone, zones) 直接按中文字符串命中，不经 zone_code 翻译。
--         因此「补防区词」= 把业务表实际出现的中文 zone 值登记进 sys_zone.zone_name。
--   现状：V34 七个（救援队伍 area）+ V46 五个（fac_device.zone）共 12 词；
--        fac_workstation.zone 实际取值 8 个，其中 4 个尚未登记：
--          { 全厂范围, 化工区, 储运区, 公用工程区 }
--        （V2 种子：罐区A、全厂范围；V44 加厚：炼油区、化工区、乙烯区、储运区、
--          公用工程区、码头区、罐区A、全厂范围）
--   本迁移只补词，不改既有行、不加数据范围过滤——工作站当前没有独立列表查询，
--        仅在 DashboardService 聚合里消费，故「补词」与「套过滤」分两步：
--        待工作站出现独立列表端点时，再仿 DeviceService.page() 注入 DataScopeHelper.apply。
--   H2 保留字规避：沿用 zone_code/zone_name/sort_order/status，无裸 type/value/order。
--   方言：仅 H2（与 V46–V48 一致）；达梦 / PG 镜像待两库激活时补，见 R5。
-- =============================================================================

INSERT INTO sys_zone (zone_code, zone_name, sort_order, status) VALUES
  ('ALL_PLANT', '全厂范围',   13, 1),
  ('CHEM',      '化工区',     14, 1),
  ('STORAGE',   '储运区',     15, 1),
  ('UTILITY',   '公用工程区', 16, 1);
