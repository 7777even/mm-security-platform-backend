-- V63 事故救援「响应动态」按事件隔离：fac_accident_dynamic 增加 incident_id 列，
-- 现有全局参考动态归到默认事件（incident_id=1，对应 event_id=4）；新增演练事件
-- （储罐区消防演练，event_id=11）的 fac_accident_incident 行与演练专属动态，使
-- 演练页与真实事件页各自展示独立动态，不再共用同一份全局参考。
-- 注：仅演练事件 11 编有演练专属动态；其余演练事件（12-16）未建 fac_accident_incident 行，
-- 后端回退默认事件动态（与改造前一致），后续可按需补种子。

ALTER TABLE fac_accident_dynamic ADD COLUMN incident_id BIGINT;
CREATE INDEX idx_fac_accident_dynamic_incident ON fac_accident_dynamic(incident_id);

-- 现有 19 条全局参考动态归到默认事件（incident_id=1）
UPDATE fac_accident_dynamic SET incident_id = 1;

-- 演练事件：储罐区消防演练（event_id=11）的事故救援聚合行（仅承载演练专属动态，
-- 调度资源/值班/辅助统计仍走全局参考主数据）。id 交由自增分配，避免与 V58 已占 id 冲突。
INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (11, '储罐区消防演练', '储罐区B-1', 110.879442, 21.681415, '二级', '演练处置', '2026-03-16 10:00:00', NULL, 'processing', TRUE, '储罐区B-1', FALSE);

-- 演练专属动态（incident_id 经子查询取刚插入的演练事件行，按现有全局参考主数据风格编写）
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-储罐火灾扑救', '【演练指令】', '2026-03-16 10:05:12', '指令内容：演练导调组下令，消防一中队按预案展开储罐区B-1 泡沫灭火演练，注意安全距离与协同配合。', '演练导调组', '【演练已回复】：演练单元已就位，开始扑救演练。', NULL, 1, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-工艺处置', '【演练指令】', '2026-03-16 10:12:30', '指令内容：工艺处置组演练切断储罐区上下游物料，执行模拟退守，验证切断流程熟练度。', '工艺演练组', '【演练已回复】：模拟切断完成，装置进入演练退守状态。', NULL, 2, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-人员疏散', '【演练指令】', '2026-03-16 10:20:05', '指令内容：警戒组引导参演人员按疏散路线撤离至集结点，核实人数。', '警戒演练组', '【演练已回复】：参演人员已撤离至集结点，清点完毕。', NULL, 3, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-接警研判', '【演练指令】', '2026-03-16 10:02:00', '指令内容：导调组宣布演练开始，模拟接警并研判储罐区火情，启动预案。', '演练导调组', '【演练已回复】：演练火情已研判，进入响应。', '1. 演练接警研判', 4, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-应急响应', '【演练指令】', '2026-03-16 10:06:40', '指令内容：下达一分钟应急响应指令，各演练单元展开处置，核实到位情况。', '演练导调组', '【演练已回复】：各单元已进入演练响应。', '2. 一分钟演练响应', 5, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-现场警戒', '【演练指令】', '2026-03-16 10:10:15', '指令内容：封锁储罐区B-1 出入口，引导演练车辆通行，维护现场秩序。', '警戒演练组', '【演练已回复】：演练警戒已布设。', '3. 三分钟演练退守', 6, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-扑救', '【快讯】', '2026-03-16 10:15:00', '演练火情受控，参演单元配合良好，无模拟伤亡，处置流程符合预案。', '演练指挥部', '【已发布】', NULL, 7, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-环境监测', '【快讯】', '2026-03-16 10:22:10', '演练区域下风向模拟监测达标，未检出异常组分，环境风险可控。', '环境监测组', '【已发布】', NULL, 8, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-温度', '【快报】', '2026-03-16 10:18:30', '演练监测显示储罐区温度平稳，模拟可燃气体浓度受控，态势向好。', '监测中心', '【已同步】', NULL, 9, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-人员', '【快报】', '2026-03-16 10:25:33', '参演人员全部在集结点，无失联，疏散演练达到预期。', '人员定位系统', '【已同步】', NULL, 10, (SELECT id FROM fac_accident_incident WHERE event_id = 11));
