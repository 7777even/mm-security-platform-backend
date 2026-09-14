-- =============================================================================
-- V50 数据权限 · 三核心表补 zone 列（H2 方言）—— A1 剩余项 ②（schema 预备）
--   背景（沿用 V34/V46/V49 既定约定）：sys_user.zone_codes 存**中文 zone_name**，
--         DataScopeHelper 用 qw.in(zone, zones) 直接按中文字符串命中，不经 zone_code 翻译。
--   三表现状：
--     · fac_alarm        仅有 location 自由文本（V1），无防区列；
--     · fac_video_camera 仅有 location 自由文本（V14），无防区列；
--     · fac_major_hazard 连 location 都没有（V3，仅经纬度 longitude/latitude）。
--   本迁移只加可空 zone 列，不改既有行、不注入 ABAC 过滤——
--     避免非 ALL 角色 zone 为空被 DataScopeHelper 拼成 1=0 → 零可见（R1 回归，比越权更严重）。
--   回填（location/坐标 → zone 中文值）与 Service 层 DataScopeHelper.apply 注入，
--     待产品定 location→防区 归属规则后再做（见 docs/data-scope-three-tables-backfill.md）。
--   编码对齐：回填产出的 zone 值必须落在 sys_zone.zone_name 现有 16 词内（V34+V46+V49），
--     不得自造未登记词条（否则套过滤后同样查不到）。
--   方言：仅 H2（与 V46–V49 一致）；达梦 / PG 镜像待两库激活时补（R5）。
-- =============================================================================

ALTER TABLE fac_alarm ADD COLUMN zone VARCHAR(64);
ALTER TABLE fac_video_camera ADD COLUMN zone VARCHAR(64);
ALTER TABLE fac_major_hazard ADD COLUMN zone VARCHAR(64);
