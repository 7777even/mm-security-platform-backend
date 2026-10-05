-- V85 应急预案「域内核预案」+ 一键调用（PostgreSQL 方言，未实跑验证）
-- 镜像自 h2/V85。本文件按标准 PG 语法编写，需上环境复核。

ALTER TABLE fac_emergency_plan ADD COLUMN domain_code VARCHAR(32) NOT NULL DEFAULT 'production';
ALTER TABLE fac_emergency_plan ADD COLUMN nuclear BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE fac_emergency_plan ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE fac_emergency_plan ADD COLUMN invoke_count INT NOT NULL DEFAULT 0;
ALTER TABLE fac_emergency_plan ADD COLUMN last_invoked_at TIMESTAMP;
CREATE INDEX idx_emergency_plan_domain ON fac_emergency_plan (domain_code);

UPDATE fac_emergency_plan SET domain_code='production', nuclear=TRUE  WHERE plan_name='乙烯储罐火灾处置方案';
UPDATE fac_emergency_plan SET domain_code='production', nuclear=FALSE WHERE plan_name='液氨泄漏现场处置方案';
UPDATE fac_emergency_plan SET domain_code='fire',       nuclear=FALSE WHERE plan_name='乙烯装置消防救援处置方案';
UPDATE fac_emergency_plan SET domain_code='fire',       nuclear=FALSE WHERE plan_name='储罐区泡沫灭火救援预案';
UPDATE fac_emergency_plan SET domain_code='production', nuclear=TRUE  WHERE plan_name='茂名石化应急预案';
UPDATE fac_emergency_plan SET domain_code='production', nuclear=FALSE WHERE plan_name='茂名石化综合应急预案（修订版）';
UPDATE fac_emergency_plan SET domain_code='superior',   nuclear=FALSE WHERE plan_name='广东省石化行业应急预案';

CREATE TABLE IF NOT EXISTS fac_emergency_plan_invoke_log (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    domain_code VARCHAR(32),
    operator VARCHAR(64),
    invoke_note VARCHAR(256),
    invoke_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
