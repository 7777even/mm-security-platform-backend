CREATE TABLE IF NOT EXISTS sys_notification (
  id BIGSERIAL PRIMARY KEY,
  category VARCHAR(16) NOT NULL,
  title VARCHAR(200) NOT NULL,
  summary VARCHAR(500),
  target_type VARCHAR(16),
  target_id VARCHAR(64),
  recipient VARCHAR(64),
  read_flag SMALLINT NOT NULL DEFAULT 0,
  deleted SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

INSERT INTO sys_notification (category, title, summary, target_type, target_id, recipient, read_flag, deleted, created_at, updated_at)
VALUES
  ('system', '平台例行版本升级通知', '系统将于维护窗口进行升级，请关注公告', NULL, NULL, NULL, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('system', '全员应急演练公告', '今日将开展全员应急演练，请届时配合', NULL, NULL, NULL, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
