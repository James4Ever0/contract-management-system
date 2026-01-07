package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 印章实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_seal")
public class Seal extends BaseEntity {

    @TableField(exist = false)
    private String sealCode;

    private String sealName;

    private String sealType;

    /**
     * 保管人姓名
     */
    @TableField(exist = false)
    private String keeper;

    /**
     * 保管人ID
     */
    @TableField(exist = false)
    private Long keeperId;

    @TableField(exist = false)
    private Long departmentId;

    /**
     * 印章状态: AVAILABLE-可用, BORROWED-已借出, MAINTENANCE-维护中, LOST-已遗失
     */
    @TableField(exist = false)
    private String sealStatus;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String keeperName;
}

