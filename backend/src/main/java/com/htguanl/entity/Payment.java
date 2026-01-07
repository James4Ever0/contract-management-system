package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收款/返款实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_payment")
public class Payment extends BaseEntity {

    private String paymentNo;

    private Long contractId;

    /**
     * 款项类型：RECEIVE-收款, RETURN-返款
     */
    private String paymentType;

    private BigDecimal paymentAmount;

    /**
     * 收款状态: UNPAID-未支付, PARTIAL_PAID-部分支付, PAID-已支付, OVERDUE-逾期
     */
    private String paymentStatus;

    private LocalDate plannedPaymentDate;

    private LocalDate actualPaymentDate;

    @TableField(exist = false)
    private String paymentMethod;

    @TableField(exist = false)
    private String paymentAccount;

    @TableField(exist = false)
    private String payer;

    @TableField(exist = false)
    private String receiver;

    @TableField(exist = false)
    private String invoiceNo;

    @TableField(exist = false)
    private String invoiceStatus;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private String contractName;
}

