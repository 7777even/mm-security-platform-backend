-- =============================================================================
-- V71 修正 fac_fire_facility_monitor 种子 offline_count 与 total/online 不一致（达梦 DM8 方言）
--   V20 种子中 5 行 online_count + offline_count != total_count（各差 1）：
--     water(消防水源)           44+1=45 ≠ 46
--     sprinkler(自动喷水灭火系统) 62+1=63 ≠ 64
--     gas(气体灭火系统)         37+0=37 ≠ 38
--     smoke(防烟排烟系统)       50+1=51 ≠ 52
--     power(消防电源)           93+2=95 ≠ 96
--   令 offline_count = total_count - online_count，使离线数准确；983 台总数不变。
--   影响：GET /fire/equipment-status 与 /fire-facility/monitors 的 offline 由 15 修正为 20，
--         onlineRate/integrityRate 不变（四舍五入后一致）。
-- =============================================================================

UPDATE fac_fire_facility_monitor
SET offline_count = total_count - online_count
WHERE key_code IN ('water', 'sprinkler', 'gas', 'smoke', 'power');
