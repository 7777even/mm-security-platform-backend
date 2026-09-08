-- V4 安全防恐域：巡逻摄像机 / 道闸 / 防恐柱 / 车辆·人员识别检索 / 门禁事件
-- 种子平移自前端 fixture(security.ts / securitySearchMock.ts / securityEventStore.ts)，保证 UI 展示一致且为真实数据

CREATE TABLE fac_patrol_camera (
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(128),
    zone     VARCHAR(32),
    status   VARCHAR(16),
    longitude DOUBLE,
    latitude  DOUBLE
);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (1, '北环路1#', '路网防控', '正常', 110.88165, 21.68112);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (2, '北环路1#', '路网防控', '离线', 110.8821, 21.6811);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (3, '北环路1#', '路网防控', '故障', 110.8826, 21.68108);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (4, '北环路2#', '路网防控', '正常', 110.8832, 21.68106);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (5, '东卡口3#', '门禁卡口防控', '正常', 110.89175, 21.67778);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (6, '东卡口4#', '门禁卡口防控', '离线', 110.8917, 21.6757);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (7, '核心区A-1#', '核心区防控', '正常', 110.88962, 21.67569);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (8, '核心区A-2#', '核心区防控', '故障', 110.88736, 21.6757);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (9, '周界西段1#', '周界防控', '正常', 110.87651, 21.68121);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (10, '周界西段2#', '周界防控', '离线', 110.87382, 21.68353);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (11, '外围南门1#', '外围防控', '正常', 110.88162, 21.67038);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (12, '外围南门2#', '外围防控', '正常', 110.88307, 21.67044);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (13, '炼油大道5#', '路网防控', '故障', 110.88731, 21.6778);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (14, '炼油大道6#', '路网防控', '正常', 110.8815, 21.67776);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (15, '西门岗1#', '门禁卡口防控', '离线', 110.87648, 21.68357);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (16, '西门岗2#', '门禁卡口防控', '正常', 110.87386, 21.68471);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (17, '核心区B-1#', '核心区防控', '正常', 110.88519, 21.67357);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (18, '核心区B-2#', '核心区防控', '离线', 110.88307, 21.67359);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (19, '周界东段1#', '周界防控', '正常', 110.8918, 21.67476);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (20, '周界东段2#', '周界防控', '故障', 110.89178, 21.67051);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (21, '外围北门1#', '外围防控', '正常', 110.87449, 21.6847);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (22, '外围北门2#', '外围防控', '离线', 110.88156, 21.68091);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (23, '南环路3#', '路网防控', '正常', 110.8898, 21.67808);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (24, '南环路4#', '路网防控', '正常', 110.8917, 21.67769);
INSERT INTO fac_patrol_camera (id, name, zone, status, longitude, latitude) VALUES (25, '东门岗1#', '门禁卡口防控', '故障', 110.89195, 21.6777);

CREATE TABLE fac_gate_control (
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(128),
    location VARCHAR(64),
    status   VARCHAR(16),
    longitude DOUBLE,
    latitude  DOUBLE
);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (1, '1#门-道闸1', '1#门', '正常', 110.89175, 21.67778);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (2, '1#门-道闸2', '1#门', '正常', 110.8917, 21.6777);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (3, '1#门-道闸3', '1#门', '离线', 110.89165, 21.67762);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (4, '2#门-道闸1', '2#门', '正常', 110.88162, 21.68112);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (5, '2#门-道闸2', '2#门', '故障', 110.88156, 21.68091);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (6, '3#门-道闸1', '3#门', '正常', 110.87648, 21.68357);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (7, '3#门-道闸2', '3#门', '离线', 110.87386, 21.68471);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (8, '东门-道闸1', '东门', '正常', 110.894, 21.6855);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (9, '南门-道闸1', '南门', '正常', 110.8806, 21.6707);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (10, '西门-道闸1', '西门', '正常', 110.8664, 21.6784);
INSERT INTO fac_gate_control (id, name, location, status, longitude, latitude) VALUES (11, '北门-道闸1', '北门', '离线', 110.8776, 21.6898);

CREATE TABLE fac_bollard (
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(128),
    zone     VARCHAR(32),
    status   VARCHAR(16),
    longitude DOUBLE,
    latitude  DOUBLE
);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (1, '1#门防恐柱', '1#门', '正常', 110.89175, 21.67778);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (2, '1#门防恐柱', '1#门', '离线', 110.8917, 21.6777);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (3, '1#门防恐柱', '1#门', '故障', 110.89165, 21.67762);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (4, '2#门防恐柱', '2#门', '正常', 110.88162, 21.68112);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (5, '2#门防恐柱', '2#门', '正常', 110.88156, 21.68091);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (6, '3#门防恐柱', '3#门', '正常', 110.87648, 21.68357);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (7, '3#门防恐柱', '3#门', '离线', 110.87386, 21.68471);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (8, '东门防恐柱', '东门', '正常', 110.894, 21.6855);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (9, '南门防恐柱', '南门', '正常', 110.8806, 21.6707);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (10, '西门防恐柱', '西门', '正常', 110.8664, 21.6784);
INSERT INTO fac_bollard (id, name, zone, status, longitude, latitude) VALUES (11, '北门防恐柱', '北门', '离线', 110.8776, 21.6898);

CREATE TABLE fac_vehicle_search (
    id         BIGINT PRIMARY KEY,
    plate      VARCHAR(32),
    confidence INT,
    gate       VARCHAR(32),
    status     VARCHAR(16),
    time       VARCHAR(32)
);
INSERT INTO fac_vehicle_search (id, plate, confidence, gate, status, time) VALUES (1, '粤KA4543', 80, '东门-入', '入厂', '2026-01-20 10:23:23');
INSERT INTO fac_vehicle_search (id, plate, confidence, gate, status, time) VALUES (2, '未识别', 30, '南门-入', '入厂', '2026-01-20 10:18:05');
INSERT INTO fac_vehicle_search (id, plate, confidence, gate, status, time) VALUES (3, '粤K·B8821', 92, '西门-出', '出厂', '2026-01-20 09:56:41');
INSERT INTO fac_vehicle_search (id, plate, confidence, gate, status, time) VALUES (4, '粤K·C1208', NULL, '北门-入', '入厂', '2026-01-20 09:42:17');
INSERT INTO fac_vehicle_search (id, plate, confidence, gate, status, time) VALUES (5, '未识别', 45, '东门-入', '入厂', '2026-01-20 09:31:08');
INSERT INTO fac_vehicle_search (id, plate, confidence, gate, status, time) VALUES (6, '粤K·D5560', 88, '南门-出', '出厂', '2026-01-20 09:15:33');

CREATE TABLE fac_person_search (
    id     BIGINT PRIMARY KEY,
    name   VARCHAR(32),
    gate   VARCHAR(32),
    status VARCHAR(16),
    date   VARCHAR(32)
);
INSERT INTO fac_person_search (id, name, gate, status, date) VALUES (1, '张三', '东门-入', '入厂', '2026-01-20');
INSERT INTO fac_person_search (id, name, gate, status, date) VALUES (2, '李四', '南门-入', '入厂', '2026-01-20');
INSERT INTO fac_person_search (id, name, gate, status, date) VALUES (3, '王五', '西门-出', '出厂', '2026-01-20');
INSERT INTO fac_person_search (id, name, gate, status, date) VALUES (4, '赵六', '北门-入', '入厂', '2026-01-19');
INSERT INTO fac_person_search (id, name, gate, status, date) VALUES (5, '陈七', '东门-出', '出厂', '2026-01-19');
INSERT INTO fac_person_search (id, name, gate, status, date) VALUES (6, '周八', '南门-入', '入厂', '2026-01-19');

CREATE TABLE fac_security_event (
    event_id VARCHAR(32) PRIMARY KEY,
    person   VARCHAR(32),
    channel  VARCHAR(64),
    card_id  VARCHAR(32),
    vehicle  VARCHAR(32),
    direction VARCHAR(8),
    level    INT,
    ts       VARCHAR(32)
);
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0001', '张伟', '1#门-道闸1', 'C1001', '粤K·12345', '进', 1, '2026-09-07 08:02:11');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0002', '李娜', '1#门-道闸2', 'C1002', '粤K·23456', '出', 1, '2026-09-07 08:05:33');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0003', '王强', '2#门-道闸1', 'C1003', '粤K·34567', '进', 2, '2026-09-07 08:11:47');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0004', '赵敏', '3#门-道闸1', 'C1004', '粤K·45678', '出', 1, '2026-09-07 08:19:02');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0005', '陈杰', '东门-道闸1', 'C1005', '粤K·56789', '进', 3, '2026-09-07 08:24:15');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0006', '刘洋', '南门-道闸1', 'C1006', '粤K·67890', '进', 1, '2026-09-07 08:31:40');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0007', '孙莉', '西门-道闸1', 'C1007', '粤K·78901', '出', 2, '2026-09-07 08:38:09');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0008', '周涛', '北门-道闸1', 'C1008', '粤K·89012', '进', 1, '2026-09-07 08:45:22');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0009', '吴昊', '1#门-道闸3', 'C1009', '粤K·90123', '出', 1, '2026-09-07 08:52:57');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0010', '郑爽', '2#门-道闸2', 'C1010', '粤K·01234', '进', 3, '2026-09-07 09:01:13');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0011', '冯磊', '3#门-道闸2', 'C1011', '粤K·12340', '出', 1, '2026-09-07 09:08:48');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0012', '蒋雯', '东门-道闸1', 'C1012', '粤K·23401', '进', 2, '2026-09-07 09:15:30');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0013', '韩雪', '南门-道闸1', 'C1013', '粤K·34502', '进', 1, '2026-09-07 09:23:05');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0014', '杨帆', '西门-道闸1', 'C1014', '粤K·45603', '出', 1, '2026-09-07 09:30:41');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0015', '朱琳', '北门-道闸1', 'C1015', '粤K·56704', '进', 2, '2026-09-07 09:37:18');
INSERT INTO fac_security_event (event_id, person, channel, card_id, vehicle, direction, level, ts) VALUES ('EVT-20260907-0016', '秦风', '1#门-道闸1', 'C1016', '粤K·67805', '出', 3, '2026-09-07 09:44:55');
