package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer")
public class Customer extends BaseEntity {

    private String customerCode;

    private String customerName;

    private String customerType;

    private String creditLevel;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private String address;

    /**
     * 以下字段标记为不存在于数据库表中，避免查询时出错
     */
    @TableField(exist = false)
    private String taxNumber;

    @TableField(exist = false)
    private String bankName;

    @TableField(exist = false)
    private String bankAccount;

    /**
     * 状态：1-在网，0-离网
     */
    private Integer status;
}

