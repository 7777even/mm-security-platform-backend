-- =============================================================================
-- V6 现场上报域 —— 达梦 DM8 方言（Oracle 兼容，未实跑验证）
--   镜像自 h2/V6__field_report.sql。本文件未经达梦实例实跑验证，按 DM8 语法编写，需上环境复核。
-- =============================================================================

-- V6 现场采集回传落库（防爆手机离线队列上行）
-- 此前 UplinkService.submitFieldReport 仅受理确认（返回 204），本期收口为「受理即落库」，
-- 供应急复盘/核查。id 由客户端 UUID 生成，用 INPUT（业务侧显式赋值，非自增）。
CREATE TABLE fac_field_report (
    id           VARCHAR2(64) PRIMARY KEY,
    kind         VARCHAR2(32),
    title        VARCHAR2(256),
    note         VARCHAR2(512),
    device_code  VARCHAR2(32),
    media_json   VARCHAR2(4000),
    created_at   NUMBER(19),
    status       VARCHAR2(16),
    reporter     VARCHAR2(64),
    attempts     NUMBER(9) DEFAULT 0,
    last_error   VARCHAR2(512),
    synced_at    NUMBER(19)
);


-- 种子：2 条已落库回传，供联调验证真实落库（reporter 仅为演示值，真实受理时由服务端按登录态覆盖）
INSERT INTO fac_field_report (id, kind, title, note, device_code, media_json, created_at, status, reporter, attempts, last_error, synced_at) VALUES ('r-seed-001', 'field-report', 'A2 区火情处置回传', '现场明火已控制', 'DT-A-3012', NULL, 1717488000000, 'done', 'zhang.san', 1, NULL, 1717488060000)
INSERT INTO fac_field_report (id, kind, title, note, device_code, media_json, created_at, status, reporter, attempts, last_error, synced_at) VALUES ('r-seed-002', 'task-ack', 'B3 区泄漏任务确认', NULL, NULL, NULL, 1717491600000, 'done', 'li.si', 2, NULL, 1717491660000);
