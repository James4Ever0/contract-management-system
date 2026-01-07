package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dify配置实体类
 * 用于存储Dify API的动态配置信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dify_config")
public class DifyConfigEntity extends BaseEntity {

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
     * 名称（兼容字段）
     */
    public String getName() {
        return configName;
    }

    public void setName(String name) {
        this.configName = name;
    }

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * Dify API地址
     */
    private String apiUrl;

    /**
     * Dify API密钥
     */
    private String apiKey;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout;

    /**
     * 最大重试次数（对应数据库字段retry_count）
     */
    @TableField("retry_count")
    private Integer maxRetries;

    /**
     * 备注
     */
    private String remark;
}
