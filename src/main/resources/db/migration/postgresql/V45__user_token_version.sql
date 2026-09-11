-- 令牌失效版本号（PostgreSQL）
-- 语义与 h2/V45 完全一致，详见 h2/V45__user_token_version.sql 的注释。
ALTER TABLE sys_user ADD COLUMN token_version INT NOT NULL DEFAULT 0;
