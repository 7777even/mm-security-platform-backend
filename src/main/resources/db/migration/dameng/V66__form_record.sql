-- =============================================================================
-- V66 流程填报记录存储（达梦 DM8 方言，Oracle 兼容，未实跑验证）
--   镜像自 h2/V66__form_record.sql。本文件未经达梦实例实跑验证，按 DM8 语法编写，需上环境复核。
--   DM8 不支持 IF NOT EXISTS / ADD COLUMN IF NOT EXISTS；CREATE TABLE 直接建。
--   VARCHAR 统一用 VARCHAR2(n CHAR)；自增主键用 NUMBER(19) IDENTITY(1,1)。
-- =============================================================================

CREATE TABLE fac_form_record (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    form_no VARCHAR2(64 CHAR) NOT NULL,
    form_type VARCHAR2(32 CHAR) NOT NULL,
    title VARCHAR2(255 CHAR),
    reporter VARCHAR2(64 CHAR) NOT NULL,
    department VARCHAR2(64 CHAR),
    fill_at VARCHAR2(32 CHAR),
    detail_json LONGVARCHAR,
    status VARCHAR2(16 CHAR) NOT NULL DEFAULT 'SUBMITTED',
    remark VARCHAR2(512 CHAR),
    version NUMBER(19) NOT NULL DEFAULT 0
);

-- 演示种子（用户填报数据，仅初始化展示用）
INSERT INTO fac_form_record (id, form_no, form_type, title, reporter, department, fill_at, detail_json, status, remark, version)
VALUES (1, 'FR-20260901-0001', '隐患排查', '储罐区防静电接地巡检填报', '张伟', '储运部', '2026-09-01 09:20:00',
        '{"location":"储罐区T-301","level":"一般","measure":"更换接地点并复测","owner":"张伟"}', 'REVIEWED', '已归档。', 0);
INSERT INTO fac_form_record (id, form_no, form_type, title, reporter, department, fill_at, detail_json, status, remark, version)
VALUES (2, 'FR-20260915-0002', '设备巡检', '消防泵房周检填报', '李娜', '消防大队', '2026-09-15 14:05:00',
        '{"deviceNo":"XF-1#","deviceName":"消防泵1#","result":"正常","abnormal":""}', 'SUBMITTED', NULL, 0);
