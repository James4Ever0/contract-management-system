package com.htguanl.service;

import com.htguanl.entity.Dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    @Autowired
    private ContractService contractService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RiskService riskService;

    public Dashboard getDashboardData() {
        Dashboard dashboard = new Dashboard();

        // 合同统计
        dashboard.setTotalContracts(contractService.count());
        dashboard.setExecutingContracts(contractService.countByStatus("EXECUTING"));
        dashboard.setExpiringContracts(contractService.countExpiring());
        dashboard.setExpiredContracts(contractService.countExpired());

        // 金额统计
        BigDecimal totalAmount = contractService.getTotalAmount();
        BigDecimal receivedAmount = paymentService.getTotalReceived();
        dashboard.setTotalAmount(totalAmount);
        dashboard.setReceivedAmount(receivedAmount);
        dashboard.setOutstandingAmount(totalAmount.subtract(receivedAmount != null ? receivedAmount : BigDecimal.ZERO));

        // 客户统计
        dashboard.setTotalCustomers(customerService.countTotal());

        // 风险统计
        dashboard.setRiskWarnings(riskService.countUnresolved());

        // 待审批统计
        dashboard.setPendingApprovals(contractService.countByStatus("PENDING_APPROVAL"));

        // 最近合同
        dashboard.setRecentContracts(contractService.getRecentContracts(5));

        // 收款趋势
        dashboard.setPaymentTrend(paymentService.getPaymentTrend());

        // 合同类型分布
        dashboard.setContractTypeDistribution(contractService.getContractTypeDistribution());

        return dashboard;
    }
}

