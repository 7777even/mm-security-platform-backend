-- V55 应急演练域（移动端演练信息/详情）真后端化（PostgreSQL 方言）
-- 新增只读表 fac_drill + fac_drill_task，替代 apps/mobile/data/mock.ts 的 drills 静态数据。
-- 契约：docs/api/drills.openapi.json（GET /drills、GET /drills/{id}）。

CREATE TABLE IF NOT EXISTS fac_drill (
    id BIGSERIAL PRIMARY KEY,
    drill_code VARCHAR(64) NOT NULL,
    name VARCHAR(256) NOT NULL,
    drill_type VARCHAR(32),
    form VARCHAR(32),
    time_range VARCHAR(64),
    place VARCHAR(128),
    status VARCHAR(32),
    departments VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS fac_drill_task (
    id BIGSERIAL PRIMARY KEY,
    drill_id BIGINT NOT NULL,
    name VARCHAR(256) NOT NULL,
    status VARCHAR(32)
);

INSERT INTO fac_drill (id, drill_code, name, drill_type, form, time_range, place, status, departments) VALUES (1, 'YL-008', '储运部液化烃储罐泄漏实战演练', '实战演练', '现场演练', '2026-08-18 09:30-11:30', '储运部 T-301 罐区', '进行中', '应急救援中心、储运部、消防大队、安环部');
INSERT INTO fac_drill (id, drill_code, name, drill_type, form, time_range, place, status, departments) VALUES (2, 'YL-009', '聚丙烯装置火灾桌面推演', '桌面推演', '桌面推演', '2026-08-22 14:00', '应急救援中心会议室', '计划中', '应急救援中心、聚丙烯装置');

INSERT INTO fac_drill_task (id, drill_id, name, status) VALUES (1, 1, '任务 1 · 现场泄漏点确认与上报', '待确认');
INSERT INTO fac_drill_task (id, drill_id, name, status) VALUES (2, 1, '任务 2 · 模拟关断 T-301 进料切断阀', '已提交');
INSERT INTO fac_drill_task (id, drill_id, name, status) VALUES (3, 2, '任务 1 · 研判推演', '未开始');
INSERT INTO fac_drill_task (id, drill_id, name, status) VALUES (4, 2, '任务 2 · 处置推演', '未开始');
