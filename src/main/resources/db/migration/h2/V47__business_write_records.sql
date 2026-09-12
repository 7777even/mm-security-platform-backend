-- =============================================================================
-- V47 业务写侧 4 张记录表（H2 方言）—— A2「D 类写侧」落地之 D2 数据建模
--   背景：应急指令下发/状态推进、台风资源调度、巡更、值班签到 4 域此前后端
--        全部 GET 只读、前端无写 API（是"未实现写"，非被红线拦）。
--        经拍板 D1=全启用、D2=新增独立业务表，本迁移提供写侧落库载体。
--   边界（D4 拍板）：fac_emergency_command_record 仅记录<b>系统内部</b>指令与
--        状态推进，严禁据此触发物理设备；任何物理下行仍由 HardControlPaths
--        红线在前后端双重拦截，与本表无关。
--   不污染读表：既有 fac_fire_patrol（巡查计划/记录）与
--        fac_typhoon_dispatch_resource（资源清单）仍保持只读清单语义，
--        写操作落在本次新增的执行/单据表上。
--   审计（D3）：每张表带 operator + created_at/updated_at；状态推进类额外带
--        prev_/curr_ 前后值；完整变更审计另由 SystemAuditHelper 落
--        fac_audit_log（尽力而为，绝不阻断主流程）。
--   坑位：level/status/time/type/value/command/mode/seq 为 H2 保留字风险列名，
--        统一加前缀（cmd_status / record_status / sign_time / resource_type 等）。
--   方言：仅 H2（与 V46 一致）。达梦 / PG 镜像待两库激活时补，见 R5。
-- =============================================================================

-- 1) 应急指令下发与状态推进记录（系统内部指令，不含任何物理触发）
CREATE TABLE fac_emergency_command_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 2) 台风资源调度单（指派 / 确认 / 释放）
CREATE TABLE fac_typhoon_dispatch_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 3) 巡更执行记录（打卡 / 结果上报）
CREATE TABLE fac_patrol_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- 4) 值班签到记录（签到 / 签退）
CREATE TABLE fac_duty_sign_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
