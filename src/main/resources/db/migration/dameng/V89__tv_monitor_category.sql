-- V87 工业电视监控点位新增监控分类 monitor_category（L4 库结构变更，达梦 DM8 方言）。
--
-- 业务背景：视频概览卡片（生产设施/厂界/封闭入口/其他入口/其它）此前取 fac_tv_stat_item
-- 手填字典值（item_count），与真实监控点位表 fac_tv_monitor 无关联键，导致概览数字无法
-- 下钻到真实点位（"其它=6"在库里对应不出任何具体点位）。
-- 本迁移为 fac_tv_monitor 增加 monitor_category 列，并把现有 15 个种子点位按场所语义打标，
-- 使 TvService.computeOverview 改由 fac_tv_monitor GROUP BY monitor_category 实时计数，
-- 前端按 category 下钻到对应分类的真实监控点位列表（fac_tv_monitor 真实表）。
--
-- 重大危险源概览项仍走 fac_major_hazard（与 GET /hazards 同源），不在此列映射，故本表不设
-- MAJOR_HAZARD 分类，避免与重大危险源实时计数重复/混淆。
--
-- 种子打标仅为演示数据（V24 的 15 个点），语义近似：高空AR/聚焦类归生产设施，厂界门归厂界，
-- 重大危险源装置区监控暂归封闭入口/其他入口/其它以演示五类均可下钻。真实接入后由运维在监控
-- 档案维护中修正 monitor_category，映射不进入任何权限/ABAC 模型。
-- 达梦 VARCHAR 默认以字符为单位，16 足够容纳 CLOSED_GATE（11 字符）。

ALTER TABLE fac_tv_monitor ADD COLUMN monitor_category VARCHAR(16);

UPDATE fac_tv_monitor SET monitor_category = 'BOUNDARY'   WHERE monitor_code IN ('boundary-01', 'boundary-02', 'boundary-03', 'boundary-04');
UPDATE fac_tv_monitor SET monitor_category = 'PRODUCTION'  WHERE monitor_code IN ('ar-01', 'ar-02', 'ar-03', 'focus-01', 'focus-02', 'focus-03', 'focus-04');
UPDATE fac_tv_monitor SET monitor_category = 'CLOSED_GATE' WHERE monitor_code IN ('hazard-01', 'hazard-02');
UPDATE fac_tv_monitor SET monitor_category = 'OTHER_GATE'  WHERE monitor_code IN ('hazard-03');
UPDATE fac_tv_monitor SET monitor_category = 'OTHER'       WHERE monitor_code IN ('hazard-04');

CREATE INDEX idx_tv_monitor_category ON fac_tv_monitor (monitor_category);
