-- V6 现场采集回传落库（防爆手机离线队列上行）
-- 此前 UplinkService.submitFieldReport 仅受理确认（返回 204），本期收口为「受理即落库」，
-- 供应急复盘/核查。id 由客户端 UUID 生成，用 INPUT（业务侧显式赋值，非自增）。
CREATE TABLE fac_field_report (
    id           VARCHAR(64) PRIMARY KEY,
    kind         VARCHAR(32),
    title        VARCHAR(256),
    note         VARCHAR(512),
    device_code  VARCHAR(32),
    media_json   VARCHAR(4000),
    created_at   BIGINT,
    status       VARCHAR(16),
    reporter     VARCHAR(64),
    attempts     INT DEFAULT 0,
    last_error   VARCHAR(512),
    synced_at    BIGINT
);

-- 种子：2 条已落库回传，供联调验证真实落库（reporter 仅为演示值，真实受理时由服务端按登录态覆盖）
INSERT INTO fac_field_report (id, kind, title, note, device_code, media_json, created_at, status, reporter, attempts, last_error, synced_at) VALUES
 ('r-seed-001', 'field-report', 'A2 区火情处置回传', '现场明火已控制', 'DT-A-3012', NULL, 1717488000000, 'done', 'zhang.san', 1, NULL, 1717488060000),
 ('r-seed-002', 'task-ack', 'B3 区泄漏任务确认', NULL, NULL, NULL, 1717491600000, 'done', 'li.si', 2, NULL, 1717491660000);
