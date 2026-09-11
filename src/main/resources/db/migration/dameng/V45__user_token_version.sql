-- 令牌失效版本号（达梦 DM8 / Oracle 兼容语法：ADD 子句不带 COLUMN 关键字）
-- 语义与 h2/V45 完全一致，详见 h2/V45__user_token_version.sql 的注释。
ALTER TABLE sys_user ADD token_version INT DEFAULT 0 NOT NULL;
