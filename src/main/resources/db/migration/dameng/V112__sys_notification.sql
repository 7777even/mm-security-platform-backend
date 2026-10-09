CREATE TABLE sys_notification (
  id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
  category VARCHAR2(16 CHAR) NOT NULL,
  title VARCHAR2(200 CHAR) NOT NULL,
  summary VARCHAR2(500 CHAR),
  target_type VARCHAR2(16 CHAR),
  target_id VARCHAR2(64 CHAR),
  recipient VARCHAR2(64 CHAR),
  read_flag NUMBER(3) NOT NULL DEFAULT 0,
  deleted NUMBER(3) NOT NULL DEFAULT 0,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

INSERT INTO sys_notification (category, title, summary, target_type, target_id, recipient, read_flag, deleted, created_at, updated_at)
VALUES ('system', '平台例行版本升级通知', '系统将于维护窗口进行升级，请关注公告', NULL, NULL, NULL, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_notification (category, title, summary, target_type, target_id, recipient, read_flag, deleted, created_at, updated_at)
VALUES ('system', '全员应急演练公告', '今日将开展全员应急演练，请届时配合', NULL, NULL, NULL, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
