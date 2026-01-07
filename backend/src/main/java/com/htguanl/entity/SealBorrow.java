package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 印章借用实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_seal_borrow")
public class SealBorrow extends BaseEntity {

    private String borrowNo;

    private Long sealId;

    private Long contractId;

    /**
     * 借用人姓名（直接存储）
     */
    private String borrower;

    /**
     * 借用人ID（关联sys_user）
     */
    private Long borrowerId;

    private String borrowReason;

    private LocalDateTime borrowTime;

    private LocalDateTime expectedReturnTime;

    private LocalDateTime actualReturnTime;

    /**
     * 借用状态: BORROWED-已借出, RETURNED-已归还, OVERDUE-逾期未还
     */
    private String borrowStatus;

    @TableField(exist = false)
    private String sealName;

    @TableField(exist = false)
    private String sealType;

    @TableField(exist = false)
    private String borrowerName;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private String contractName;
}

