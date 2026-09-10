-- V25: 规范化 3 条 GAS 设备编码（19 位 -> 20 位）
--
-- 缺陷链（2026-09-10 全量端点冒烟发现）：
--   V2 种子误用 19 位编码（FAC2026 + GAS + 1 位区带字母 = 11 + 8 位序号 = 19），
--   而 fac_device.device_code 为 CHAR(20) 主键，H2 存储时以尾随空格补位
--   （列表接口返回 "FAC2026GASA00000001 "）。
--   按真实业务编码（19 位）调 GET /devices/{code} 时，@DeviceCode(20 位) 方法级校验失败，
--   抛 HandlerMethodValidationException —— 该异常曾未被 GlobalExceptionHandler 接住而落 500。
--   其他类型编码（FIREA/FIREB/FLOOD/CCTVA）恰为 20 位，故仅 GAS 三条暴露此问题。
--
-- 编码规范化规则：FAC2026 + 类型区带 + 9 位序号（保持 20 位，不引入空格）。
-- V2 为已进共享环境的不可变迁移，故以本 V25 数据订正；fac_alarm.device_code 为普通列（无外键），
-- 引用同步订正保持一致。

UPDATE fac_device SET device_code = 'FAC2026GASA000000001' WHERE device_code = 'FAC2026GASA00000001';
UPDATE fac_device SET device_code = 'FAC2026GASB000000001' WHERE device_code = 'FAC2026GASB00000001';
UPDATE fac_device SET device_code = 'FAC2026GASC000000001' WHERE device_code = 'FAC2026GASC00000001';

UPDATE fac_alarm SET device_code = 'FAC2026GASA000000001' WHERE device_code = 'FAC2026GASA00000001';
UPDATE fac_alarm SET device_code = 'FAC2026GASB000000001' WHERE device_code = 'FAC2026GASB00000001';
