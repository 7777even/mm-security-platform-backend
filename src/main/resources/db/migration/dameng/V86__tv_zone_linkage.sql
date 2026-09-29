-- V86 工业电视监控点位/截图与系统防区（sys_zone）建立关联（L4 库结构变更，达梦 DM8 方言）。
-- 同 h2/V86__tv_zone_linkage.sql 语义，仅方言差异：DM8 用 VARCHAR2(32 CHAR)、ALTER 无 ADD COLUMN 关键字。
-- 监控点位防区指派为种子演示映射，按监控点位名称语义指派，后续由运维在监控档案维护中修正。

ALTER TABLE fac_tv_monitor ADD zone_code VARCHAR2(32 CHAR);

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

ALTER TABLE fac_tv_snapshot ADD zone_code VARCHAR2(32 CHAR);

UPDATE fac_tv_snapshot s
SET s.zone_code = (SELECT m.zone_code FROM fac_tv_monitor m WHERE m.monitor_code = s.monitor_code);

CREATE INDEX idx_tv_snapshot_zone ON fac_tv_snapshot (zone_code);
