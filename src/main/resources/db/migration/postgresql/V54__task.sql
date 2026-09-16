-- V54 处置任务域（移动端任务中心）真后端化（PostgreSQL 方言）
-- 新增只读表 fac_dispatch_task，替代 apps/mobile/data/mock.ts 的 tasks 静态数据。
-- 契约：docs/api/tasks.openapi.json（GET /tasks、GET /tasks/{id}）。
-- 列名用 task_level 而非 level：LEVEL 在达梦/Oracle 为保留字（规避方言差异）。

CREATE TABLE IF NOT EXISTS fac_dispatch_task (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(64) NOT NULL,
    title VARCHAR(256) NOT NULL,
    task_level VARCHAR(16),
    source VARCHAR(64),
    area VARCHAR(128),
    deadline VARCHAR(64),
    status VARCHAR(32),
    description VARCHAR(1024)
);

INSERT INTO fac_dispatch_task (id, task_code, title, task_level, source, area, deadline, status, description) VALUES (1, 'TASK-001', '储运部液化烃储罐现场处置', '紧急', '后台派发', '储运部 T-301', '2026-08-18 11:30', '待接收', '前往 T-301 罐区核实泄漏点，反馈现场情况。');
INSERT INTO fac_dispatch_task (id, task_code, title, task_level, source, area, deadline, status, description) VALUES (2, 'TASK-002', '乙烯装置区例行巡查', '一般', '巡查计划', '乙烯装置区', '2026-08-18 17:00', '执行中', '按巡查路线执行防火巡查。');
INSERT INTO fac_dispatch_task (id, task_code, title, task_level, source, area, deadline, status, description) VALUES (3, 'TASK-003', '机柜间A气体灭火故障维修', '重要', '故障工单', '电仪中心机柜间A', '2026-08-19 12:00', '已签收', '排查气体灭火系统故障并完成维修。');
INSERT INTO fac_dispatch_task (id, task_code, title, task_level, source, area, deadline, status, description) VALUES (4, 'TASK-004', '视频监控点位校时', '一般', '后台派发', '中控室', '2026-08-20 09:00', '待接收', '对厂区视频监控点位进行时钟校准。');
