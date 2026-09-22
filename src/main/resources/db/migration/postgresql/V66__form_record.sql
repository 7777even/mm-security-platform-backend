-- =============================================================================
-- V66 流程填报记录存储（PostgreSQL 方言，未实跑验证）
--   镜像自 h2/V66__form_record.sql。本文件未经 PostgreSQL 实例实跑验证，按标准 PG 语法编写，需上环境复核。
-- =============================================================================

CREATE TABLE IF NOT EXISTS fac_form_record (
    id BIGSERIAL PRIMARY KEY,
    form_no VARCHAR(64) NOT NULL,
    form_type VARCHAR(32) NOT NULL,
    title VARCHAR(255),
    reporter VARCHAR(64) NOT NULL,
    department VARCHAR(64),
    fill_at VARCHAR(32),
    detail_json TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED',
    remark VARCHAR(512),
    version BIGINT NOT NULL DEFAULT 0
);

-- 演示种子（用户填报数据，仅初始化展示用）
INSERT INTO fac_form_record (id, form_no, form_type, title, reporter, department, fill_at, detail_json, status, remark, version)
VALUES (1, 'FR-20260901-0001', '隐患排查', '储罐区防静电接地巡检填报', '张伟', '储运部', '2026-09-01 09:20:00',
        '{"location":"储罐区T-301","level":"一般","measure":"更换接地点并复测","owner":"张伟"}', 'REVIEWED', '已归档。', 0);
INSERT INTO fac_form_record (id, form_no, form_type, title, reporter, department, fill_at, detail_json, status, remark, version)
VALUES (2, 'FR-20260915-0002', '设备巡检', '消防泵房周检填报', '李娜', '消防大队', '2026-09-15 14:05:00',
        '{"deviceNo":"XF-1#","deviceName":"消防泵1#","result":"正常","abnormal":""}', 'SUBMITTED', NULL, 0);
