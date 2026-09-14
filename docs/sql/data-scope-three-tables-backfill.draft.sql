-- =============================================================================
-- DRAFT · 三核心表 zone 回填（NOT FOR AUTO-APPLY）
--   本文件**不放在** db/migration 下，Flyway 不会自动执行；待产品确认 location→防区
--   归属规则后，由 DBA / 数据任务手动执行（或转写为正式迁移 V51）。
--   前提：V50 已加 zone 列；sys_zone.zone_name 现有 16 词（V34+V46+V49）。
--   约束：回填产出的 zone 值必须是 sys_zone.zone_name 已有词条，禁止自造。
--   风险：回填前若已注入 ABAC 过滤，非 ALL 角色 zone 为空 → 1=0 零可见（R1）。
--         ⇒ 先回填、后注入，顺序不可反。
-- =============================================================================

-- 1) fac_alarm：按 location 自由文本关键词映射到既有防区词
UPDATE fac_alarm
SET zone = CASE
    WHEN location LIKE '%罐区%'     THEN '罐区A'
    WHEN location LIKE '%炼油%'     THEN '炼油区'
    WHEN location LIKE '%化工%'     THEN '化工区'
    WHEN location LIKE '%乙烯%'     THEN '乙烯区'
    WHEN location LIKE '%储运%' OR location LIKE '%装卸%' OR location LIKE '%仓库%'
                                    THEN '储运区'
    WHEN location LIKE '%公用工程%' THEN '公用工程区'
    WHEN location LIKE '%码头%'     THEN '码头区'
    ELSE '全厂范围'
END
WHERE zone IS NULL;

-- 2) fac_video_camera：同上（location 自由文本）
UPDATE fac_video_camera
SET zone = CASE
    WHEN location LIKE '%罐区%'     THEN '罐区A'
    WHEN location LIKE '%炼油%'     THEN '炼油区'
    WHEN location LIKE '%化工%'     THEN '化工区'
    WHEN location LIKE '%乙烯%'     THEN '乙烯区'
    WHEN location LIKE '%储运%' OR location LIKE '%装卸%' OR location LIKE '%仓库%'
                                    THEN '储运区'
    WHEN location LIKE '%公用工程%' THEN '公用工程区'
    WHEN location LIKE '%码头%'     THEN '码头区'
    ELSE '全厂范围'
END
WHERE zone IS NULL;

-- 3) fac_major_hazard：无 location，需产品定「企业/类别/经纬度 → 防区」规则后补充。
--    例（占位，待确认）：按 enterprise 含 '码头' → '码头区'；含 '储运' → '储运区'；
--    其余按坐标网格划分或默认 '全厂范围'。下句仅为结构示意，执行前须替换映射。
-- UPDATE fac_major_hazard
-- SET zone = CASE
--     WHEN enterprise LIKE '%码头%' THEN '码头区'
--     WHEN enterprise LIKE '%储运%' THEN '储运区'
--     ELSE '全厂范围'
-- END
-- WHERE zone IS NULL;

-- 4) 回填后校验：zone 必须全部命中 sys_zone.zone_name（任一 NULL / 未登记即阻断注入）
-- SELECT COUNT(*) FROM fac_alarm WHERE zone IS NULL OR zone NOT IN (SELECT zone_name FROM sys_zone);
-- SELECT COUNT(*) FROM fac_video_camera WHERE zone IS NULL OR zone NOT IN (SELECT zone_name FROM sys_zone);
-- SELECT COUNT(*) FROM fac_major_hazard WHERE zone IS NULL OR zone NOT IN (SELECT zone_name FROM sys_zone);
