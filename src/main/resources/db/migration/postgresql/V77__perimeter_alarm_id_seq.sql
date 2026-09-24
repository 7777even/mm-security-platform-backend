-- =============================================================================
-- V77 复位 fac_perimeter_alarm 自增主键序列（PostgreSQL 方言，未实跑验证）
--   同 H2：V29 种子显式插入 id=1,2 不会推进 BIGSERIAL 序列，导致录入 INSERT 主键冲突。
--   setval 把序列当前值设为 MAX(id)，下一次 nextval 即返回 MAX(id)+1。
-- =============================================================================
SELECT setval(
    pg_get_serial_sequence('fac_perimeter_alarm', 'id'),
    COALESCE((SELECT MAX(id) FROM fac_perimeter_alarm), 1)
);
