-- 客户表
CREATE TABLE IF NOT EXISTS biz_customer (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) UNIQUE,
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
    status INT DEFAULT 1,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 合同表
CREATE TABLE IF NOT EXISTS biz_contract (
    id BIGSERIAL PRIMARY KEY,
    contract_no VARCHAR(50) UNIQUE,
    contract_name VARCHAR(200) NOT NULL,
    contract_type VARCHAR(50),
    customer_id BIGINT,
    contract_amount DECIMAL(18,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    sign_date DATE,
    start_date DATE,
    end_date DATE,
    service_period INT,
    contract_status VARCHAR(20) DEFAULT 'DRAFT',
    payment_terms TEXT,
    delivery_terms TEXT,
    quality_terms TEXT,
    breach_clause TEXT,
    other_terms TEXT,
    attachment_url VARCHAR(500),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 收款表
CREATE TABLE IF NOT EXISTS biz_payment (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(50) UNIQUE,
    contract_id BIGINT,
    payment_type VARCHAR(20),
    payment_amount DECIMAL(18,2),
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
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
    update_by BIGINT
);

-- 风险预警表
CREATE TABLE IF NOT EXISTS biz_contract_risk (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT,
    risk_type VARCHAR(30),
    risk_level VARCHAR(10),
    risk_description TEXT,
    risk_rule VARCHAR(100),
    risk_value VARCHAR(100),
    risk_status VARCHAR(20) DEFAULT 'PENDING',
    handle_measure TEXT,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolve_time TIMESTAMP,
    resolve_by BIGINT,
    resolve_remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 印章表
CREATE TABLE IF NOT EXISTS biz_seal (
    id BIGSERIAL PRIMARY KEY,
    seal_code VARCHAR(50) UNIQUE,
    seal_name VARCHAR(100) NOT NULL,
    seal_type VARCHAR(20),
    keeper VARCHAR(50),
    keeper_id BIGINT,
    department_id BIGINT,
    seal_status VARCHAR(20) DEFAULT 'AVAILABLE',
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 印章借用表
CREATE TABLE IF NOT EXISTS biz_seal_borrow (
    id BIGSERIAL PRIMARY KEY,
    borrow_no VARCHAR(50) UNIQUE,
    seal_id BIGINT,
    contract_id BIGINT,
    borrower VARCHAR(50),
    borrower_id BIGINT,
    borrow_reason TEXT,
    borrow_time TIMESTAMP,
    expected_return_time TIMESTAMP,
    actual_return_time TIMESTAMP,
    borrow_status VARCHAR(20) DEFAULT 'BORROWED',
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    department_id BIGINT,
    position VARCHAR(100),
    status INT DEFAULT 1,
    ldap_dn VARCHAR(255),
    ldap_sync_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    remark TEXT
);

-- 合同附件表
CREATE TABLE IF NOT EXISTS biz_contract_attachment (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(50),
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upload_by BIGINT,
    ai_extract_status VARCHAR(20) DEFAULT 'PENDING',
    upload_status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 合同AI提取结果表
CREATE TABLE IF NOT EXISTS biz_contract_ai_extract (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT,
    attachment_id BIGINT,
    extract_status VARCHAR(20) DEFAULT 'PENDING',
    extract_data TEXT,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    pdf_content TEXT,
    dify_task_id VARCHAR(255),
    contract_no VARCHAR(50),
    contract_name VARCHAR(255),
    contract_type VARCHAR(50),
    customer_name TEXT,
    contract_amount DECIMAL(18,2),
    sign_date TIMESTAMP,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    payment_terms TEXT,
    delivery_terms TEXT,
    quality_terms TEXT,
    breach_clause TEXT,
    other_terms TEXT,
    raw_data TEXT,
    extract_start_time TIMESTAMP,
    extract_end_time TIMESTAMP,
    extract_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- LDAP配置表
CREATE TABLE IF NOT EXISTS sys_ldap_config (
    id BIGSERIAL PRIMARY KEY,
    config_name VARCHAR(100),
    enabled BOOLEAN DEFAULT FALSE,
    host VARCHAR(255),
    port INT DEFAULT 389,
    base_dn VARCHAR(255),
    user_dn VARCHAR(255),
    password VARCHAR(255),
    sync_enabled BOOLEAN DEFAULT FALSE,
    user_search_filter VARCHAR(255) DEFAULT '(uid={0})',
    user_search_attribute VARCHAR(50) DEFAULT 'uid',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    remark TEXT
);

-- Dify配置表
CREATE TABLE IF NOT EXISTS sys_dify_config (
    id BIGSERIAL PRIMARY KEY,
    config_name VARCHAR(100),
    enabled BOOLEAN DEFAULT FALSE,
    api_url VARCHAR(500),
    api_key VARCHAR(255),
    workflow_id VARCHAR(255),
    timeout INT DEFAULT 30000,
    retry_count INT DEFAULT 3,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    remark TEXT
);

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    module VARCHAR(50),
    ip_address VARCHAR(50),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params TEXT,
    response_status INT,
    response_time INT,
    error_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入默认管理员用户（密码为 admin123，使用BCrypt加密）
-- BCrypt密码哈希值是通过 passwordEncoder.encode("admin123") 生成
INSERT INTO sys_user (username, password, real_name, email, status)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKIcTQw6/XkKKwKGG3YZPLQVj5de', '系统管理员', 'admin@htguanl.com', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');
