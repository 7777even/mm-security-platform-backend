-- =============================================================================
-- V47 业务写侧 4 张记录表（达梦 DM8 方言）—— 与 h2/V47 语义完全一致
--   方言翻译：AUTO_INCREMENT → NUMBER(19) IDENTITY(1,1)；
--   VARCHAR → VARCHAR2；TIMESTAMP 默认值 CURRENT_TIMESTAMP（达梦支持）。
--   列名已规避达梦保留字（level/status 等加前缀，见 h2/V47 注释）。
--   未实跑验证：本机无 DM8 实例、无 docker，待 DM8 环境激活后 flyway 校验。
-- =============================================================================

CREATE TABLE fac_emergency_command_record (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    command_code VARCHAR2(32 CHAR) NOT NULL,
    command_name VARCHAR2(64 CHAR) NOT NULL,
    command_kind VARCHAR2(32 CHAR),
    prev_status VARCHAR2(16 CHAR),
    curr_status VARCHAR2(16 CHAR) NOT NULL,
    dispatch_mode VARCHAR2(32 CHAR),
    target VARCHAR2(128 CHAR),
    remark VARCHAR2(255 CHAR),
    operator VARCHAR2(32 CHAR) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_ecr_command_code ON fac_emergency_command_record (command_code);

CREATE TABLE fac_typhoon_dispatch_order (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    order_no VARCHAR2(32 CHAR) NOT NULL,
    resource_code VARCHAR2(32 CHAR) NOT NULL,
    resource_name VARCHAR2(64 CHAR),
    dispatch_action VARCHAR2(16 CHAR) NOT NULL,
    prev_status VARCHAR2(16 CHAR),
    curr_status VARCHAR2(16 CHAR) NOT NULL,
    assignee VARCHAR2(64 CHAR),
    quantity INT,
    remark VARCHAR2(255 CHAR),
    operator VARCHAR2(32 CHAR) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_tdo_order_no ON fac_typhoon_dispatch_order (order_no);

CREATE TABLE fac_patrol_execution (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    patrol_date VARCHAR2(10 CHAR) NOT NULL,
    shift_name VARCHAR2(16 CHAR),
    duty_person VARCHAR2(32 CHAR) NOT NULL,
    patrol_count VARCHAR2(16 CHAR),
    location VARCHAR2(64 CHAR),
    exec_result VARCHAR2(16 CHAR) NOT NULL,
    finding VARCHAR2(255 CHAR),
    work_order_no VARCHAR2(32 CHAR),
    operator VARCHAR2(32 CHAR) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_pe_patrol_date ON fac_patrol_execution (patrol_date);

CREATE TABLE fac_duty_sign_in (
    id NUMBER(19) IDENTITY(1,1) PRIMARY KEY,
    duty_date VARCHAR2(10 CHAR) NOT NULL,
    shift_name VARCHAR2(16 CHAR),
    department VARCHAR2(64 CHAR),
    person_name VARCHAR2(32 CHAR) NOT NULL,
    sign_action VARCHAR2(16 CHAR) NOT NULL,
    sign_time VARCHAR2(19 CHAR),
    remark VARCHAR2(255 CHAR),
    operator VARCHAR2(32 CHAR) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_dsi_duty_date ON fac_duty_sign_in (duty_date);
