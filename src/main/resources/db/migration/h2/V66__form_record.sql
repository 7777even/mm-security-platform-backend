-- =============================================================================
-- V66 流程填报记录存储（H2 方言）
--   解冻 mgmt /form 流程填报向导：新建 fac_form_record 表，供前端列表 + 多步填报向导读写。
--   字段范式对齐通讯通知记录域（fac_comm_record）：业务编号 / 类型 / 标题 / 填报人 / 部门 /
--   时间(VARCHAR 原样) / 结构化内容(detail_json) / 状态 / 备注。id 自增主键，version 乐观锁。
--   填报类型 form_type 固定枚举：隐患排查 / 设备巡检 / 值班交接 / 其他。
--   状态：DRAFT 草稿 / SUBMITTED 已提交 / REVIEWED 已审核。
-- =============================================================================

CREATE TABLE fac_form_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_no VARCHAR(64) NOT NULL,
    form_type VARCHAR(32) NOT NULL,
    title VARCHAR(255),
    reporter VARCHAR(64) NOT NULL,
    department VARCHAR(64),
    fill_at VARCHAR(32),
    detail_json LONGVARCHAR,
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
