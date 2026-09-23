-- 2026-09-23：防火巡查种子日期改为相对今天，避免前端 EquipmentMonitoring 面板的 TODAY 动态化后
-- 「今日巡查」归零（原种子写死 2026-08-20，与旧写死 TODAY 巧合对齐）。
-- 按迁移运行日的 CURRENT_DATE 计算：08-20→今天 / 08-19→昨天 / 08-18→前天 / 08-17→大前天。
-- 仅匹配原种子日期，生产环境若已写入真实巡查记录不受影响。
UPDATE fac_fire_patrol SET patrol_date = TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD') WHERE patrol_date = '2026-08-20';
UPDATE fac_fire_patrol SET patrol_date = TO_CHAR(CURRENT_DATE - INTERVAL '1 day', 'YYYY-MM-DD') WHERE patrol_date = '2026-08-19';
UPDATE fac_fire_patrol SET patrol_date = TO_CHAR(CURRENT_DATE - INTERVAL '2 day', 'YYYY-MM-DD') WHERE patrol_date = '2026-08-18';
UPDATE fac_fire_patrol SET patrol_date = TO_CHAR(CURRENT_DATE - INTERVAL '3 day', 'YYYY-MM-DD') WHERE patrol_date = '2026-08-17';
