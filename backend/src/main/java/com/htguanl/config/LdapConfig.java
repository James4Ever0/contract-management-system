package com.htguanl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LDAP配置类
 * 从 application.yml 读取LDAP配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "ldap")
public class LdapConfig {

    /**
     * LDAP认证开关
     */
    private Boolean enabled = false;

    /**
     * LDAP服务器地址
     */
    private String host;

    /**
     * LDAP服务器端口
     */
    private Integer port = 389;

    /**
     * 基础专有名称
     */
    private String baseDn;

    /**
     * 用户专有名称（管理员DN）
     */
    private String userDn;

    /**
     * LDAP服务器连接密码
     */
    private String password;

    /**
     * 本地账号同步开关
     */
    private Boolean syncEnabled = false;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 操作超时时间（毫秒）
     */
    private Integer operationTimeout = 10000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;
}
