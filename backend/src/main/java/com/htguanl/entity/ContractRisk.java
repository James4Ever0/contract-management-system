package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合同风险实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract_risk")
public class ContractRisk extends BaseEntity {

    private Long contractId;

    /**
     * 风险类型: EXPIRY-到期风险, PAYMENT-收款风险, PERFORMANCE-履约风险
     */
    private String riskType;

    /**
     * 风险等级: LOW-低, MEDIUM-中, HIGH-高, CRITICAL-严重
     */
    private String riskLevel;

    private String riskDescription;

    @TableField(exist = false)
    private String riskRule;

    @TableField(exist = false)
    private String riskValue;

    /**
     * 风险状态: PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, IGNORED-已忽略
     */
    private String riskStatus;

    @TableField(exist = false)
    private String handleMeasure;

    /**
     * 是否已解决：0-未解决，1-已解决
     */
    private Integer isResolved;

    @TableField(exist = false)
    private LocalDateTime resolveTime;

    @TableField(exist = false)
    private Long resolveBy;

    @TableField(exist = false)
    private String resolveRemark;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private String contractName;

    @TableField(exist = false)
    private String customerName;
}

