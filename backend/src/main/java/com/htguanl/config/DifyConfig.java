package com.htguanl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify API配置类
 * 读取application.yml中的dify配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyConfig {

    /**
     * 是否启用Dify
     */
    private Boolean enabled = true;

    /**
     * Dify API端点
     */
    private String apiUrl;

    /**
     * Dify API密钥
     */
    private String apiKey;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 重试间隔（毫秒）
     */
    private Integer retryInterval = 1000;
}
