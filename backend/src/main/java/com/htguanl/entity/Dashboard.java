package com.htguanl.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据
 */
@Data
public class Dashboard {

    /**
     * 合同总数
     */
    private Long totalContracts;

    /**
     * 执行中合同数
     */
    private Long executingContracts;

    /**
     * 即将到期合同数（30天内）
     */
    private Long expiringContracts;

    /**
     * 已到期合同数
     */
    private Long expiredContracts;

    /**
     * 合同总金额
     */
    private BigDecimal totalAmount;

    /**
     * 已收金额
     */
    private BigDecimal receivedAmount;

    /**
     * 未收金额
     */
    private BigDecimal outstandingAmount;

    /**
     * 客户总数
     */
    private Long totalCustomers;

    /**
     * 风险预警数
     */
    private Long riskWarnings;

    /**
     * 待审批数
     */
    private Long pendingApprovals;

    /**
     * 最近合同列表
     */
    private List<Contract> recentContracts;

    /**
     * 收款趋势
     */
    private List<Map<String, Object>> paymentTrend;

    /**
     * 合同类型分布
     */
    private List<Map<String, Object>> contractTypeDistribution;
}

