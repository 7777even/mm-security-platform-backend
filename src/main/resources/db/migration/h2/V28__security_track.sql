-- V28 巡更/通行轨迹 + 检索详情扩展（替代前端 securityTrackMock / securitySearchMock 的假数据）。
-- 1) 车辆检索详情扩展列（原 securitySearchMock.vehicleDetailExtras 迁入 DB）
ALTER TABLE fac_vehicle_search ADD COLUMN vehicle_type VARCHAR(32);
ALTER TABLE fac_vehicle_search ADD COLUMN driver_name VARCHAR(32);
ALTER TABLE fac_vehicle_search ADD COLUMN driver_phone VARCHAR(32);
ALTER TABLE fac_vehicle_search ADD COLUMN company VARCHAR(64);
ALTER TABLE fac_vehicle_search ADD COLUMN appointment_no VARCHAR(32);
ALTER TABLE fac_vehicle_search ADD COLUMN appointment_time VARCHAR(64);
ALTER TABLE fac_vehicle_search ADD COLUMN visit_purpose VARCHAR(64);
ALTER TABLE fac_vehicle_search ADD COLUMN waybill_no VARCHAR(32);
ALTER TABLE fac_vehicle_search ADD COLUMN cargo VARCHAR(64);
ALTER TABLE fac_vehicle_search ADD COLUMN destination VARCHAR(64);

-- 2) 人员检索详情扩展列（原 securitySearchMock.personDetailExtras 迁入 DB）
ALTER TABLE fac_person_search ADD COLUMN gender VARCHAR(8);
ALTER TABLE fac_person_search ADD COLUMN phone VARCHAR(32);
ALTER TABLE fac_person_search ADD COLUMN company VARCHAR(64);
ALTER TABLE fac_person_search ADD COLUMN id_number VARCHAR(32);
ALTER TABLE fac_person_search ADD COLUMN appointment_no VARCHAR(32);
ALTER TABLE fac_person_search ADD COLUMN appointment_time VARCHAR(64);
ALTER TABLE fac_person_search ADD COLUMN visit_purpose VARCHAR(64);
ALTER TABLE fac_person_search ADD COLUMN special_operation VARCHAR(64);
ALTER TABLE fac_person_search ADD COLUMN operation_area VARCHAR(64);

-- 3) 轨迹时间轴表（entity_id 为 NULL 的行表示该模式下的默认轨迹）
CREATE TABLE fac_security_track (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    track_mode VARCHAR(16) NOT NULL,
    entity_id BIGINT NULL,
    seq_no INT NOT NULL,
    location VARCHAR(64),
    status VARCHAR(32),
    status_tone VARCHAR(16),
    track_time VARCHAR(32),
    capture_hint VARCHAR(64)
);

-- 4) 轨迹模式配置表（起止点标签）
CREATE TABLE fac_security_track_meta (
    track_mode VARCHAR(16) PRIMARY KEY,
    start_label VARCHAR(32),
    end_label VARCHAR(32)
);
INSERT INTO fac_security_track_meta (track_mode, start_label, end_label) VALUES ('vehicle', '东门', '装卸点');
INSERT INTO fac_security_track_meta (track_mode, start_label, end_label) VALUES ('person', '西门', '作业区');

-- 5) 轨迹种子：车辆 id=1
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', 1, 1, '东门-入', '入厂', 'enter', '2026-01-20 09:12:08', '东门卡口');
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', 1, 2, '主干道 A 段', '通行', 'pass', '2026-01-20 09:18:33', '路网抓拍');
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', 1, 3, '炼油一区', '到达', 'pass', '2026-01-20 09:26:41', '区域卡口');
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', 1, 4, '装卸点', '停留', 'pass', '2026-01-20 10:05:12', '装卸区监控');

-- 6) 轨迹种子：车辆默认（entity_id IS NULL）
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', NULL, 1, '东门-入', '入厂', 'enter', '2026-01-20 09:12:08', NULL);
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', NULL, 2, '厂内路网', '通行', 'pass', '2026-01-20 09:20:00', NULL);
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('vehicle', NULL, 3, '目标区域', '到达', 'pass', '2026-01-20 09:35:22', NULL);

-- 7) 轨迹种子：人员 id=1
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', 1, 1, '西门-入', '入厂', 'enter', '2026-01-20 08:02:15', '门禁抓拍');
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', 1, 2, '行政楼通道', '通行', 'pass', '2026-01-20 08:09:40', '走廊监控');
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', 1, 3, '炼油二区', '到达', 'pass', '2026-01-20 08:22:18', '作业区门禁');
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', 1, 4, '检修平台', '作业', 'pass', '2026-01-20 09:10:05', '高处作业监控');

-- 8) 轨迹种子：人员默认（entity_id IS NULL）
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', NULL, 1, '北门-入', '入厂', 'enter', '2026-01-20 08:00:12', NULL);
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', NULL, 2, '厂区通道', '通行', 'pass', '2026-01-20 08:15:30', NULL);
INSERT INTO fac_security_track (track_mode, entity_id, seq_no, location, status, status_tone, track_time, capture_hint) VALUES ('person', NULL, 3, '作业区域', '到达', 'pass', '2026-01-20 08:40:18', NULL);

-- 9) 车辆检索详情种子
UPDATE fac_vehicle_search SET vehicle_type='危化品运输车', driver_name='刘师傅', driver_phone='138****4521', company='茂名顺达物流有限公司', appointment_no='YY202601200018', appointment_time='2026-01-20 09:00 — 18:00', visit_purpose='原料配送', waybill_no='YD202601200031', cargo='工业乙醇', destination='炼油一区装卸点' WHERE id=1;
UPDATE fac_vehicle_search SET vehicle_type='未知', driver_name='—', driver_phone='—', company='—', appointment_no='—', appointment_time='—', visit_purpose='—', waybill_no='—', cargo='—', destination='—' WHERE id=2;
UPDATE fac_vehicle_search SET vehicle_type='普通货车', driver_name='王师傅', driver_phone='139****8820', company='粤西运输队', appointment_no='YY202601200012', appointment_time='2026-01-20 08:30 — 17:30', visit_purpose='设备检修物资', waybill_no='YD202601200022', cargo='检修工具及配件', destination='机修车间' WHERE id=3;
UPDATE fac_vehicle_search SET vehicle_type='厢式货车', driver_name='陈师傅', driver_phone='137****1208', company='茂名石化物资供应中心', appointment_no='YY202601190045', appointment_time='2026-01-20 07:00 — 16:00', visit_purpose='日常物资配送', waybill_no='YD202601200015', cargo='劳保用品', destination='仓储中心' WHERE id=4;
UPDATE fac_vehicle_search SET vehicle_type='未知', driver_name='—', driver_phone='—', company='—', appointment_no='—', appointment_time='—', visit_purpose='—', waybill_no='—', cargo='—', destination='—' WHERE id=5;
UPDATE fac_vehicle_search SET vehicle_type='罐车', driver_name='张师傅', driver_phone='136****5560', company='华南化工物流', appointment_no='YY202601200009', appointment_time='2026-01-20 06:00 — 15:00', visit_purpose='化工原料入厂', waybill_no='YD202601200008', cargo='丙烯', destination='化工装置区' WHERE id=6;

-- 10) 人员检索详情种子
UPDATE fac_person_search SET gender='男', phone='138****1001', company='茂名石化检修公司', id_number='4409**********1234', appointment_no='YY202601200021', appointment_time='2026-01-20 08:00 — 17:00', visit_purpose='设备检修', special_operation='高处作业', operation_area='炼油二区' WHERE id=1;
UPDATE fac_person_search SET gender='男', phone='139****2002', company='广东安环检测中心', id_number='4409**********5678', appointment_no='YY202601200019', appointment_time='2026-01-20 09:00 — 16:00', visit_purpose='环保检测', special_operation='受限空间作业', operation_area='污水处理站' WHERE id=2;
UPDATE fac_person_search SET gender='女', phone='137****3003', company='茂名石化设计院', id_number='4409**********9012', appointment_no='YY202601200015', appointment_time='2026-01-20 10:00 — 18:00', visit_purpose='现场勘察', special_operation='—', operation_area='—' WHERE id=3;
UPDATE fac_person_search SET gender='男', phone='136****4004', company='中石化工程公司', id_number='4409**********3456', appointment_no='YY202601190038', appointment_time='2026-01-19 08:00 — 17:00', visit_purpose='工程施工', special_operation='动火作业', operation_area='化工新区' WHERE id=4;
UPDATE fac_person_search SET gender='男', phone='135****5005', company='茂名石化保卫部', id_number='4409**********7890', appointment_no='—', appointment_time='—', visit_purpose='内部巡检', special_operation='—', operation_area='全厂区' WHERE id=5;
UPDATE fac_person_search SET gender='女', phone='134****6006', company='华南设备供应商', id_number='4409**********2345', appointment_no='YY202601190032', appointment_time='2026-01-19 09:00 — 17:00', visit_purpose='设备安装调试', special_operation='吊装作业', operation_area='动力站' WHERE id=6;
