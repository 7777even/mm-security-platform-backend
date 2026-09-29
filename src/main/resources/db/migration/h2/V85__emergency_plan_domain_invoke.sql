-- V85 应急预案「域内核预案」+ 一键调用（H2 方言）
-- 为 fac_emergency_plan 增加业务域(domain)/核武器化标记(nuclear)/激活态(is_active)/
-- 调用次数(invoke_count)/最近调用时间(last_invoked_at)，使生产应急二级功能可按域筛选
-- 浏览核预案并一键调用（激活+广播+留痕，不触达任何物理设备，符合零下行控制红线）。
-- 新增 fac_emergency_plan_invoke_log 调用留痕表。

ALTER TABLE fac_emergency_plan ADD COLUMN domain VARCHAR(32) NOT NULL DEFAULT 'production';
ALTER TABLE fac_emergency_plan ADD COLUMN nuclear BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE fac_emergency_plan ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE fac_emergency_plan ADD COLUMN invoke_count INT NOT NULL DEFAULT 0;
ALTER TABLE fac_emergency_plan ADD COLUMN last_invoked_at TIMESTAMP;
CREATE INDEX idx_emergency_plan_domain ON fac_emergency_plan (domain);

-- 为 7 条既有预案标注业务域与核预案标记（按预案名称精确更新，避免依赖自增 id）
UPDATE fac_emergency_plan SET domain='production', nuclear=TRUE  WHERE plan_name='乙烯储罐火灾处置方案';
UPDATE fac_emergency_plan SET domain='production', nuclear=FALSE WHERE plan_name='液氨泄漏现场处置方案';
UPDATE fac_emergency_plan SET domain='fire',       nuclear=FALSE WHERE plan_name='乙烯装置消防救援处置方案';
UPDATE fac_emergency_plan SET domain='fire',       nuclear=FALSE WHERE plan_name='储罐区泡沫灭火救援预案';
UPDATE fac_emergency_plan SET domain='production', nuclear=TRUE  WHERE plan_name='茂名石化应急预案';
UPDATE fac_emergency_plan SET domain='production', nuclear=FALSE WHERE plan_name='茂名石化综合应急预案（修订版）';
UPDATE fac_emergency_plan SET domain='superior',   nuclear=FALSE WHERE plan_name='广东省石化行业应急预案';

CREATE TABLE fac_emergency_plan_invoke_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    domain VARCHAR(32),
    operator VARCHAR(64),
    invoke_note VARCHAR(256),
    invoke_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
