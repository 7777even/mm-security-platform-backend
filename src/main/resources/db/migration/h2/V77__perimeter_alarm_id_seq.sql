-- =============================================================================
-- V77 复位 fac_perimeter_alarm 自增主键序列（H2 方言）
--   根因：V29 种子显式写入 id=1,2，但 H2 的 AUTO_INCREMENT(BIGINT) 在「显式插入自增列」
--         时不会自动推进内部序列；首个无 id 的录入 INSERT 取到 id=1，与种子行主键冲突
--         （H2 错误码 23505 → DataIntegrityViolationException → 端点返回 409
--          POST /api/v1/security/perimeter-alarms）。
--   修复：将自增序列复位到 MAX(id)+1。当前种子固定 2 行（id=1,2），故复位为 3；
--         若后续任何 V 文件向本表新增种子行，须同步上调此值（= 新 MAX(id)+1）。
-- =============================================================================
ALTER TABLE fac_perimeter_alarm ALTER COLUMN id RESTART WITH 3;
