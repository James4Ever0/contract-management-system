package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LDAP配置实体类
 * 用于存储动态LDAP配置信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ldap_config")
public class LdapConfigEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 名称（兼容方法）
     */
    public String getName() {
        return configName;
    }

    public void setName(String name) {
        this.configName = name;
    }

    /**
     * 是否启用LDAP认证
     */
    private Boolean enabled;

    /**
     * LDAP服务器地址
     */
    private String host;

    /**
     * LDAP服务器端口
     */
    private Integer port;

    /**
     * 基础DN
     */
    private String baseDn;

    /**
     * 管理员DN
     */
    private String userDn;

    /**
     * 管理员密码（加密存储）
     */
    private String password;

    /**
     * 用户搜索基础DN（不在数据库表中）
     */
    @TableField(exist = false)
    private String userSearchBase;

    /**
     * 用户搜索过滤器
     */
    private String userSearchFilter;

    /**
     * 用户ID属性（不在数据库表中，使用userSearchAttribute代替）
     */
    @TableField(exist = false)
    private String userIdAttribute;

    /**
     * 用户搜索属性（对应数据库字段user_search_attribute）
     */
    private String userSearchAttribute;

    /**
     * 用户邮箱属性（不在数据库表中）
     */
    @TableField(exist = false)
    private String userEmailAttribute;

    /**
     * 用户姓名属性（不在数据库表中）
     */
    @TableField(exist = false)
    private String userNameAttribute;

    /**
     * 是否启用用户同步
     */
    private Boolean syncEnabled;

    /**
     * 同步间隔（分钟，不在数据库表中）
     */
    @TableField(exist = false)
    private Integer syncInterval;

    /**
     * 连接超时时间（毫秒，不在数据库表中）
     */
    @TableField(exist = false)
    private Integer connectTimeout;

    /**
     * 操作超时时间（毫秒，不在数据库表中）
     */
    @TableField(exist = false)
    private Integer operationTimeout;

    /**
     * 备注
     */
    private String remark;
}
