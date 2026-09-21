-- V62 救援力量唯一真源：以「扁平资源台账」fac_rescue_* 为唯一真源（装备/人员/车辆），
-- 退役原「队伍体系」子表 fac_brigade_{equipment,person,vehicle}（fac_brigade_team 保留为中队主表）。
-- 背景：V19 曾把前端 4 个互不相干的 mock 各自独立落库，导致同一批 8 个中队出现两套并行的
-- 装备/人员/车辆（扁平台账 35/52/12 vs 队伍子表 71/110/39），消防大屏与管理端数字不一致。
-- 统一后：大屏「消防救援力量」、管理端 /rescue-resources/*、应急面板「应急救援力量」同取扁平表口径。

-- 1) 给扁平表补「队伍详情」所需字段（原仅存在于队伍子表）
ALTER TABLE fac_rescue_personnel ADD person_group VARCHAR2(16 CHAR);
ALTER TABLE fac_rescue_personnel ADD phone VARCHAR2(32 CHAR);
ALTER TABLE fac_rescue_personnel ADD duty_status VARCHAR2(16 CHAR);
ALTER TABLE fac_rescue_equipment ADD category VARCHAR2(32 CHAR);
ALTER TABLE fac_rescue_equipment ADD unit VARCHAR2(16 CHAR);

-- 2) 回填人员分组（按岗位推导，与消防队伍详情分组口径一致：指挥/战斗/驾驶/通信/保障）
UPDATE fac_rescue_personnel SET person_group = CASE
    WHEN person_role IN ('班长', '副班长') THEN '指挥'
    WHEN person_role = '战斗员' THEN '战斗'
    WHEN person_role = '驾驶员' THEN '驾驶'
    WHEN person_role = '通信员' THEN '通信'
    ELSE '保障'
END;
UPDATE fac_rescue_personnel SET duty_status = CASE
    WHEN MOD(id, 7) = 0 THEN '休假'
    WHEN MOD(id, 3) = 0 THEN '备勤'
    ELSE '在岗'
END;
UPDATE fac_rescue_personnel SET phone = CASE squadron
    WHEN '乙烯中队' THEN '13665898855'
    WHEN '炼油中队' THEN '13866887766'
    WHEN '罐区中队' THEN '13788996655'
    WHEN '仓储中队' THEN '13977665544'
    WHEN '码头中队' THEN '13699887766'
    WHEN '芳烃中队' THEN '13566778899'
    WHEN '特勤一中队' THEN '13855667788'
    WHEN '特勤二中队' THEN '13744556677'
    ELSE '13700000000'
END;

-- 3) 回填装备类别（与消防队伍详情 6 类口径一致：防护装备/灭火器材/破拆工具/侦检仪器/通讯设备/照明排烟）
UPDATE fac_rescue_equipment SET category = CASE
    WHEN equip_name IN ('防毒面罩', '空气呼吸器', '正压式呼吸机', '隔热服', '化学防护服') THEN '防护装备'
    WHEN equip_name IN ('消防水带', '手提式灭火器', '泡沫枪', '机动泵') THEN '灭火器材'
    WHEN equip_name IN ('破拆工具组', '液压扩张器', '无齿锯', '堵漏工具', '救生绳') THEN '破拆工具'
    WHEN equip_name = '强光手电' THEN '照明排烟'
    ELSE '其他'
END;
UPDATE fac_rescue_equipment SET unit = CASE equip_name
    WHEN '消防水带' THEN '盘'
    WHEN '手提式灭火器' THEN '具'
    WHEN '泡沫枪' THEN '支'
    WHEN '机动泵' THEN '台'
    WHEN '液压扩张器' THEN '台'
    WHEN '无齿锯' THEN '台'
    WHEN '救生绳' THEN '条'
    WHEN '强光手电' THEN '个'
    ELSE '套'
END;

-- 4) 应急力量「装备车辆」更名为「救援装备」（与消防大屏口径一致；计数由 fac_rescue_equipment 实时覆盖）
UPDATE sys_emergency_strength SET kind = '救援装备', count = 35 WHERE kind = '装备车辆';

-- 5) 退役队伍子表（数据以扁平表为唯一真源）
DROP TABLE fac_brigade_equipment;
DROP TABLE fac_brigade_person;
DROP TABLE fac_brigade_vehicle;
