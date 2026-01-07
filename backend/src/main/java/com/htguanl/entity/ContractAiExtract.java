package com.htguanl.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合同AI提取数据实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract_ai_extract")
public class ContractAiExtract extends BaseEntity {

    /**
     * 关联的合同ID
     */
    private Long contractId;

    /**
     * 关联的附件ID
     */
    private Long attachmentId;

    /**
     * 提取状态: PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败
     */
    private String extractStatus;

    /**
     * 合同编号（AI提取）
     */
    private String contractNo;

    /**
     * 合同名称（AI提取）
     */
    private String contractName;

    /**
     * 合同类型（AI提取）
     */
    private String contractType;

    /**
     * 客户名称（AI提取）
     */
    private String customerName;

    /**
     * 合同金额（AI提取）
     */
    private java.math.BigDecimal contractAmount;

    /**
     * 签署日期（AI提取）
     */
    private LocalDateTime signDate;

    /**
     * 开始日期（AI提取）
     */
    private LocalDateTime startDate;

    /**
     * 结束日期（AI提取）
     */
    private LocalDateTime endDate;

    /**
     * 付款条款（AI提取，JSON格式）
     */
    private String paymentTerms;

    /**
     * 交付条款（AI提取，JSON格式）
     */
    private String deliveryTerms;

    /**
     * 质量条款（AI提取，JSON格式）
     */
    private String qualityTerms;

    /**
     * 违约责任（AI提取，文本）
     */
    private String breachClause;

    /**
     * 其他条款（AI提取，文本）
     */
    private String otherTerms;

    /**
     * 原始数据（Dify返回的完整数据，JSON格式）
     */
    private String rawData;

    /**
     * 提取开始时间
     */
    private LocalDateTime extractStartTime;

    /**
     * 提取完成时间
     */
    private LocalDateTime extractEndTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * PDF内容
     */
    private String pdfContent;

    /**
     * 提取数据（兼容字段）
     */
    private String extractData; // json data

    /**
     * 提取时间（兼容字段）
     */
    private java.time.LocalDateTime extractTime;
}
