-- V86 工业电视监控点位/截图与系统防区（sys_zone）建立关联（L4 库结构变更，H2 方言）。
--
-- 业务背景：工业电视「设备/防区筛选」二级页需要按防区维度过滤监控点位与录像截图。
-- 此前 fac_tv_monitor / fac_tv_snapshot 均无防区字段，只能按 monitor_code 过滤。
-- 本迁移为两张表增加 zone_code，并关联 sys_zone（防区主数据，V34 落地）。
--
-- 监控点位防区指派：种子演示映射，按监控点位名称语义指派（高空AR→乙烯区、储罐/液化烃→罐区、
-- 催化裂化/加氢→炼油区、装卸区→仓储区、厂界门→特勤保障区）。后续由运维在监控档案维护中修正，
-- 映射仅为演示种子数据，不进入任何权限/ABAC 模型。
-- 录像截图 zone_code 由监控点位 zone_code 回填（已存在截图）与采集时回查（新截图）两路保证一致。

ALTER TABLE fac_tv_monitor ADD COLUMN zone_code VARCHAR(32);

UPDATE fac_tv_monitor SET zone_code = 'YIXI'     WHERE monitor_code = 'ar-01';
UPDATE fac_tv_monitor SET zone_code = 'YIXI'     WHERE monitor_code = 'ar-02';
UPDATE fac_tv_monitor SET zone_code = 'YIXI'     WHERE monitor_code = 'ar-03';
UPDATE fac_tv_monitor SET zone_code = 'GUANQU'   WHERE monitor_code = 'focus-01';
UPDATE fac_tv_monitor SET zone_code = 'YIXI'     WHERE monitor_code = 'focus-02';
UPDATE fac_tv_monitor SET zone_code = 'YIXI'     WHERE monitor_code = 'focus-03';
UPDATE fac_tv_monitor SET zone_code = 'CANGCUN'  WHERE monitor_code = 'focus-04';
UPDATE fac_tv_monitor SET zone_code = 'GUANQU'   WHERE monitor_code = 'hazard-01';
UPDATE fac_tv_monitor SET zone_code = 'LIANYOU'  WHERE monitor_code = 'hazard-02';
UPDATE fac_tv_monitor SET zone_code = 'LIANYOU'  WHERE monitor_code = 'hazard-03';
UPDATE fac_tv_monitor SET zone_code = 'GUANQU'   WHERE monitor_code = 'hazard-04';
UPDATE fac_tv_monitor SET zone_code = 'TEQIN'    WHERE monitor_code = 'boundary-01';
UPDATE fac_tv_monitor SET zone_code = 'TEQIN'    WHERE monitor_code = 'boundary-02';
UPDATE fac_tv_monitor SET zone_code = 'TEQIN'    WHERE monitor_code = 'boundary-03';
UPDATE fac_tv_monitor SET zone_code = 'TEQIN'    WHERE monitor_code = 'boundary-04';

CREATE INDEX idx_tv_monitor_zone ON fac_tv_monitor (zone_code);

ALTER TABLE fac_tv_snapshot ADD COLUMN zone_code VARCHAR(32);

-- 已存在截图按 monitor_code 回填 zone_code
UPDATE fac_tv_snapshot s
SET s.zone_code = (SELECT m.zone_code FROM fac_tv_monitor m WHERE m.monitor_code = s.monitor_code);

CREATE INDEX idx_tv_snapshot_zone ON fac_tv_snapshot (zone_code);
