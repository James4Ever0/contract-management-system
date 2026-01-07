package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private Long departmentId;

    /**
     * 部门名称（不在数据库表中）
     */
    @TableField(exist = false)
    private String department;

    private String position;

    /**
     * 用户角色（不在数据库表中）
     */
    @TableField(exist = false)
    private String role;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * LDAP DN（数据库表中可能不存在）
     */
    @TableField(exist = false)
    private String ldapDn;

    /**
     * 是否启用LDAP（不在数据库表中）
     */
    @TableField(exist = false)
    private Boolean ldapEnabled;

    /**
     * LDAP同步时间（数据库表中可能不存在）
     */
    @TableField(exist = false)
    private java.time.LocalDateTime ldapSyncTime;
}

