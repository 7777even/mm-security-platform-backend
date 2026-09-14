-- =============================================================================
-- V47 业务写侧 4 张记录表（postgresql 方言）—— 与 h2/V47 语义完全一致
--   方言翻译：AUTO_INCREMENT → BIGSERIAL；VARCHAR 不变；
--   TIMESTAMP 默认值 CURRENT_TIMESTAMP（PG 原生支持）。
--   未实跑验证：本机无 PG 实例、无 docker，待 PG 环境激活后 flyway 校验。
-- =============================================================================

CREATE TABLE fac_emergency_command_record (
    id BIGSERIAL PRIMARY KEY,
    command_code VARCHAR(32) NOT NULL,
    command_name VARCHAR(64) NOT NULL,
    command_kind VARCHAR(32),
    prev_status VARCHAR(16),
    curr_status VARCHAR(16) NOT NULL,
    dispatch_mode VARCHAR(32),
    target VARCHAR(128),
    remark VARCHAR(255),
    operator VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_ecr_command_code ON fac_emergency_command_record (command_code);

CREATE TABLE fac_typhoon_dispatch_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    resource_code VARCHAR(32) NOT NULL,
    resource_name VARCHAR(64),
    dispatch_action VARCHAR(16) NOT NULL,
    prev_status VARCHAR(16),
    curr_status VARCHAR(16) NOT NULL,
    assignee VARCHAR(64),
    quantity INT,
    remark VARCHAR(255),
    operator VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_tdo_order_no ON fac_typhoon_dispatch_order (order_no);

CREATE TABLE fac_patrol_execution (
    id BIGSERIAL PRIMARY KEY,
    patrol_date VARCHAR(10) NOT NULL,
    shift_name VARCHAR(16),
    duty_person VARCHAR(32) NOT NULL,
    patrol_count VARCHAR(16),
    location VARCHAR(64),
    exec_result VARCHAR(16) NOT NULL,
    finding VARCHAR(255),
    work_order_no VARCHAR(32),
    operator VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_pe_patrol_date ON fac_patrol_execution (patrol_date);

CREATE TABLE fac_duty_sign_in (
    id BIGSERIAL PRIMARY KEY,
    duty_date VARCHAR(10) NOT NULL,
    shift_name VARCHAR(16),
    department VARCHAR(64),
    person_name VARCHAR(32) NOT NULL,
    sign_action VARCHAR(16) NOT NULL,
    sign_time VARCHAR(19),
    remark VARCHAR(255),
    operator VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_dsi_duty_date ON fac_duty_sign_in (duty_date);
