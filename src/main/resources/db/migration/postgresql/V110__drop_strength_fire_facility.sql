-- 应急指挥「应急救援力量」不再聚合消防设施台账：消防设施属于消防域（fire-facility），
-- 不应作为应急救援力量类别混入应急指挥大屏。删除 sys_emergency_strength 字典中的「消防设施」种子行，
-- 使 GET /api/v1/emergency/strength 返回的类别均属应急指挥域。
-- H2 / PostgreSQL / 达梦 DM8 通用 SQL；DELETE 幂等，缺行不影响。
DELETE FROM sys_emergency_strength WHERE kind = '消防设施';
