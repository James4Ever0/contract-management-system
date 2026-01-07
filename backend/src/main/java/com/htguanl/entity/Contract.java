package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合同实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract")
public class Contract extends BaseEntity {

    private String contractNo;

    private String contractName;

    private String contractType;

    private Long customerId;

    private BigDecimal contractAmount;

    @TableField(exist = false)
    private String currency;

    private LocalDate signDate;

    private LocalDate startDate;

    private LocalDate endDate;

    @TableField(exist = false)
    private Integer servicePeriod;

    /**
     * 合同状态: DRAFT-草稿, PENDING_APPROVAL-待审批, APPROVED-已审批,
     * REJECTED-已驳回, SIGNED-已签署, EXECUTING-执行中, COMPLETED-已完成,
     * TERMINATED-已终止, ARCHIVED-已归档
     */
    private String contractStatus;

    @TableField(exist = false)
    private String paymentTerms;

    @TableField(exist = false)
    private String deliveryTerms;

    @TableField(exist = false)
    private String qualityTerms;

    @TableField(exist = false)
    private String breachClause;

    @TableField(exist = false)
    private String otherTerms;

    @TableField(exist = false)
    private String attachmentUrl;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private Integer daysRemaining;
}

