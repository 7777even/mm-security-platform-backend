-- V85 应急预案「域内核预案」+ 一键调用（达梦 DM8 方言，Oracle 兼容，未实跑验证）
-- 镜像自 h2/V85。本文件按 DM8 语法编写，需上环境复核。
-- 注意：DM8 不支持 ADD COLUMN 关键字，直接用 ADD <列定义>；布尔用 NUMBER(1)。

ALTER TABLE fac_emergency_plan ADD domain_code VARCHAR2(32 CHAR) DEFAULT 'production';
ALTER TABLE fac_emergency_plan ADD nuclear NUMBER(1) DEFAULT 0;
ALTER TABLE fac_emergency_plan ADD is_active NUMBER(1) DEFAULT 0;
ALTER TABLE fac_emergency_plan ADD invoke_count INT DEFAULT 0;
ALTER TABLE fac_emergency_plan ADD last_invoked_at TIMESTAMP;
CREATE INDEX idx_emergency_plan_domain ON fac_emergency_plan (domain_code);

UPDATE fac_emergency_plan SET domain_code='production', nuclear=1 WHERE plan_name='乙烯储罐火灾处置方案';
UPDATE fac_emergency_plan SET domain_code='production', nuclear=0 WHERE plan_name='液氨泄漏现场处置方案';
UPDATE fac_emergency_plan SET domain_code='fire',       nuclear=0 WHERE plan_name='乙烯装置消防救援处置方案';
UPDATE fac_emergency_plan SET domain_code='fire',       nuclear=0 WHERE plan_name='储罐区泡沫灭火救援预案';
UPDATE fac_emergency_plan SET domain_code='production', nuclear=1 WHERE plan_name='茂名石化应急预案';
UPDATE fac_emergency_plan SET domain_code='production', nuclear=0 WHERE plan_name='茂名石化综合应急预案（修订版）';
UPDATE fac_emergency_plan SET domain_code='superior',   nuclear=0 WHERE plan_name='广东省石化行业应急预案';

CREATE TABLE fac_emergency_plan_invoke_log (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    plan_id NUMBER(19) NOT NULL,
    plan_name VARCHAR2(128 CHAR) NOT NULL,
    domain_code VARCHAR2(32 CHAR),
    operator VARCHAR2(64 CHAR),
    invoke_note VARCHAR2(256 CHAR),
    invoke_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
