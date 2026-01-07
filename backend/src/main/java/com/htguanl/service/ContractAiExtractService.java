package com.htguanl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htguanl.entity.ContractAiExtract;
import com.htguanl.entity.ContractAttachment;
import com.htguanl.mapper.ContractAiExtractMapper;
import com.htguanl.mapper.ContractAttachmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 合同AI提取服务类
 * 处理AI提取的合同数据
 */
@Slf4j
@Service
public class ContractAiExtractService extends ServiceImpl<ContractAiExtractMapper, ContractAiExtract> {

    @Autowired
    private ContractAiExtractMapper aiExtractMapper;

    @Autowired
    private ContractAttachmentMapper attachmentMapper;

    @Autowired
    private DifyService difyService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private PdfParserService pdfParserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建AI提取记录
     */
    public ContractAiExtract createExtractRecord(Long contractId, Long attachmentId) {
        ContractAiExtract extract = new ContractAiExtract();
        extract.setContractId(contractId);
        extract.setAttachmentId(attachmentId);
        extract.setExtractStatus("PENDING");
        extract.setCreateTime(LocalDateTime.now());
        extract.setUpdateTime(LocalDateTime.now());
        aiExtractMapper.insert(extract);
        return extract;
    }

    /**
     * 异步执行AI提取
     */
    @Async
    public void asyncExtractContractData(Long extractId) {
        ContractAiExtract extract = aiExtractMapper.selectById(extractId);
        if (extract == null) {
            log.error("AI提取记录不存在: {}", extractId);
            return;
        }

        try {
            // 更新状态为处理中
            extract.setExtractStatus("PROCESSING");
            extract.setUpdateTime(LocalDateTime.now());
            aiExtractMapper.updateById(extract);

            // 获取附件信息
            ContractAttachment attachment = attachmentMapper.selectById(extract.getAttachmentId());
            if (attachment == null) {
                throw new RuntimeException("附件不存在");
            }

            // 读取PDF文件
            String pdfContent = pdfParserService.parsePdfText(attachment.getFilePath());

            // 调用Dify API进行数据提取
            ContractAiExtract difyExtract = difyService.extractContractData(
                extract.getContractId(), extract.getAttachmentId(), pdfContent);
            Map<String, Object> difyResult = new HashMap<>();
            difyResult.put("result", difyExtract.getExtractData());

            // 保存提取结果
            extract.setExtractData(objectMapper.writeValueAsString(difyResult));
            extract.setExtractStatus("COMPLETED");
            extract.setExtractTime(LocalDateTime.now());
            extract.setUpdateTime(LocalDateTime.now());
            aiExtractMapper.updateById(extract);

            log.info("AI提取完成，提取ID: {}", extractId);

        } catch (Exception e) {
            log.error("AI提取失败，提取ID: {}", extractId, e);
            extract.setExtractStatus("FAILED");
            extract.setErrorMessage(e.getMessage());
            extract.setUpdateTime(LocalDateTime.now());
            aiExtractMapper.updateById(extract);
        }
    }

    /**
     * 获取提取结果
     */
    public Map<String, Object> getExtractResult(Long extractId) {
        ContractAiExtract extract = aiExtractMapper.selectById(extractId);
        if (extract == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", extract.getId());
        result.put("contractId", extract.getContractId());
        result.put("attachmentId", extract.getAttachmentId());
        result.put("extractStatus", extract.getExtractStatus());
        result.put("createTime", extract.getCreateTime());
        result.put("extractTime", extract.getExtractTime());
        result.put("errorMessage", extract.getErrorMessage());

        if (extract.getExtractData() != null) {
            try {
                Map<String, Object> extractData = objectMapper.readValue(
                    extract.getExtractData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                result.put("extractData", extractData);
            } catch (Exception e) {
                log.error("解析提取数据失败", e);
                result.put("extractData", extract.getExtractData());
            }
        }

        return result;
    }

    /**
     * 根据合同ID获取提取结果
     */
    public ContractAiExtract getByContractId(Long contractId) {
        LambdaQueryWrapper<ContractAiExtract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractAiExtract::getContractId, contractId)
               .orderByDesc(ContractAiExtract::getCreateTime)
               .last("LIMIT 1");
        return aiExtractMapper.selectOne(wrapper);
    }

    /**
     * 重新执行提取
     */
    public void reExtract(Long extractId) {
        ContractAiExtract extract = aiExtractMapper.selectById(extractId);
        if (extract == null) {
            throw new RuntimeException("提取记录不存在");
        }

        // 重置状态
        extract.setExtractStatus("PENDING");
        extract.setExtractData(null);
        extract.setErrorMessage(null);
        extract.setExtractTime(null);
        extract.setUpdateTime(LocalDateTime.now());
        aiExtractMapper.updateById(extract);

        // 异步执行
        asyncExtractContractData(extractId);
    }

    /**
     * 重试提取（通过合同ID）
     */
    public ContractAiExtract retryExtract(Long contractId) {
        ContractAiExtract existing = getByContractId(contractId);
        if (existing != null) {
            reExtract(existing.getId());
            return existing;
        } else {
            throw new RuntimeException("未找到合同的AI提取记录");
        }
    }
}
