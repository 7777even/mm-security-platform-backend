-- V10 消防监控大屏真实数据源：消防救援力量统计 / 特殊作业统计 / 消防设施设备状态 / 防火巡查记录。
-- 数据来源：大屏存量硬编码常量与 src/screen/lib/data/{mock,firePatrolMock}.ts，
-- 迁移后数值语义保持不变（沿用原硬编码数值），但改为可维护的 DB 数据源，运营可更新而无需改代码。
-- 命名回避数据库保留字（count/shift/result/level），统一加后缀，便于后期迁移达梦 DM8。

CREATE TABLE fac_rescue_force_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(32) NOT NULL,
    stat_count INT NOT NULL DEFAULT 0,
    unit VARCHAR(16) NOT NULL,
    icon_type VARCHAR(32) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_special_operation_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(32) NOT NULL,
    stat_count INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

-- 单行聚合状态表：消防设施设备整体状态（完好率 / 在线率为百分比整数 0-100）
CREATE TABLE fac_fire_equipment_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_cnt INT NOT NULL DEFAULT 0,
    offline_cnt INT NOT NULL DEFAULT 0,
    fault_cnt INT NOT NULL DEFAULT 0,
    integrity_rate INT NOT NULL DEFAULT 0,
    online_rate INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_patrol (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patrol_date VARCHAR(10) NOT NULL,
    shift_name VARCHAR(16) NOT NULL,
    duty_person VARCHAR(32) NOT NULL,
    patrol_count VARCHAR(16) NOT NULL,
    locations VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    work_order_no VARCHAR(64)
);

-- 15 项标准巡查检查项定义（防火巡查标准检查表）
CREATE TABLE fac_fire_patrol_item_def (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code VARCHAR(8) NOT NULL,
    category VARCHAR(32) NOT NULL,
    content VARCHAR(128) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

-- 仅记录非「正常」的检查项结果（异常 / 不适用），缺省项由服务端按标准表补齐为「正常」
CREATE TABLE fac_fire_patrol_item_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patrol_id BIGINT NOT NULL,
    item_code VARCHAR(8) NOT NULL,
    check_result VARCHAR(16) NOT NULL,
    abnormal_desc VARCHAR(255),
    photo_file VARCHAR(128)
);

-- —— 种子：消防救援力量统计（原 mock.ts rescueStats）——
INSERT INTO fac_rescue_force_stat (label, stat_count, unit, icon_type, sort_no) VALUES
 ('消防队伍', 10,  '支', 'squad',     1),
 ('救援人员', 398, '人', 'person',    2),
 ('救援装备', 123, '套', 'equipment', 3),
 ('救援车辆', 83,  '台', 'vehicle',   4);

-- —— 种子：八大特殊作业统计（原 mock.ts specialOperations）——
INSERT INTO fac_special_operation_stat (label, stat_count, sort_no) VALUES
 ('动火作业',   48, 1),
 ('盲板抽堵',   3,  2),
 ('吊装作业',   2,  3),
 ('动土作业',   0,  4),
 ('受限空间',   9,  5),
 ('高处作业',   35, 6),
 ('临时用电',   25, 7),
 ('断路作业',   1,  8);

-- —— 种子：消防设施设备状态（原 mock.ts equipmentStatus）——
INSERT INTO fac_fire_equipment_status (total_cnt, offline_cnt, fault_cnt, integrity_rate, online_rate) VALUES
 (1233, 23, 23, 98, 98);

-- —— 种子：防火巡查记录（原 firePatrolMock.ts firePatrolRecords，12 条）——
INSERT INTO fac_fire_patrol (patrol_date, shift_name, duty_person, patrol_count, locations, completed, work_order_no) VALUES
 ('2026-08-20', '上午', '张三',     '第1次', '1#联合装置,中央控制室',        TRUE,  NULL),
 ('2026-08-20', '下午', '王值班长', '第2次', '储运部罐区,装卸区',            FALSE, NULL),
 ('2026-08-20', '夜间', '赵四',     '第3次', '常减压装置,消防泵房',          FALSE, NULL),
 ('2026-08-19', '上午', '张三',     '第1次', '1#联合装置,中央控制室',        TRUE,  'WO-20260819-010'),
 ('2026-08-19', '下午', '王值班长', '第2次', '储运部罐区,装卸区',            TRUE,  NULL),
 ('2026-08-19', '夜间', '赵四',     '第3次', '常减压装置,消防泵房',          TRUE,  NULL),
 ('2026-08-18', '上午', '李五',     '第1次', '1#联合装置,中央控制室,储运部罐区', TRUE, NULL),
 ('2026-08-18', '下午', '张三',     '第2次', '装卸区,消防泵房',              TRUE,  NULL),
 ('2026-08-18', '夜间', '王值班长', '第3次', '常减压装置',                   TRUE,  NULL),
 ('2026-08-17', '上午', '赵四',     '第1次', '1#联合装置,中央控制室',        TRUE,  NULL),
 ('2026-08-17', '下午', '李五',     '第2次', '储运部罐区,装卸区',            TRUE,  NULL),
 ('2026-08-17', '夜间', '张三',     '第3次', '常减压装置,消防泵房',          TRUE,  NULL);

-- —— 种子：15 项标准检查项定义（原 firePatrolMock.ts patrolCheckItemDefs）——
INSERT INTO fac_fire_patrol_item_def (item_code, category, content, sort_no) VALUES
 ('A1', '用火用电安全管理', '有无违章用火情况', 1),
 ('A2', '用火用电安全管理', '有无违章用电情况', 2),
 ('B1', '疏散通道', '安全出口、疏散通道、疏散楼梯是否畅通', 3),
 ('B2', '疏散通道', '疏散走道、疏散楼梯、安全出口是否堆放可燃物', 4),
 ('B3', '疏散通道', '疏散走道、疏散楼梯、顶棚装修材料是否合格', 5),
 ('C1', '防火分隔设施', '常闭防火门是否处于正常关闭状态', 6),
 ('C2', '防火分隔设施', '常闭防火门是否被锁闭', 7),
 ('C3', '防火分隔设施', '防火卷帘是否处于正常工作状态', 8),
 ('C4', '防火分隔设施', '防火卷帘下方是否堆放物品', 9),
 ('D1', '消防设施器材', '疏散指示标志是否完好', 10),
 ('D2', '消防设施器材', '应急照明是否完好', 11),
 ('D3', '消防设施器材', '火灾探测器是否正常', 12),
 ('D4', '消防设施器材', '自动喷水灭火系统组件是否完好', 13),
 ('D5', '消防设施器材', '室内外消火栓是否完好', 14),
 ('D6', '消防设施器材', '灭火器是否处于正常完好状态', 15);

-- —— 种子：非「正常」检查项结果（仅异常 / 不适用，其余由服务端按标准表补齐为「正常」）——
-- 巡查 4（2026-08-19 上午）：C1 异常
INSERT INTO fac_fire_patrol_item_result (patrol_id, item_code, check_result, abnormal_desc, photo_file) VALUES
 (4, 'C1', '异常',   '3F 常闭防火门被挡块撑开', 'patrol-photo-placeholder.png'),
 (7, 'B2', '异常',   '疏散走道堆放施工材料',    'patrol-photo-placeholder.png'),
 (7, 'C3', '不适用', NULL, NULL),
 (7, 'C4', '不适用', NULL, NULL),
 (9, 'D3', '异常',   '5#装置区探测器指示灯不亮', 'patrol-photo-placeholder.png'),
 (11, 'A2', '异常',  '临时用电线路私拉乱接',    'patrol-photo-placeholder.png');
