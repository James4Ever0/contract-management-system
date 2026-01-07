-- 初始化客户数据
INSERT INTO biz_customer (customer_code, customer_name, customer_type, contact_person, contact_phone, credit_level, status) VALUES
('CUS001', '北京科技有限公司', 'ENTERPRISE', '张三', '13800138001', 'A', 1);
INSERT INTO biz_customer (customer_code, customer_name, customer_type, contact_person, contact_phone, credit_level, status) VALUES
('CUS002', '上海贸易有限公司', 'ENTERPRISE', '李四', '13800138002', 'B', 1);
INSERT INTO biz_customer (customer_code, customer_name, customer_type, contact_person, contact_phone, credit_level, status) VALUES
('CUS003', '广州服务有限公司', 'ENTERPRISE', '王五', '13800138003', 'A', 1);
INSERT INTO biz_customer (customer_code, customer_name, customer_type, contact_person, contact_phone, credit_level, status) VALUES
('CUS004', '深圳创新科技', 'ENTERPRISE', '赵六', '13800138004', 'C', 1);
INSERT INTO biz_customer (customer_code, customer_name, customer_type, contact_person, contact_phone, credit_level, status) VALUES
('CUS005', '杭州电商有限公司', 'ENTERPRISE', '钱七', '13800138005', 'B', 1);

-- 初始化合同数据
INSERT INTO biz_contract (contract_no, contract_name, customer_id, contract_type, contract_amount, sign_date, start_date, end_date, contract_status) VALUES
('HT2024001', '软件开发服务合同', 1, 'SERVICE', 500000.00, '2024-01-15', '2024-02-01', '2025-01-31', 'EXECUTING');
INSERT INTO biz_contract (contract_no, contract_name, customer_id, contract_type, contract_amount, sign_date, start_date, end_date, contract_status) VALUES
('HT2024002', '设备采购合同', 2, 'PURCHASE', 280000.00, '2024-02-20', '2024-03-01', '2024-12-31', 'EXECUTING');
INSERT INTO biz_contract (contract_no, contract_name, customer_id, contract_type, contract_amount, sign_date, start_date, end_date, contract_status) VALUES
('HT2024003', '技术咨询服务合同', 3, 'SERVICE', 150000.00, '2024-03-10', '2024-04-01', '2025-03-31', 'EXECUTING');
INSERT INTO biz_contract (contract_no, contract_name, customer_id, contract_type, contract_amount, sign_date, start_date, end_date, contract_status) VALUES
('HT2024004', '产品销售合同', 4, 'SALES', 320000.00, '2024-04-05', '2024-04-15', '2025-04-14', 'APPROVED');
INSERT INTO biz_contract (contract_no, contract_name, customer_id, contract_type, contract_amount, sign_date, start_date, end_date, contract_status) VALUES
('HT2024005', '年度维护服务合同', 5, 'SERVICE', 80000.00, '2024-05-01', '2024-05-15', '2025-05-14', 'DRAFT');

-- 初始化收款数据
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date, actual_payment_date) VALUES
('PAY2024001', 1, 'RECEIVE', 150000.00, 'PAID', '2024-02-15', '2024-02-14');
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date, actual_payment_date) VALUES
('PAY2024002', 1, 'RECEIVE', 200000.00, 'PAID', '2024-06-15', '2024-06-20');
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date) VALUES
('PAY2024003', 1, 'RECEIVE', 150000.00, 'PENDING', '2025-01-15');
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date, actual_payment_date) VALUES
('PAY2024004', 2, 'RECEIVE', 140000.00, 'PAID', '2024-03-15', '2024-03-15');
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date) VALUES
('PAY2024005', 2, 'RECEIVE', 140000.00, 'PENDING', '2024-09-15');
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date, actual_payment_date) VALUES
('PAY2024006', 3, 'RECEIVE', 75000.00, 'PAID', '2024-04-15', '2024-04-18');
INSERT INTO biz_payment (payment_no, contract_id, payment_type, payment_amount, payment_status, planned_payment_date) VALUES
('PAY2024007', 3, 'RECEIVE', 75000.00, 'PENDING', '2024-10-15');

-- 初始化风险数据
INSERT INTO biz_contract_risk (contract_id, risk_type, risk_level, risk_description, risk_status, is_resolved) VALUES
(2, 'CONTRACT_EXPIRING', 'MEDIUM', '合同将于2024-12-31到期，请及时续签', 'PENDING', 0);
INSERT INTO biz_contract_risk (contract_id, risk_type, risk_level, risk_description, risk_status, is_resolved) VALUES
(5, 'PAYMENT_OVERDUE', 'HIGH', '收款计划已逾期15天', 'PROCESSING', 0);

-- 初始化印章数据
INSERT INTO biz_seal (seal_name, seal_type, keeper, status) VALUES
('公司公章', 'OFFICIAL', '行政部', 'AVAILABLE');
INSERT INTO biz_seal (seal_name, seal_type, keeper, status) VALUES
('合同专用章', 'CONTRACT', '法务部', 'AVAILABLE');
INSERT INTO biz_seal (seal_name, seal_type, keeper, status) VALUES
('财务专用章', 'FINANCE', '财务部', 'BORROWED');
