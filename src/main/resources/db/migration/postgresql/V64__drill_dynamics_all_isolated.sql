-- V64 事故救援「响应动态」按事件隔离（续）：为其余演练事件 12-16 各建
-- fac_accident_incident 聚合行 + 演练专属动态，使每个演练事件在 /accident/rescue-incident
-- 下展示各自独立的响应动态，彼此不共用、也不回退默认事件动态。
-- 演练事件 11 的隔离由 V63 完成；本迁移补齐 12-16。
-- 仅承载演练专属动态；调度资源/值班/辅助统计仍走全局参考主数据（与 V63 一致）。
-- 动态按现有全局参考主数据（V12）与演练事件 11（V63）风格编写；incident_id 经子查询引用刚插入行。
-- 所有 INSERT 均为单条，达梦 DM8 亦可直接复用本文件（无多行 VALUES）。

-- 演练事件 12：装置区疏散演练（东厂区-A装置，已结束）
INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (12, '装置区疏散演练', '东厂区-A装置', 110.882102, 21.683203, NULL, '演练结束', '2026-03-15 14:30:00', '2026-03-15 16:00:00', 'done', TRUE, '东厂区-A装置', FALSE);
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-开始疏散', '【演练指令】', '2026-03-15 14:30:00', '指令内容：导调组宣布演练开始，A装置区拉响疏散警报，启动人员疏散预案。', '演练导调组', '【演练已回复】：演练已启动，进入疏散阶段。', '1. 演练接警', 1, (SELECT id FROM fac_accident_incident WHERE event_id = 12));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练指令-分区撤离', '【演练指令】', '2026-03-15 14:33:20', '指令内容：各楼层引导员按既定路线引导参演人员向集结点撤离，注意防踩踏。', '警戒演练组', '【演练已回复】：分区撤离展开。', '2. 演练响应', 2, (SELECT id FROM fac_accident_incident WHERE event_id = 12));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-清点人数', '【演练指令】', '2026-03-15 14:38:10', '指令内容：集结点核对各组人数，确认无遗漏，记录未到位人员。', '人员清点组', '【演练已回复】：人数核对中。', NULL, 3, (SELECT id FROM fac_accident_incident WHERE event_id = 12));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-模拟搜救', '【演练指令】', '2026-03-15 14:42:00', '指令内容：搜救组对A装置区开展模拟搜救，确认无滞留人员。', '搜救演练组', '【演练已回复】：搜救完成，无滞留。', '3. 演练搜救', 4, (SELECT id FROM fac_accident_incident WHERE event_id = 12));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-疏散完成', '【快讯】', '2026-03-15 14:46:30', '全体参演人员安全撤离至集结点，疏散用时16分钟，达到演练预期。', '演练指挥部', '【已发布】', NULL, 5, (SELECT id FROM fac_accident_incident WHERE event_id = 12));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-人员', '【快报】', '2026-03-15 14:50:00', '人员定位系统显示参演人员全部在位，无失联，疏散演练达到预期。', '人员定位系统', '【已同步】', NULL, 6, (SELECT id FROM fac_accident_incident WHERE event_id = 12));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-环境', '【快报】', '2026-03-15 14:55:00', '演练区域监测正常，无模拟次生风险，环境态势平稳。', '环境监测组', '【已同步】', NULL, 7, (SELECT id FROM fac_accident_incident WHERE event_id = 12));

-- 演练事件 13：危化品泄漏演练（装卸区东侧，待演练）
INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (13, '危化品泄漏演练', '装卸区东侧', 110.884270, 21.679168, NULL, '待演练', '2026-03-14 09:00:00', NULL, 'pending', FALSE, '装卸区东侧', FALSE);
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-接警研判', '【演练指令】', '2026-03-14 09:00:00', '指令内容：模拟装卸区东侧危化品泄漏报警，导调组宣布演练开始并研判险情。', '演练导调组', '【演练已回复】：演练开始，进入研判。', '1. 演练接警', 1, (SELECT id FROM fac_accident_incident WHERE event_id = 13));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练指令-警戒隔离', '【演练指令】', '2026-03-14 09:04:30', '指令内容：警戒组封锁泄漏区周边，设置警戒带，禁止无关人员与车辆进入。', '警戒演练组', '【演练已回复】：警戒已布设。', '2. 演练响应', 2, (SELECT id FROM fac_accident_incident WHERE event_id = 13));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-模拟堵漏', '【演练指令】', '2026-03-14 09:10:15', '指令内容：工艺处置组穿戴防化服执行模拟堵漏，控制泄漏源。', '工艺处置组', '【演练已回复】：堵漏演练展开。', NULL, 3, (SELECT id FROM fac_accident_incident WHERE event_id = 13));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-洗消', '【演练指令】', '2026-03-14 09:18:40', '指令内容：洗消组对受污染区域开展模拟洗消，防止污染物扩散。', '洗消演练组', '【演练已回复】：洗消展开。', NULL, 4, (SELECT id FROM fac_accident_incident WHERE event_id = 13));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-环境监测', '【演练指令】', '2026-03-14 09:25:00', '指令内容：环境监测组对下风向布点采样，评估扩散风险。', '环境监测组', '【演练已回复】：监测布点完成。', '3. 演练处置', 5, (SELECT id FROM fac_accident_incident WHERE event_id = 13));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-处置', '【快讯】', '2026-03-14 09:32:00', '模拟泄漏源已控制，洗消完成，演练区域态势平稳。', '演练指挥部', '【已发布】', NULL, 6, (SELECT id FROM fac_accident_incident WHERE event_id = 13));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-浓度', '【快报】', '2026-03-14 09:38:00', '监测显示模拟可燃气体浓度下降，环境风险可控。', '监测中心', '【已同步】', NULL, 7, (SELECT id FROM fac_accident_incident WHERE event_id = 13));

-- 演练事件 14：夜间消防联动演练（消防泵房北侧，演练进行中）
INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (14, '夜间消防联动演练', '消防泵房北侧', 110.877191, 21.680255, NULL, '演练处置', '2026-03-13 20:00:00', NULL, 'processing', TRUE, '消防泵房北侧', FALSE);
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-夜间拉动', '【演练指令】', '2026-03-13 20:00:00', '指令内容：导调组下达夜间演练拉动指令，检验暗光条件下响应能力。', '演练导调组', '【演练已回复】：夜间拉动开始。', '1. 演练接警', 1, (SELECT id FROM fac_accident_incident WHERE event_id = 14));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练指令-力量集结点名', '【演练指令】', '2026-03-13 20:05:00', '指令内容：各消防单元夜间集结，核对人员装备到位情况。', '消防演练组', '【演练已回复】：集结点名完成。', '2. 演练响应', 2, (SELECT id FROM fac_accident_incident WHERE event_id = 14));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-夜间出水', '【演练指令】', '2026-03-13 20:12:30', '指令内容：消防单元模拟夜间火点出水扑救，验证照明与单元协同。', '消防一中队', '【演练已回复】：夜间出水演练展开。', NULL, 3, (SELECT id FROM fac_accident_incident WHERE event_id = 14));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-联动供水', '【演练指令】', '2026-03-13 20:20:00', '指令内容：测试消防管网联动供水，保障持续供水能力。', '供水保障组', '【演练已回复】：联动供水测试。', NULL, 4, (SELECT id FROM fac_accident_incident WHERE event_id = 14));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-通信保障', '【演练指令】', '2026-03-13 20:28:00', '指令内容：通信组保障夜间演练通信畅通，核实链路稳定性。', '通信保障组', '【演练已回复】：通信链路正常。', '3. 演练处置', 5, (SELECT id FROM fac_accident_incident WHERE event_id = 14));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-进展', '【快讯】', '2026-03-13 20:35:00', '夜间扑救演练推进有序，照明与供水满足要求。', '演练指挥部', '【已发布】', NULL, 6, (SELECT id FROM fac_accident_incident WHERE event_id = 14));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-照明', '【快报】', '2026-03-13 20:42:00', '演练区域照明充足，各单元可视协同良好。', '监测中心', '【已同步】', NULL, 7, (SELECT id FROM fac_accident_incident WHERE event_id = 14));

-- 演练事件 15：危化品事故综合演练（化工区中央大道，已结束）
INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (15, '危化品事故综合演练', '化工区中央大道', 110.880751, 21.682309, NULL, '演练结束', '2026-03-12 10:30:00', '2026-03-12 12:00:00', 'done', TRUE, '化工区中央大道', FALSE);
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-综合启动', '【演练指令】', '2026-03-12 10:30:00', '指令内容：多部门协同综合演练启动，导调组统筹指挥。', '演练导调组', '【演练已回复】：综合演练启动。', '1. 演练接警', 1, (SELECT id FROM fac_accident_incident WHERE event_id = 15));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练指令-分级响应', '【演练指令】', '2026-03-12 10:34:00', '指令内容：按预案启动分级响应，消防、工艺、医疗、环保联动处置。', '演练指挥部', '【演练已回复】：分级响应已启动。', '2. 综合响应', 2, (SELECT id FROM fac_accident_incident WHERE event_id = 15));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-灭火', '【演练指令】', '2026-03-12 10:40:00', '指令内容：消防力量展开模拟灭火，控制火情蔓延。', '消防演练组', '【演练已回复】：灭火演练展开。', NULL, 3, (SELECT id FROM fac_accident_incident WHERE event_id = 15));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-伤员救护', '【演练指令】', '2026-03-12 10:48:00', '指令内容：医疗组对模拟伤员实施急救与转运。', '医疗救护组', '【演练已回复】：伤员救护展开。', NULL, 4, (SELECT id FROM fac_accident_incident WHERE event_id = 15));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-环保联动', '【演练指令】', '2026-03-12 10:55:00', '指令内容：环保组监测水体与大气，防范次生污染。', '环保演练组', '【演练已回复】：环保监测联动。', '3. 综合处置', 5, (SELECT id FROM fac_accident_incident WHERE event_id = 15));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-收尾', '【快讯】', '2026-03-12 11:05:00', '综合演练科目全部完成，多部门协同顺畅。', '演练指挥部', '【已发布】', NULL, 6, (SELECT id FROM fac_accident_incident WHERE event_id = 15));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-总体', '【快报】', '2026-03-12 11:15:00', '演练总体态势平稳，达到综合处置预期。', '监测中心', '【已同步】', NULL, 7, (SELECT id FROM fac_accident_incident WHERE event_id = 15));

-- 演练事件 16：港区溢油应急演练（水东港区码头，待演练）
INSERT INTO fac_accident_incident (event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)
VALUES (16, '港区溢油应急演练', '水东港区码头', 110.885171, 21.678564, NULL, '待演练', '2026-03-11 15:00:00', NULL, 'pending', FALSE, '水东港区码头', FALSE);
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-溢油报警', '【演练指令】', '2026-03-11 15:00:00', '指令内容：模拟码头油品泄漏溢油报警，导调组宣布演练开始。', '演练导调组', '【演练已回复】：演练开始，进入响应。', '1. 演练接警', 1, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练指令-围控', '【演练指令】', '2026-03-11 15:05:30', '指令内容：抢险组布设围油栏，控制溢油扩散范围。', '抢险演练组', '【演练已回复】：围油栏布设。', '2. 演练响应', 2, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-回收', '【演练指令】', '2026-03-11 15:12:00', '指令内容：收油机模拟回收水面溢油，减少污染。', '回收演练组', '【演练已回复】：溢油回收展开。', NULL, 3, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('rescue', '演练科目-清污', '【演练指令】', '2026-03-11 15:20:00', '指令内容：清污组对岸线开展模拟清污作业。', '清污演练组', '【演练已回复】：岸线清污展开。', NULL, 4, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('command', '演练导调-水上交通管制', '【演练指令】', '2026-03-11 15:28:00', '指令内容：海事协调组实施演练水域交通管制，保障作业安全。', '海事协调组', '【演练已回复】：交通管制实施。', '3. 演练处置', 5, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('brief', '演练快讯-控制', '【快讯】', '2026-03-11 15:35:00', '溢油围控与回收推进，污染范围受控。', '演练指挥部', '【已发布】', NULL, 6, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
INSERT INTO fac_accident_dynamic (category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)
VALUES ('awareness', '演练态势-水质', '【快报】', '2026-03-11 15:42:00', '演练水域监测正常，未检出异常扩散。', '监测中心', '【已同步】', NULL, 7, (SELECT id FROM fac_accident_incident WHERE event_id = 16));
