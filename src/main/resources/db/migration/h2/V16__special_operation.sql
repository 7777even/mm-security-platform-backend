-- V16 特殊作业大屏真实数据源（壳内特殊作业面板 + 消防特殊作业弹窗复用）。
-- 数据来源：src/screen/lib/data/specialOperationMock.ts 存量硬编码（作业票台账 + 现场视频/
-- 气体检测/作业人员子表），种子按 buildRecord 生成规则物化 12 张票，覆盖全部作业类型
-- （8 类）×区域（6 处）×等级（3 级）×状态（4 态）的关键组合，数值与文案语义保持不变。
-- 命名回避数据库保留字（value/left/top/type/status/level/count/command）：
-- type→op_type、level→op_level、status→ticket_status、气体检测值→value_text、状态→status_name。
-- 与既有 /fire/special-operations 统计端点（V10 FacSpecialOperationStat）职责不同：本域为作业票明细。

CREATE TABLE fac_special_operation_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_area VARCHAR(64) NOT NULL,
    op_type VARCHAR(32) NOT NULL,
    op_level VARCHAR(16) NOT NULL,
    ticket_status VARCHAR(16) NOT NULL,
    start_time VARCHAR(32) NOT NULL,
    end_time VARCHAR(32) NOT NULL,
    time_range VARCHAR(64) NOT NULL,
    work_unit VARCHAR(128) NOT NULL,
    apply_unit VARCHAR(64) NOT NULL,
    operation_date VARCHAR(32) NOT NULL,
    work_location VARCHAR(128) NOT NULL,
    is_contractor VARCHAR(8) NOT NULL,
    hazard_type VARCHAR(32) NOT NULL,
    leader_name VARCHAR(32) NOT NULL,
    leader_phone VARCHAR(32) NOT NULL,
    position VARCHAR(128) NOT NULL,
    longitude DOUBLE NOT NULL,
    latitude DOUBLE NOT NULL,
    change_reason VARCHAR(128),
    cancel_reason VARCHAR(128),
    guardian_name VARCHAR(32) NOT NULL,
    workers VARCHAR(256) NOT NULL,
    permit_no VARCHAR(64) NOT NULL,
    content VARCHAR(256) NOT NULL,
    video_count INT NOT NULL DEFAULT 0,
    gas_monitor_count INT NOT NULL DEFAULT 0,
    personnel_count INT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_special_operation_video (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    location VARCHAR(128) NOT NULL
);

CREATE TABLE fac_special_operation_gas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    value_text VARCHAR(32) NOT NULL,
    status_name VARCHAR(16) NOT NULL
);

CREATE TABLE fac_special_operation_person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    role_name VARCHAR(32) NOT NULL,
    phone VARCHAR(32) NOT NULL
);

-- 作业票种子（12 张，按 buildRecord 规则物化 id 1..12）
INSERT INTO fac_special_operation_ticket
  (ticket_area, op_type, op_level, ticket_status, start_time, end_time, time_range, work_unit, apply_unit,
   operation_date, work_location, is_contractor, hazard_type, leader_name, leader_phone, position,
   longitude, latitude, change_reason, cancel_reason, guardian_name, workers, permit_no, content,
   video_count, gas_monitor_count, personnel_count, sort_no) VALUES
  ('重油加氢装置', '动火作业', '二级', '已签发', '2026-06-02 09:30:00', '2026-06-02 17:00:00', '2026.06.02 - 2026.06.02', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-02 09:30:00', '重油加氢装置', '否', '--', '赵忠阳', '11111111', '重油加氢装置一层阀口', 110.8811, 21.6749, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001321.pdf', '雨水池堵漏', 26, 3, 2, 1),
  ('乙烯装置区', '盲板抽堵', '三级', '进行中', '2026-06-03 09:30:00', '2026-06-03 17:00:00', '2026.06.03 - 2026.06.03', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-03 09:30:00', '乙烯装置区', '否', '--', '赵忠阳', '11111111', '乙烯装置区一层阀口', 110.8822, 21.6758, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001322.pdf', '盲板抽堵现场施工', 26, 3, 2, 2),
  ('芳烃装置区', '吊装作业', '一级', '已完成', '2026-06-04 09:30:00', '2026-06-04 17:00:00', '2026.06.04 - 2026.06.04', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-04 09:30:00', '芳烃装置区', '否', '--', '赵忠阳', '11111111', '芳烃装置区一层阀口', 110.8833, 21.6767, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001323.pdf', '吊装作业现场施工', 26, 3, 2, 3),
  ('罐区', '动土作业', '二级', '已取消', '2026-06-05 09:30:00', '2026-06-05 17:00:00', '2026.06.05 - 2026.06.05', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-05 09:30:00', '罐区', '是', '--', '赵忠阳', '11111111', '罐区一层阀口', 110.8844, 21.6776, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001324.pdf', '动土作业现场施工', 26, 3, 2, 4),
  ('公用工程区', '受限空间', '三级', '进行中', '2026-06-06 09:30:00', '2026-06-06 17:00:00', '2026.06.06 - 2026.06.06', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-06 09:30:00', '公用工程区', '否', '火灾爆炸', '赵忠阳', '11111111', '公用工程区一层阀口', 110.8855, 21.6785, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001325.pdf', '受限空间现场施工', 26, 3, 2, 5),
  ('仓储区', '高处作业', '一级', '已完成', '2026-06-07 09:30:00', '2026-06-07 17:00:00', '2026.06.07 - 2026.06.07', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-07 09:30:00', '仓储区', '否', '--', '赵忠阳', '11111111', '仓储区一层阀口', 110.8866, 21.6794, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001326.pdf', '高处作业现场施工', 26, 3, 2, 6),
  ('重油加氢装置', '临时用电', '二级', '已取消', '2026-06-08 09:30:00', '2026-06-08 17:00:00', '2026.06.08 - 2026.06.08', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-08 09:30:00', '重油加氢装置', '否', '--', '赵忠阳', '11111111', '重油加氢装置一层阀口', 110.8877, 21.6803, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001327.pdf', '临时用电现场施工', 26, 3, 2, 7),
  ('乙烯装置区', '断路作业', '三级', '已签发', '2026-06-09 09:30:00', '2026-06-09 17:00:00', '2026.06.09 - 2026.06.09', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-09 09:30:00', '乙烯装置区', '是', '--', '赵忠阳', '11111111', '乙烯装置区一层阀口', 110.8888, 21.6812, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001328.pdf', '断路作业现场施工', 26, 3, 2, 8),
  ('芳烃装置区', '动火作业', '一级', '进行中', '2026-06-10 09:30:00', '2026-06-10 17:00:00', '2026.06.10 - 2026.06.10', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-10 09:30:00', '芳烃装置区', '否', '--', '赵忠阳', '11111111', '芳烃装置区一层阀口', 110.8899, 21.6821, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001329.pdf', '雨水池堵漏', 26, 3, 2, 9),
  ('罐区', '盲板抽堵', '二级', '已完成', '2026-06-11 09:30:00', '2026-06-11 17:00:00', '2026.06.11 - 2026.06.11', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-11 09:30:00', '罐区', '否', '火灾爆炸', '赵忠阳', '11111111', '罐区一层阀口', 110.8910, 21.6740, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001330.pdf', '盲板抽堵现场施工', 26, 3, 2, 10),
  ('公用工程区', '吊装作业', '三级', '已取消', '2026-06-12 09:30:00', '2026-06-12 17:00:00', '2026.06.12 - 2026.06.12', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-12 09:30:00', '公用工程区', '否', '--', '赵忠阳', '11111111', '公用工程区一层阀口', 110.8921, 21.6749, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001331.pdf', '吊装作业现场施工', 26, 3, 2, 11),
  ('仓储区', '动土作业', '一级', '已签发', '2026-06-13 09:30:00', '2026-06-13 17:00:00', '2026.06.13 - 2026.06.13', '中国石油天然气第六建设有限公司', '化工一部', '2026-06-13 09:30:00', '仓储区', '是', '--', '赵忠阳', '11111111', '仓储区一层阀口', 110.8932, 21.6758, '--', '--', '王学龙', '阮国述, 孙业光', '20260601150001332.pdf', '动土作业现场施工', 26, 3, 2, 12);

-- 现场视频（每票 4 路，沿用 mock videos 生成规则）
INSERT INTO fac_special_operation_video (ticket_id, name, location)
SELECT id, '现场视频-1', CONCAT(ticket_area, '监控点1') FROM fac_special_operation_ticket ORDER BY id;
INSERT INTO fac_special_operation_video (ticket_id, name, location)
SELECT id, '现场视频-2', CONCAT(ticket_area, '监控点2') FROM fac_special_operation_ticket ORDER BY id;
INSERT INTO fac_special_operation_video (ticket_id, name, location)
SELECT id, '现场视频-3', CONCAT(ticket_area, '监控点3') FROM fac_special_operation_ticket ORDER BY id;
INSERT INTO fac_special_operation_video (ticket_id, name, location)
SELECT id, '现场视频-4', CONCAT(ticket_area, '监控点4') FROM fac_special_operation_ticket ORDER BY id;

-- 气体检测点（每票 3 项，沿用 mock gasPoints）
INSERT INTO fac_special_operation_gas (ticket_id, name, value_text, status_name)
SELECT id, '可燃气体', '0.2%LEL', '正常' FROM fac_special_operation_ticket ORDER BY id;
INSERT INTO fac_special_operation_gas (ticket_id, name, value_text, status_name)
SELECT id, '氧气', '20.8%', '正常' FROM fac_special_operation_ticket ORDER BY id;
INSERT INTO fac_special_operation_gas (ticket_id, name, value_text, status_name)
SELECT id, '硫化氢', '0ppm', '正常' FROM fac_special_operation_ticket ORDER BY id;

-- 作业人员（每票 2 人，沿用 mock personnel）
INSERT INTO fac_special_operation_person (ticket_id, name, role_name, phone)
SELECT id, '阮国述', '施工人员', '13800001111' FROM fac_special_operation_ticket ORDER BY id;
INSERT INTO fac_special_operation_person (ticket_id, name, role_name, phone)
SELECT id, '孙业光', '监护人员', '13800002222' FROM fac_special_operation_ticket ORDER BY id;
