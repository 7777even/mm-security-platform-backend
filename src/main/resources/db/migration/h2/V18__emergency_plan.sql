-- V18 应急预案大屏真实数据源（预案切换面板 + 预案矩阵）。
-- 数据来源：src/screen/lib/data/emergencyPlanSwitchMock.ts（4 个页签、事故类型/装置筛选字典、
-- 7 条预案目录）与 src/screen/lib/data/planMatrixMock.ts（planMatrixPlans 全部 3 个预案实例：
-- plan-t103-002 T103 塔灭火救援预案（默认）、plan-flood-003 防洪防内涝专项预案、
-- plan-maoming-001 化工厂区突发环境事件综合应急预案），迁移后数值与文案语义保持不变。
-- 命名回避数据库保留字（value/command）：计数列 expected_count / actual_count、
-- 正文列 content_text / description_text、状态列 card_status / is_global。
-- 父子表以 *_code 字符串外键关联（instance_id 仅用于按预案实例筛选，插入时由 plan_code 子查询取得）。

CREATE TABLE fac_emergency_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tab_key VARCHAR(32) NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    accident_type VARCHAR(32) NOT NULL,
    facility VARCHAR(64) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_plan_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(64) NOT NULL,
    title VARCHAR(256) NOT NULL,
    description VARCHAR(1024) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_plan_major_phase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    phase_code VARCHAR(32) NOT NULL,
    phase_name VARCHAR(128) NOT NULL,
    phase_order INT NOT NULL DEFAULT 0,
    upgrade_process VARCHAR(1024)
);

CREATE TABLE fac_plan_sub_phase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    phase_code VARCHAR(32) NOT NULL,
    parent_code VARCHAR(32) NOT NULL,
    phase_name VARCHAR(128) NOT NULL,
    phase_order INT NOT NULL DEFAULT 0,
    progress INT
);

CREATE TABLE fac_plan_risk_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    event_code VARCHAR(32) NOT NULL,
    sub_phase_code VARCHAR(32) NOT NULL,
    event_name VARCHAR(256) NOT NULL
);

CREATE TABLE fac_plan_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    resource_code VARCHAR(32) NOT NULL,
    resource_name VARCHAR(128) NOT NULL,
    expected_count VARCHAR(32) NOT NULL,
    actual_count VARCHAR(32) NOT NULL,
    leader_name VARCHAR(64),
    contact_phone VARCHAR(64),
    duties VARCHAR(1024) NOT NULL,
    longitude DOUBLE,
    latitude DOUBLE,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_plan_action_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    card_code VARCHAR(32) NOT NULL,
    resource_code VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    content_text VARCHAR(512),
    description_text VARCHAR(512),
    start_sub_phase_code VARCHAR(32) NOT NULL,
    end_sub_phase_code VARCHAR(32) NOT NULL,
    risk_event_code VARCHAR(32),
    card_status VARCHAR(16) NOT NULL,
    is_global BOOLEAN NOT NULL DEFAULT FALSE,
    sort_no INT NOT NULL DEFAULT 0
);

-- 预案切换目录（7 条，页签顺序：disposal / fire / company / superior）
INSERT INTO fac_emergency_plan (tab_key, plan_name, accident_type, facility, sort_no) VALUES
  ('disposal', '乙烯储罐火灾处置方案', '火灾/爆炸', '乙烯罐区', 1),
  ('disposal', '液氨泄漏现场处置方案', '泄漏', '乙烯罐区', 2),
  ('fire', '乙烯装置消防救援处置方案', '火灾/爆炸', '乙烯裂解装置', 3),
  ('fire', '储罐区泡沫灭火救援预案', '火灾/爆炸', '乙烯罐区', 4),
  ('company', '茂名石化应急预案', '火灾/爆炸', '乙烯罐区', 5),
  ('company', '茂名石化综合应急预案（修订版）', '泄漏', '重油加氢装置', 6),
  ('superior', '广东省石化行业应急预案', '火灾/爆炸', '乙烯罐区', 7);

-- 预案实例（3 条，sort_no=1 的 T103 塔灭火救援预案为默认预案）
INSERT INTO fac_plan_instance (plan_code, title, description, sort_no) VALUES
  ('plan-t103-002', '加氢制氢部加氢裂化装置T103塔灭火救援预案',
   '针对加氢裂化装置T103减压分馏塔底泵泄漏着火、流洒火蔓延、邻近设备受热辐射威胁等火灾险情的特种消防扑救预案。', 1),
  ('plan-flood-003', '应急救援中心防洪防内涝应急专项预案',
   '应对厂区降雨量达到150毫米至200毫米状态下，中心各消防中队、机关后勤协同开展挡水防汛与大功率排涝强排的实战响应矩阵。', 2),
  ('plan-maoming-001', '中国石化茂名分公司化工厂区突发环境事件综合应急预案',
   '依据2022年版环境预案编制，实现车间级(Ⅲ级)、分部级(Ⅱ级)和茂名石化社会级(Ⅰ级)的突发环境事件响应联动。', 3);

-- ------------------------------ plan-t103-002 ------------------------------

INSERT INTO fac_plan_major_phase (instance_id, phase_code, phase_name, phase_order, upgrade_process) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'm3_1', '1、班组自救与出动侦察', 1,
   '全勤出动后，接警室与报警人联系了解信息。中队先期到达现场，马上联系工艺人员，30分钟内安排人员侦察、搜救、拉设警戒线并实施控制。若火势扩大或涉及有毒气体扩散，立即呼叫总指挥升级响应。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'm3_2', '2、中队展开与泡沫主攻', 2,
   '消防指挥部成立，辖区中队主战，部署气防车、泡沫车、供液车展开战斗，利用泡沫炮和移动水炮对T103塔塔底及邻近柴油加氢、四催装置进行冷却和扑灭流洒火。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'm3_3', '3、联合总攻与工艺切断', 3,
   '配合车间工艺操作组关闭泄漏阀门并进行氮气置换；上级指挥员到场接管，出动增援泡沫车和重型排涝机器，对着火区域下风向实施水幕覆盖和泡沫消防流洒火。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'm3_4', '4、监护退却与后期恢复', 4,
   '明火扑灭后持续冷却，直到设备温度降至自燃点以下。安全观察组清查现场，确认无余气和零星阴燃后，下达撤退与应急终止指令。');

INSERT INTO fac_plan_sub_phase (instance_id, phase_code, parent_code, phase_name, phase_order, progress) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_1_1', 'm3_1', '接警核实与全勤出动', 1, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_1_2', 'm3_1', '工艺联系与侦察搜救', 2, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_2_1', 'm3_2', '辖区消防力量部署', 3, 85),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_2_2', 'm3_2', '泡沫主攻与流洒火扑灭', 4, 70),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_2_3', 'm3_2', '邻近装置降温隔离防护', 5, 60),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_3_1', 'm3_3', '配合工艺带火紧急关阀', 6, 25),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_3_2', 'm3_3', '增援力量出动与泡沫运输', 7, 10),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_3_3', 'm3_3', '总攻灭火与系统氮气置换', 8, 0),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_4_1', 'm3_4', '设备持续冷却降温', 9, 0),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'sp3_4_2', 'm3_4', '清查防复燃与终止应急', 10, 0);

INSERT INTO fac_plan_risk_event (instance_id, event_code, sub_phase_code, event_name) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 're3_1', 'sp3_2_2', '地面油品流洒火大面积蔓延'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 're3_2', 'sp3_3_1', '泄漏工艺阀门高温受热卡死无法操作'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 're3_3', 'sp3_3_3', 'T101脱丁烷塔或F101加热炉受热开裂发生二次爆炸'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 're3_4', 'sp3_4_1', '设备温度高于储存介质自燃点导致复燃');

INSERT INTO fac_plan_resource (instance_id, resource_code, resource_name, expected_count, actual_count, leader_name, contact_phone, duties, longitude, latitude, sort_no) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'res-t103-1', '火灾救援现场消防总指挥部与通信班组', '5', '5', NULL, NULL,
   '指挥部总指挥由先期到场的责任区中队长担任，审定T103塔及邻近装置火灾攻防方案并组织扑救；上级指挥员到场后移交指挥权并协同调度。', 110.8856, 21.6858, 1),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'res-t103-2', '气防中心专业侦察与突击搜救中队', '5', '5', NULL, NULL,
   '着全封闭重型防护服深入下风向与核心热区检测硫化氢与可燃烃类浓度，对高温浓烟区域实施失联人员与被困职工快速突击搜救。', 110.886, 21.6862, 2),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'res-t103-3', '专职消防中队重型泡沫扑救总攻突击队', '6', '6', NULL, NULL,
   '实施主流洒火扑灭压制总攻；布置多管泡沫管枪从上风向及侧上风向持续覆盖池火与管道泄漏火，防止复燃。', 110.8865, 21.685, 3),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'res-t103-4', '邻近核心装置高压水幕隔离防爆中队', '8', '8', NULL, NULL,
   '紧急部署移动炮与高架水炮，对东侧四催装置及西侧柴油加氢反应器等毗邻装置管道实施360°持续喷淋冷却，筑牢水幕隔离墙以降低热辐射。', 110.885, 21.6845, 4),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'res-t103-5', '加氢制氢部紧急切断与倒罐操作班组', '6', '6', NULL, NULL,
   '执行T103塔底紧急降压与密闭安全倒罐；现场配合消防火场关阀；实施泄漏管线的氮气吹扫与惰性置换。', 110.8842, 21.6852, 5),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'res-t103-6', '防灭减灾室现场风险与水务监督组', '4', '4', NULL, NULL,
   '落实现场供水水压保障并协调增开备用消防稳压泵；全程红外实时扫描监测T103塔体与毗邻反应器温升情况，监督指导侦察与总攻安全。', 110.8858, 21.6838, 6);

INSERT INTO fac_plan_action_card (instance_id, card_code, resource_code, title, content_text, description_text, start_sub_phase_code, end_sub_phase_code, risk_event_code, card_status, is_global, sort_no) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-101', 'res-t103-1', '成立火灾救援现场指挥部并联系工艺人员',
   '成立火灾救援现场指挥部，了解T103塔减压塔底介质参数。', NULL, 'sp3_1_1', 'sp3_1_2', NULL, 'completed', FALSE, 1),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-102', 'res-t103-2', '派员出动气防车进行毒气浓度侦检',
   '穿戴重型防护服在下风向检测烃类和二氧化硫气体，搜救失联人员。', NULL, 'sp3_1_1', 'sp3_1_2', NULL, 'completed', FALSE, 2),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-201', 'res-t103-2', '利用防爆热成像仪对T103塔底泄漏点定位',
   '对减压塔底泵及周围管段进行热成像测温，回传温度数据。', NULL, 'sp3_1_2', 'sp3_2_1', NULL, 'in-progress', FALSE, 3),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-202', 'res-t103-3', '部署泡沫主战消防车喷射泡沫扑灭地表流洒火',
   '调动主战泡沫消防车，利用车载泡沫炮大流量喷射扑灭地表流洒火。', NULL, 'sp3_2_1', 'sp3_2_2', 're3_1', 'in-progress', FALSE, 4),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-203', 'res-t103-4', '使用水炮对四催和柴油加氢邻近装置冷却',
   '设置高位移动水炮，持续对受辐射热严重的四催等邻近设备冷却。', NULL, 'sp3_2_2', 'sp3_2_3', NULL, 'in-progress', FALSE, 5),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-301', 'res-t103-5', '配合工艺人员利用防护屏板掩护，紧急带火关阀',
   '消防突击队穿隔热服，手持喷淋水幕掩护车间工艺工关闭紧急泄放阀。', NULL, 'sp3_3_1', 'sp3_3_1', 're3_2', 'pending', FALSE, 6),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-302', 'res-t103-3', '调集后方泡沫运输车增援，源源不断输送泡沫液',
   '保障室调用3台大型泡沫罐车，往火场核心持续供给高效泡沫混合液。', NULL, 'sp3_3_2', 'sp3_3_3', NULL, 'pending', TRUE, 7),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-303', 'res-t103-5', '往受灾管段吹扫高压氮气防止管道负压回火',
   '装置区工艺隔离完成后，对泄漏减压管段注入0.8MPa氮气吹扫置换。', NULL, 'sp3_3_3', 'sp3_3_3', 're3_3', 'pending', FALSE, 8),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-t103-002'), 'c-t103-401', 'res-t103-6', '明火熄灭后，继续使用水炮降温确保其低于储存自燃点',
   '明火熄灭后不停歇冷却，使用红外测温枪确认温度低于120摄氏度。', NULL, 'sp3_4_1', 'sp3_4_1', 're3_4', 'pending', FALSE, 9);

-- ------------------------------ plan-flood-003 ------------------------------

INSERT INTO fac_plan_major_phase (instance_id, phase_code, phase_name, phase_order, upgrade_process) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'm4_1', '1、预备值守与防汛动员', 1,
   '中心及各中队启动领导带班值守，30分钟内备齐防汛排涝应急物资（照明灯、抽水机、通信器材、救生衣等），并完成车辆排涝泵保养；若降雨量突破50mm且有积水趋势，启动Ⅱ级响应。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'm4_2', '2、厂区防汛与初期排涝', 2,
   '炼油中队根据防汛排涝力量布置图，在6#路与9#路、11#路交界处及各门岗设置挡水板。各中队按网格开展初期排涝。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'm4_3', '3、抢险总攻与大功率排涝', 3,
   '雨量达150-200mm，降雨级别升级。调动特勤中队紧急增援。在高碳装置雨水池污水池部署龙吸水排涝车；在净化水气泵房、新鲜水泵房部署大功率水泵和大水牛排涝机器人，实施满负荷强力抽排。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'm4_4', '4、后期清淤与保障收退', 4,
   '雨势减弱，厂区积水排干。各中队配合车间清理排洪沟泥沙淤积；安全监督员确认电力设施无漏电风险后，清理器材并归建。');

INSERT INTO fac_plan_sub_phase (instance_id, phase_code, parent_code, phase_name, phase_order, progress) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_1_1', 'm4_1', '启动领导带班与预案动员', 1, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_1_2', 'm4_1', '防排涝装备检查维护', 2, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_2_1', 'm4_2', '6#/9#/11#路挡水板安装', 3, 90),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_2_2', 'm4_2', '各中队排水防线预置', 4, 80),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_3_1', 'm4_3', '龙吸水排涝车部署强排', 5, 60),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_3_2', 'm4_3', '大水牛机器人深水排涝', 6, 40),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_3_3', 'm4_3', '地磅北地沟大功率强排', 7, 10),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_4_1', 'm4_4', '排洪主沟泥沙防阻清淤', 8, 0),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'sp4_4_2', 'm4_4', '防触电检测与防线收退', 9, 0);

INSERT INTO fac_plan_risk_event (instance_id, event_code, sub_phase_code, event_name) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 're4_1', 'sp4_2_2', '强降雨沙土流失导致排洪沟堵塞溢流'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 're4_2', 'sp4_3_2', '变电站及水泵房深水淹没区发生线路漏电'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 're4_3', 'sp4_3_3', '强力抽排导致301事故池超负荷漫溢');

INSERT INTO fac_plan_resource (instance_id, resource_code, resource_name, expected_count, actual_count, leader_name, contact_phone, duties, longitude, latitude, sort_no) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'res-flood-1', '应急中心领导与带班室', '5', '5', NULL, NULL,
   '负责该状态下中心所有管理工作的指挥和协调，分派化工组和炼油组。', NULL, NULL, 1),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'res-flood-2', '装备保障物资组', '8', '8', NULL, NULL,
   '准备防汛排涝应急物资如照明灯、抽水泵、通信器材、救生衣等。', NULL, NULL, 2),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'res-flood-3', '安全监督防灾室', '4', '4', NULL, NULL,
   '负责各中队排涝抢险的风险评估识别及现场过程安全监控。', NULL, NULL, 3),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'res-flood-4', '乙烯中队（化工厂区）', '15', '15', NULL, NULL,
   '负责化工装置区域内的所有挡水排涝及初期抢险任务。', NULL, NULL, 4),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'res-flood-5', '炼油中队（炼油厂区）', '20', '20', NULL, NULL,
   '负责炼油区域内应急，在6#路与9#路、11#路交界处及各门岗设置挡水板。', NULL, NULL, 5),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'res-flood-6', '高碳中队（雨排污）', '12', '12', NULL, NULL,
   '负责高碳装置雨污水池排涝，部署和操作大排量“龙吸水”排涝车。', NULL, NULL, 6);

INSERT INTO fac_plan_action_card (instance_id, card_code, resource_code, title, content_text, description_text, start_sub_phase_code, end_sub_phase_code, risk_event_code, card_status, is_global, sort_no) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-101', 'res-flood-1', '机关室分为化工组与炼油组，分头执勤值守',
   '中心领导、中队领导启动带班制。机关分成化工组和炼油组分别下沉现场指导。', NULL, 'sp4_1_1', 'sp4_1_1', NULL, 'completed', FALSE, 1),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-102', 'res-flood-2', '清点并分发防汛排涝应急物资与通信器材',
   '准备并调配照明灯、防寒劳保、移动抽水泵、穿式救生衣和移动充电设备。', NULL, 'sp4_1_1', 'sp4_1_2', NULL, 'completed', FALSE, 2),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-201', 'res-flood-5', '在6#路与9#路、11#路交界处及各门岗安装挡水板',
   '炼油中队部署消防力量，迅速在关键路口交界及门岗处搭设防汛挡水防线。', NULL, 'sp4_2_1', 'sp4_2_1', NULL, 'completed', FALSE, 3),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-202', 'res-flood-3', '对4号排洪沟和导流沟垃圾浮余进行清理',
   '指战员清理排洪明沟，防止树枝泥沙淤积导致的排水不畅。', NULL, 'sp4_2_1', 'sp4_2_2', 're4_1', 'completed', FALSE, 4),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-301', 'res-flood-6', '在高碳雨污池部署龙吸水排涝车强力排水',
   '将龙吸水排涝车停驻在高碳装置区，架设强排管线满负荷抽水。', NULL, 'sp4_3_1', 'sp4_3_1', NULL, 'in-progress', FALSE, 5),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-302', 'res-flood-4', '新鲜水泵房区域部署大水牛排涝机器人',
   '使用特勤大水牛机器人，进入泵房低洼深水积水处，启动智能高位排水。', NULL, 'sp4_3_2', 'sp4_3_2', 're4_2', 'in-progress', FALSE, 6),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-303', 'res-flood-5', '6#路地磅北地沟积水强排至301事故池',
   '炼油中队启动大功率抽水车将地磅北地沟积水引入301事故暂存池。', NULL, 'sp4_3_2', 'sp4_3_3', 're4_3', 'in-progress', FALSE, 7),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-401', 'res-flood-3', '使用漏电测试仪对配电房周边水体检测',
   '防灾监督员现场监督，检测强排低洼淹没区有无动力漏电，确保作业安全。', NULL, 'sp4_3_2', 'sp4_4_2', NULL, 'pending', TRUE, 8),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-flood-003'), 'c-flood-402', 'res-flood-4', '配合装置进行排洪明沟和临时排涝渠防漏清淤',
   '清除积水消退后的砂石泥污，保障后期管网排水通畅，防止泥浆滞留。', NULL, 'sp4_4_1', 'sp4_4_2', NULL, 'pending', FALSE, 9);

-- ----------------------------- plan-maoming-001 -----------------------------

INSERT INTO fac_plan_major_phase (instance_id, phase_code, phase_name, phase_order, upgrade_process) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'm1', '1、Ⅲ级响应（车间自救）', 1,
   '当班班组/巡检员发现异常立即报告控制室主操；由当班班长上报至车间安全员与车间主任，15分钟内上报分部调度室。若事故在车间内未能控制或涉及易燃易爆、毒气外泄，即刻启动升级程序。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'm2', '2、Ⅱ级响应（分部级处置）', 2,
   '分部应急指挥中心接管，由现场总指挥下达紧急处置方案，并由应急办公室向地方政府通报；若污染可能超出厂界或伴随严重闪燃、漫溢，立即直报市生态环境局。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'm3', '3、Ⅰ级响应（社会政企联动）', 3,
   '茂名石化公司应急指挥中心接管，启动政企联动机制，30分钟内向茂名市政府及市生态环境局提报初报，全力配合政府消防、交警、环保、医疗等增援力量。'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'm4', '4、后期处置与解除', 4,
   '危险源彻底消除，由茂名市环境指挥部或现场总指挥共同确认无二次衍生隐患后，下达应急解除令，转入环境洗消、损害评估与调查总结工作。');

INSERT INTO fac_plan_sub_phase (instance_id, phase_code, parent_code, phase_name, phase_order, progress) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp1_1', 'm1', '异常察觉与15分钟初报', 1, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp1_2', 'm1', '初期自救与工艺切断', 2, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp1_3', 'm1', '雨水排口关闭与围堵', 3, 100),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp2_1', 'm2', '分部现场指挥部成立', 4, 80),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp2_2', 'm2', '工艺倒罐与SDS紧急停车', 5, 60),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp3_1', 'm3', '政企联动与政府预案请求', 6, 20),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp3_2', 'm3', '泡沫洗消与水幕隔离总攻', 7, 0),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp3_3', 'm3', '周边社区疏散与交警戒严', 8, 0),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp4_1', 'm4', '现场洗消与废水回收降解', 9, 0),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'sp4_2', 'm4', '环境损害评估与事故调查', 10, 0);

INSERT INTO fac_plan_risk_event (instance_id, event_code, sub_phase_code, event_name) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'r1', 'sp1_2', '泄漏剧毒/易燃品遇静电产生闪燃爆炸'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'r2', 'sp1_3', '有毒事故废水经雨水沟排入厂外河流'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'r3', 'sp2_2', 'DCS/SIS紧急切断阀卡涩导致持续泄漏'),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'r4', 'sp3_3', '恶劣风向致有毒气体向下风向敏感点扩散');

INSERT INTO fac_plan_resource (instance_id, resource_code, resource_name, expected_count, actual_count, leader_name, contact_phone, duties, longitude, latitude, sort_no) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'res-1', '现场应急指挥中心与总指挥组', '6', '6',
   '张建国（总指挥）', '0668-2288119（红机专线 8001）',
   '最高决策中枢；评估突发事件态势与级别；统辖调动消防、车间、抢险各战斗力；向茂名市政府及省厅通报事件信息并请求外部增援。', 110.885759, 21.686, 1),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'res-2', '公司应急调度指挥与综合通报组', '8', '8',
   '王立新（办公室主任）', '0668-2288301（调度专线 8002）',
   '常驻应急调度中心；传达总指挥各项战斗指令；统辖全厂公用工程（水、电、蒸汽、风）联锁切断与供应；负责上下级政企信息不间断直报。', 110.8845, 21.6868, 2),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'res-3', '企业专职消防一中队与港区气防中队', '40', '40',
   '李胜利（特勤大队长）', '139-0251-1119（对讲频道 Ch-1）',
   '实施重度热区警戒与被困人员突击搜救；展开360°水幕喷淋降温防爆；铺设高倍数抗溶泡沫覆盖罐区液面隔绝空气。', 110.8872, 21.6853, 3),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'res-4', '化工车间工艺控制与自救应急班组', '18', '18',
   '陈光远（车间主任）', '0668-2288405（内线 8405）',
   '第一现场初期先期处置；紧急操作ESD系统实施联锁切料、火炬放空与安全倒罐；现场协助关闭雨水总排连通阀；引导后续消防主战队伍就近接入稳压消防水接口。', 110.8838, 21.6848, 4),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'res-5', '安全环保处现场督查与防护勤查组', '12', '12', NULL, NULL,
   '现场安全防护监管与风向实时研判；监督参战人员重型防护着装与双人同侪安全制落实；负责热区进出人员及设备的洗消；评估初期环境损害态势。', 110.8865, 21.683, 5);

INSERT INTO fac_plan_action_card (instance_id, card_code, resource_code, title, content_text, description_text, start_sub_phase_code, end_sub_phase_code, risk_event_code, card_status, is_global, sort_no) VALUES
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-101', 'res-4', '当班班组确认异常并执行工艺紧急切断',
   '控制室主操确认DCS/SIS报警，执行火炬放空、隔离泄漏源与安全停车。', NULL, 'sp1_1', 'sp1_2', 'r1', 'completed', FALSE, 1),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-102', 'res-4', '关闭雨水总排连通阀并围堵废水',
   '现场操作工就近关闭雨水总排连通阀，使用沙袋围堵泄漏废水。', NULL, 'sp1_2', 'sp1_3', 'r2', 'completed', FALSE, 2),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-201', 'res-1', '成立分部现场指挥部并通报地方政府',
   '分部指挥中心接管，总指挥下达处置方案，应急办公室向地方政府通报。', NULL, 'sp2_1', 'sp2_1', NULL, 'in-progress', FALSE, 3),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-202', 'res-2', '组织工艺倒罐与SDS紧急停车联动',
   '调度中心联动车间执行倒罐、SDS紧急停车，DCS联锁切料并持续监测。', NULL, 'sp2_2', 'sp2_2', 'r3', 'in-progress', FALSE, 4),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-301', 'res-1', '启动Ⅰ级响应并请求政企联动增援',
   '公司指挥中心接管，30分钟内向市政府及生态环境局提报初报并请求增援。', NULL, 'sp3_1', 'sp3_1', NULL, 'pending', FALSE, 5),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-302', 'res-3', '泡沫洗消与水幕隔离总攻',
   '消防主力展开泡沫覆盖与水幕隔离，阻止有毒气体与火焰扩散。', NULL, 'sp3_2', 'sp3_2', NULL, 'pending', FALSE, 6),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-303', 'res-5', '监测下风向敏感点并组织社区疏散',
   '实时研判风向，对下风向敏感点实施警戒疏散，配合交警戒严。', NULL, 'sp3_3', 'sp3_3', 'r4', 'pending', FALSE, 7),
  ((SELECT id FROM fac_plan_instance WHERE plan_code = 'plan-maoming-001'), 'c-env-401', 'res-5', '现场洗消与废水回收降解',
   '对污染区域洗消，废水导入事故池回收降解，确认无二次污染。', NULL, 'sp4_1', 'sp4_2', NULL, 'pending', FALSE, 8);
