-- V21 安防黑名单 + 火情态势地图点位真实数据源。
-- 数据来源一：src/screen/lib/data/blacklistMock.ts 存量硬编码
--   （vehicleBlacklist 3 条 + personBlacklist 3 条），车辆与人员合并进 fac_blacklist_entry，
--   以 entry_kind=VEHICLE|PERSON 区分；id_card 沿用前端已脱敏形态（4409**********1234）原样入库。
-- 数据来源二：src/screen/lib/data/fireSituationMapMock.ts 存量硬编码（fireSituationMarkers 7 个点位），
--   前端字符串 id（event-1 / op-hot / alarm-1 等）落到 marker_code，icon_url 保留前端静态资源路径。
-- 命名回避数据库保留字（value/command/status/level/type/count）：
--   状态列 entry_status、原因列 reason_text、时间列 event_time、等级列 level_name、重点标记列 important_flag。

CREATE TABLE fac_blacklist_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_kind VARCHAR(16) NOT NULL,
    subject_name VARCHAR(64) NOT NULL,
    id_card VARCHAR(32),
    reason_text VARCHAR(128) NOT NULL,
    event_time VARCHAR(32) NOT NULL,
    entry_status VARCHAR(16) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0
);

CREATE TABLE fac_fire_situation_marker (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    marker_code VARCHAR(32) NOT NULL,
    marker_kind VARCHAR(16) NOT NULL,
    title VARCHAR(64) NOT NULL,
    subtitle VARCHAR(128) NOT NULL,
    longitude DOUBLE,
    latitude DOUBLE,
    important_flag BOOLEAN NOT NULL DEFAULT FALSE,
    icon_url VARCHAR(128) NOT NULL,
    level_name VARCHAR(32),
    target_id BIGINT NOT NULL DEFAULT 0,
    sort_no INT NOT NULL DEFAULT 0
);

-- 车辆黑名单（VEHICLE，3 条）
INSERT INTO fac_blacklist_entry (entry_kind, subject_name, id_card, reason_text, event_time, entry_status, sort_no) VALUES
  ('VEHICLE', '粤K·A4543', NULL, '违规闯入生产区', '2026-08-05 14:20:11', '生效中', 1),
  ('VEHICLE', '粤K·B2871', NULL, '超速行驶', '2026-08-02 09:15:33', '生效中', 2),
  ('VEHICLE', '粤K·C6610', NULL, '逾期未出厂', '2026-07-28 18:40:02', '已解除', 3);

-- 人员黑名单（PERSON，3 条；id_card 为前端已脱敏文本）
INSERT INTO fac_blacklist_entry (entry_kind, subject_name, id_card, reason_text, event_time, entry_status, sort_no) VALUES
  ('PERSON', '张**', '4409**********1234', '未佩戴安全帽进入高危区', '2026-08-06 10:02:45', '生效中', 1),
  ('PERSON', '李**', '4409**********5678', '违规携带火种', '2026-08-01 16:22:19', '生效中', 2),
  ('PERSON', '王**', '4409**********9012', '恶意破坏门禁设备', '2026-07-20 11:08:37', '已解除', 3);

-- 火情态势地图点位（7 个：1 应急事件 + 4 作业 + 2 报警）
INSERT INTO fac_fire_situation_marker (marker_code, marker_kind, title, subtitle, longitude, latitude, important_flag, icon_url, level_name, target_id, sort_no) VALUES
  ('event-1', 'event', '当前应急事件', 'A装置区火灾处置中', 110.875, 21.6855, TRUE, '/icons/fire-situation/flame.svg', '处置中', 1, 1),
  ('op-hot', 'operation', '特级动火作业', '芳烃装置区 · 进行中', 110.879, 21.6815, TRUE, '/icons/fire-situation/flame.svg', '特级', 1, 2),
  ('op-confined', 'operation', '一级受限空间作业', '罐区 · 进行中', 110.8828, 21.6828, TRUE, '/icons/fire-situation/confined-space.svg', '一级', 5, 3),
  ('op-lift', 'operation', '一级吊装作业', '乙烯装置区 · 进行中', 110.8835, 21.6752, FALSE, '/icons/fire-situation/crane.svg', '一级', 3, 4),
  ('op-height', 'operation', '一级高处作业', '芳烃装置区 · 进行中', 110.8872, 21.6728, FALSE, '/icons/fire-situation/ladder.svg', '一级', 6, 5),
  ('alarm-1', 'alarm', '火灾报警', '化工区A装置西侧 · 未销警', 110.888, 21.6854, TRUE, '/icons/fire-situation/bell-ringing.svg', '未销警', 1, 6),
  ('alarm-2', 'alarm', 'GDS报警', '输油管廊 · 未销警', 110.8902, 21.6752, FALSE, '/icons/fire-situation/gas.svg', '未销警', 2, 7);
