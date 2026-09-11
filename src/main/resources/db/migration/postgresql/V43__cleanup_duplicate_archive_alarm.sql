-- V43 数据补偿迁移：清理闭环案例（fac_alarm status=3）中的重复归档事件。
-- 成因：V2__seed_data.sql 已插入 'AE-2026-005'（status=3 已归档）；
--   V35__screen_remaining_dataset.sql 做「数据集加厚」时又插入 'AE-2026-101'
--   （同标题 / 同地点 / 同归档时间 / status=3），二者为同一真实事件，
--   导致 /emergency/closed-cases 列表出现两条相同记录。
-- 处置：保留 V35 的 'AE-2026-101'（与 102..107 构成一致序列），删除 V2 遗留的旧重复行。
-- 幂等：同时限定 alarm_id 与 status，重复执行安全。
DELETE FROM fac_alarm WHERE alarm_id = 'AE-2026-005' AND status = 3;
