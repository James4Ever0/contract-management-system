-- 合同管理系统数据库初始化脚本
-- 数据库: PostgreSQL
-- 版本: 1.0.0
-- 创建日期: 2025-12-25

-- ============================================
-- 1. 创建数据库
-- ============================================
-- CREATE DATABASE contract_management_db
--     WITH 
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'zh_CN.UTF-8'
--     LC_CTYPE = 'zh_CN.UTF-8'
--     TABLESPACE = pg_default
--     CONNECTION LIMIT = -1;

-- ============================================
-- 2. 创建扩展
-- ============================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- 3. 创建枚举类型
-- ============================================

-- 合同状态枚举
CREATE TYPE contract_status_enum AS ENUM (
    'DRAFT',           -- 草稿
    'PENDING_APPROVAL',-- 待审批
    'APPROVED',        -- 已审批
    'REJECTED',        -- 已驳回
    'SIGNED',          -- 已签署
    'EXECUTING',       -- 执行中
    'COMPLETED',       -- 已完成
    'TERMINATED',      -- 已终止
    'ARCHIVED'         -- 已归档
);

-- 审批状态枚举
CREATE TYPE approval_status_enum AS ENUM (
    'PENDING',         -- 待审批
    'APPROVED',        -- 已通过
    'REJECTED',        -- 已驳回
    'CANCELLED'        -- 已取消
);

-- 印章状态枚举
CREATE TYPE seal_status_enum AS ENUM (
    'AVAILABLE',       -- 可用
    'BORROWED',        -- 已借出
    'MAINTENANCE',     -- 维护中
    'LOST'             -- 已遗失
);

-- 收款状态枚举
CREATE TYPE payment_status_enum AS ENUM (
    'UNPAID',          -- 未支付
    'PARTIAL_PAID',    -- 部分支付
    'PAID',            -- 已支付
    'OVERDUE'          -- 逾期
);

-- 风险等级枚举
CREATE TYPE risk_level_enum AS ENUM (
    'LOW',             -- 低风险
    'MEDIUM',          -- 中风险
    'HIGH',            -- 高风险
    'CRITICAL'         -- 严重风险
);

-- ============================================
-- 4. 创建基础数据表
-- ============================================

-- 4.1 用户表
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    department_id BIGINT,
    position VARCHAR(100),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    remark TEXT
);

-- 4.2 部门表
CREATE TABLE sys_department (
    id BIGSERIAL PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    dept_code VARCHAR(50),
    leader_id BIGINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark TEXT
);

-- 4.3 角色表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark TEXT
);

-- 4.4 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

-- 4.5 权限表
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    resource_type VARCHAR(20),
    resource_url VARCHAR(200),
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark TEXT
);

-- 4.6 角色权限关联表
CREATE TABLE sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

-- ============================================
-- 5. 创建业务数据表
-- ============================================

-- 5.1 客户信息表
CREATE TABLE biz_customer (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(200) NOT NULL,
    customer_type VARCHAR(20),
    credit_level VARCHAR(20),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address VARCHAR(500),
    tax_number VARCHAR(50),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    remark TEXT
);

-- 5.2 合同登记表
CREATE TABLE biz_contract (
    id BIGSERIAL PRIMARY KEY,
    contract_no VARCHAR(50) NOT NULL UNIQUE,
    contract_name VARCHAR(200) NOT NULL,
    contract_type VARCHAR(50),
    customer_id BIGINT NOT NULL,
    contract_amount DECIMAL(18,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    sign_date DATE,
    start_date DATE,
    end_date DATE,
    service_period INT,
    contract_status contract_status_enum DEFAULT 'DRAFT',
    payment_terms TEXT,
    delivery_terms TEXT,
    quality_terms TEXT,
    breach_clause TEXT,
    other_terms TEXT,
    attachment_url VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    remark TEXT,
    CONSTRAINT fk_contract_customer FOREIGN KEY (customer_id) REFERENCES biz_customer(id)
);

-- 5.3 合同审批记录表
CREATE TABLE biz_contract_approval (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    approval_no VARCHAR(50) NOT NULL UNIQUE,
    current_approver_id BIGINT,
    approval_status approval_status_enum DEFAULT 'PENDING',
    approval_level INT,
    approval_comment TEXT,
    approval_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.4 审批流程明细表
CREATE TABLE biz_approval_flow (
    id BIGSERIAL PRIMARY KEY,
    approval_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approval_order INT NOT NULL,
    approval_status approval_status_enum DEFAULT 'PENDING',
    approval_comment TEXT,
    approval_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flow_approval FOREIGN KEY (approval_id) REFERENCES biz_contract_approval(id)
);

-- 5.5 印章信息表
CREATE TABLE biz_seal (
    id BIGSERIAL PRIMARY KEY,
    seal_name VARCHAR(100) NOT NULL,
    seal_type VARCHAR(50),
    seal_code VARCHAR(50) NOT NULL UNIQUE,
    seal_status seal_status_enum DEFAULT 'AVAILABLE',
    keeper_id BIGINT,
    department_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark TEXT
);

-- 5.6 印章借用登记表
CREATE TABLE biz_seal_borrow (
    id BIGSERIAL PRIMARY KEY,
    borrow_no VARCHAR(50) NOT NULL UNIQUE,
    seal_id BIGINT NOT NULL,
    contract_id BIGINT,
    borrower_id BIGINT NOT NULL,
    borrow_reason TEXT,
    borrow_time TIMESTAMP,
    expected_return_time TIMESTAMP,
    actual_return_time TIMESTAMP,
    borrow_status VARCHAR(20) DEFAULT 'BORROWED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_borrow_seal FOREIGN KEY (seal_id) REFERENCES biz_seal(id),
    CONSTRAINT fk_borrow_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.7 合同文件资料接收登记表
CREATE TABLE biz_contract_document (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(50) NOT NULL UNIQUE,
    contract_id BIGINT NOT NULL,
    document_name VARCHAR(200) NOT NULL,
    document_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    file_format VARCHAR(20),
    receiver_id BIGINT,
    receive_time TIMESTAMP,
    sender VARCHAR(100),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.8 在网客户信息统计表
CREATE TABLE biz_customer_statistics (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    service_start_date DATE NOT NULL,
    service_end_date DATE NOT NULL,
    service_status VARCHAR(20) DEFAULT 'ACTIVE',
    service_amount DECIMAL(18,2),
    renewal_count INT DEFAULT 0,
    last_renewal_date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_statistics_customer FOREIGN KEY (customer_id) REFERENCES biz_customer(id),
    CONSTRAINT fk_statistics_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.9 合同收款及返款明细表
CREATE TABLE biz_payment (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    contract_id BIGINT NOT NULL,
    payment_type VARCHAR(20),
    payment_amount DECIMAL(18,2) NOT NULL,
    payment_status payment_status_enum DEFAULT 'UNPAID',
    planned_payment_date DATE,
    actual_payment_date DATE,
    payment_method VARCHAR(50),
    payment_account VARCHAR(100),
    payer VARCHAR(100),
    receiver VARCHAR(100),
    invoice_no VARCHAR(50),
    invoice_status VARCHAR(20),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    CONSTRAINT fk_payment_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.10 合同台账表
CREATE TABLE biz_contract_ledger (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL UNIQUE,
    contract_no VARCHAR(50) NOT NULL,
    contract_name VARCHAR(200) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    contract_amount DECIMAL(18,2),
    signed_amount DECIMAL(18,2),
    received_amount DECIMAL(18,2) DEFAULT 0,
    returned_amount DECIMAL(18,2) DEFAULT 0,
    outstanding_amount DECIMAL(18,2),
    contract_status contract_status_enum,
    sign_date DATE,
    start_date DATE,
    end_date DATE,
    days_remaining INT,
    is_expiring BOOLEAN DEFAULT FALSE,
    is_expired BOOLEAN DEFAULT FALSE,
    risk_level risk_level_enum DEFAULT 'LOW',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.11 合同风险提示表
CREATE TABLE biz_contract_risk (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    risk_type VARCHAR(50) NOT NULL,
    risk_level risk_level_enum NOT NULL,
    risk_description TEXT NOT NULL,
    risk_rule VARCHAR(100),
    risk_value VARCHAR(200),
    is_resolved BOOLEAN DEFAULT FALSE,
    resolve_time TIMESTAMP,
    resolve_by BIGINT,
    resolve_remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id)
);

-- 5.12 收款提醒记录表
CREATE TABLE biz_payment_reminder (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    payment_id BIGINT,
    reminder_type VARCHAR(20) NOT NULL,
    reminder_days INT NOT NULL,
    reminder_time TIMESTAMP NOT NULL,
    reminder_status VARCHAR(20) DEFAULT 'PENDING',
    reminder_method VARCHAR(50),
    reminder_content TEXT,
    recipient_id BIGINT,
    recipient_email VARCHAR(100),
    recipient_phone VARCHAR(20),
    send_time TIMESTAMP,
    send_result TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_contract FOREIGN KEY (contract_id) REFERENCES biz_contract(id),
    CONSTRAINT fk_reminder_payment FOREIGN KEY (payment_id) REFERENCES biz_payment(id)
);

-- 5.13 系统日志表
CREATE TABLE sys_log (
    id BIGSERIAL PRIMARY KEY,
    log_type VARCHAR(20),
    module_name VARCHAR(100),
    operation VARCHAR(100),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params TEXT,
    response_result TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(100),
    ip_address VARCHAR(50),
    execute_time BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5.14 系统配置表
CREATE TABLE sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_type VARCHAR(20),
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 6. 创建索引
-- ============================================

-- 用户表索引
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_department ON sys_user(department_id);
CREATE INDEX idx_user_status ON sys_user(status);

-- 部门表索引
CREATE INDEX idx_dept_parent ON sys_department(parent_id);
CREATE INDEX idx_dept_status ON sys_department(status);

-- 客户表索引
CREATE INDEX idx_customer_code ON biz_customer(customer_code);
CREATE INDEX idx_customer_name ON biz_customer(customer_name);
CREATE INDEX idx_customer_type ON biz_customer(customer_type);

-- 合同表索引
CREATE INDEX idx_contract_no ON biz_contract(contract_no);
CREATE INDEX idx_contract_customer ON biz_contract(customer_id);
CREATE INDEX idx_contract_status ON biz_contract(contract_status);
CREATE INDEX idx_contract_dates ON biz_contract(start_date, end_date);
CREATE INDEX idx_contract_create_by ON biz_contract(create_by);

-- 审批表索引
CREATE INDEX idx_approval_contract ON biz_contract_approval(contract_id);
CREATE INDEX idx_approval_status ON biz_contract_approval(approval_status);
CREATE INDEX idx_approval_approver ON biz_contract_approval(current_approver_id);

-- 印章表索引
CREATE INDEX idx_seal_code ON biz_seal(seal_code);
CREATE INDEX idx_seal_status ON biz_seal(seal_status);

-- 印章借用表索引
CREATE INDEX idx_borrow_seal ON biz_seal_borrow(seal_id);
CREATE INDEX idx_borrow_contract ON biz_seal_borrow(contract_id);
CREATE INDEX idx_borrow_borrower ON biz_seal_borrow(borrower_id);
CREATE INDEX idx_borrow_status ON biz_seal_borrow(borrow_status);

-- 文档表索引
CREATE INDEX idx_document_contract ON biz_contract_document(contract_id);
CREATE INDEX idx_document_type ON biz_contract_document(document_type);

-- 客户统计表索引
CREATE INDEX idx_statistics_customer ON biz_customer_statistics(customer_id);
CREATE INDEX idx_statistics_contract ON biz_customer_statistics(contract_id);
CREATE INDEX idx_statistics_dates ON biz_customer_statistics(service_start_date, service_end_date);
CREATE INDEX idx_statistics_status ON biz_customer_statistics(service_status);

-- 收款表索引
CREATE INDEX idx_payment_contract ON biz_payment(contract_id);
CREATE INDEX idx_payment_status ON biz_payment(payment_status);
CREATE INDEX idx_payment_date ON biz_payment(planned_payment_date);
CREATE INDEX idx_payment_type ON biz_payment(payment_type);

-- 合同台账索引
CREATE INDEX idx_ledger_contract ON biz_contract_ledger(contract_id);
CREATE INDEX idx_ledger_status ON biz_contract_ledger(contract_status);
CREATE INDEX idx_ledger_expiring ON biz_contract_ledger(is_expiring);
CREATE INDEX idx_ledger_risk ON biz_contract_ledger(risk_level);

-- 风险表索引
CREATE INDEX idx_risk_contract ON biz_contract_risk(contract_id);
CREATE INDEX idx_risk_level ON biz_contract_risk(risk_level);
CREATE INDEX idx_risk_resolved ON biz_contract_risk(is_resolved);

-- 提醒表索引
CREATE INDEX idx_reminder_contract ON biz_payment_reminder(contract_id);
CREATE INDEX idx_reminder_payment ON biz_payment_reminder(payment_id);
CREATE INDEX idx_reminder_time ON biz_payment_reminder(reminder_time);
CREATE INDEX idx_reminder_status ON biz_payment_reminder(reminder_status);

-- 日志表索引
CREATE INDEX idx_log_type ON sys_log(log_type);
CREATE INDEX idx_log_operator ON sys_log(operator_id);
CREATE INDEX idx_log_time ON sys_log(create_time);

-- ============================================
-- 7. 创建视图
-- ============================================

-- 合同台账视图
CREATE OR REPLACE VIEW v_contract_ledger AS
SELECT 
    c.id AS contract_id,
    c.contract_no,
    c.contract_name,
    c.customer_id,
    cu.customer_name,
    c.contract_amount,
    c.sign_date,
    c.start_date,
    c.end_date,
    c.contract_status,
    COALESCE(SUM(CASE WHEN p.payment_type = 'RECEIVE' AND p.payment_status = 'PAID' THEN p.payment_amount ELSE 0 END), 0) AS received_amount,
    COALESCE(SUM(CASE WHEN p.payment_type = 'RETURN' AND p.payment_status = 'PAID' THEN p.payment_amount ELSE 0 END), 0) AS returned_amount,
    c.contract_amount - COALESCE(SUM(CASE WHEN p.payment_type = 'RECEIVE' AND p.payment_status = 'PAID' THEN p.payment_amount ELSE 0 END), 0) AS outstanding_amount,
    CASE 
        WHEN c.end_date < CURRENT_DATE THEN TRUE
        WHEN c.end_date <= CURRENT_DATE + INTERVAL '30 days' THEN TRUE
        ELSE FALSE
    END AS is_expiring,
    CASE WHEN c.end_date < CURRENT_DATE THEN TRUE ELSE FALSE END AS is_expired,
    CASE 
        WHEN c.end_date < CURRENT_DATE THEN 'HIGH'
        WHEN c.end_date <= CURRENT_DATE + INTERVAL '7 days' THEN 'HIGH'
        WHEN c.end_date <= CURRENT_DATE + INTERVAL '30 days' THEN 'MEDIUM'
        ELSE 'LOW'
    END AS risk_level
FROM biz_contract c
LEFT JOIN