package com.htguanl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htguanl.config.DifyConfig;
import com.htguanl.entity.ContractAiExtract;
import com.htguanl.mapper.ContractAiExtractMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Dify工作流API服务类
 * 封装Dify工作流API调用，实现合同数据的智能提取
 */
@Slf4j
@Service
public class DifyService {

    @Autowired
    private DifyConfig difyConfig;

    @Autowired
    private ContractAiExtractMapper contractAiExtractMapper;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DifyService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(6000, TimeUnit.SECONDS)
                .writeTimeout(3000, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }


    public String uploadFile(String pdfContent){
        // pdfContent is the base64 encoded string of the PDF file
        /*
        curl -X POST 'http://192.168.1.15/v1/files/upload' \
--header 'Authorization: Bearer {api_key}' \
--form 'file=@localfile;type=image/[png|jpeg|jpg|webp|gif]' \
--form 'user=abc-123'
 */
        String url = difyConfig.getApiUrl()+"/files/upload";
        String apikey = difyConfig.getApiKey();
        String userId = "hetongguanli";
        java.io.File file = new java.io.File(pdfContent);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user", userId)
                .addFormDataPart("file", "test.pdf",
                        RequestBody.create(file, MediaType.parse("application/pdf")))
                .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apikey)
                    .post(requestBody).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Dify file upload failed: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                log.debug("Dify file upload 响应: {}", responseBody);

                // 解析响应
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                 if (jsonNode.has("id") ){
                    String upload_file_id = jsonNode.get("id").asText();
                    return upload_file_id;
                 }
                 else { throw new IOException("Dify file upload failed: " + response.code() + " " + response.message());}
            } catch (IOException e) {
                log.error("Dify file upload failed", e);
                throw new RuntimeException("Dify file upload failed", e);
            }

    }

    /**
     * 调用Dify工作流API进行合同数据提取
     *
     * @param contractId 合同ID
     * @param attachmentId 附件ID
     * @param pdfContent PDF文本内容
     * @return 提取结果
     */
    public ContractAiExtract extractContractData(Long contractId, Long attachmentId, String pdfContent) {
        log.info("开始调用Dify工作流API，合同ID: {}, 附件ID: {}", contractId, attachmentId);

        // assume pdfContent is base64 encoded file bytes
        // 创建提取记录
        ContractAiExtract extract = new ContractAiExtract();
        extract.setContractId(contractId);
        extract.setAttachmentId(attachmentId);
        extract.setExtractStatus("PROCESSING");
        extract.setPdfContent(pdfContent);
        extract.setExtractData(null);
        extract.setErrorMessage(null);
        contractAiExtractMapper.insert(extract);

        try {
            // 检查Dify是否启用
            if (!difyConfig.getEnabled()) {
                log.warn("Dify服务未启用，跳过AI提取");
                extract.setExtractStatus("DISABLED");
                extract.setErrorMessage("Dify服务未启用");
                contractAiExtractMapper.updateById(extract);
                return extract;
            }

            String upload_file_id = uploadFile(pdfContent);

            // 构建测试请求
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> pdf_body = new HashMap<>();
            pdf_body.put("transfer_method", "local_file");
            pdf_body.put("upload_file_id", upload_file_id);
            pdf_body.put("type", "document");

            requestBody.put("inputs", Map.of("pdf", pdf_body));
            requestBody.put("response_mode", "blocking");
            // requestBody.put("user", "system");
            requestBody.put("user", "app_contract_info_extractor");

            // error: timeout


            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 构建请求
            Request request = new Request.Builder()
                    .url(difyConfig.getApiUrl()+"/workflows/run")
                    .addHeader("Authorization", "Bearer " + difyConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            // emulate request body and store to db
            // TODO: set to false
            if (false) {
                extract.setExtractStatus("SUCCESS");
                extract.setExtractData("{\"partyA\": \"testPartyA\", \"partyB\": \"testPartyB\"}");
                extract.setErrorMessage(null);

                extract.setContractNo("testno");
                extract.setContractName("testname");
                extract.setContractType(
                "testtype"
                );
                extract.setCustomerName("testcustomer");
                extract.setContractAmount(BigDecimal.valueOf(100));
                extract.setSignDate(LocalDateTime.now()); // localdatetime
                extract.setStartDate(LocalDateTime.now());
                extract.setEndDate(LocalDateTime.now());
                extract.setPaymentTerms("testpaymentterms");
                extract.setDeliveryTerms("testdeliveryterms");
                extract.setQualityTerms("testqualityterms");
                extract.setBreachClause("testbreachclause");
                extract.setOtherTerms("testothertems");
                extract.setRawData("testrawdata");
                extract.setExtractStartTime(LocalDateTime.now());
                extract.setExtractEndTime(LocalDateTime
                        .now()
                );
                // extract.setRetryCount(0);
                extract.setExtractTime(LocalDateTime.now());
                contractAiExtractMapper.updateById(extract);
                return extract;
            }

            // 发送请求
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Dify API调用失败: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                log.debug("Dify API响应: {}", responseBody);

                // 解析响应 (parse data from dify api response)
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                if (jsonNode.has("data") && jsonNode.get("data").has("outputs")) {
                    JsonNode outputs = jsonNode.get("data").get("outputs");
                    log.info("Dify API outputs: {}", outputs);

                    // 将提取结果转换为JSON字符串
                    String extractData = objectMapper.writeValueAsString(outputs);

                    // 更新提取结果
                    // TODO: get field "parsed_result" from outputs once mutual field names and prompts are ready
                    extract.setExtractStatus("SUCCESS");
                    // TODO: set this field to jsonb type in java adaptor or string in sql schema, examine "getAiExtractResult" api in js, java api endpoint "/contract-attachment/extract/"
                    extract.setExtractData(extractData);
                    extract.setErrorMessage(null);
                    extract.setExtractTime(LocalDateTime.now());

                    log.info("Dify工作流API调用成功，合同ID: {}", contractId);
                } else {
                    throw new IOException("Dify API响应格式错误: 缺少outputs字段");
                }
            }

        } catch (Exception e) {
            log.error("Dify工作流API调用失败，合同ID: {}, 错误: {}", contractId, e.getMessage(), e);
            extract.setExtractStatus("FAILED");
            extract.setErrorMessage(e.getMessage());
        }

        contractAiExtractMapper.updateById(extract);
        return extract;
    }

    /**
     * 测试Dify连接（指定参数）
     *
     * @param apiUrl API地址
     * @param apiKey API密钥
     * @return 测试结果
     */
    public Map<String, Object> testConnection(String apiUrl, String apiKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "");

        try {
            Request request = new Request.Builder()
                    .url(apiUrl+"/info")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    result.put("success", true);
                    result.put("message", "Dify连接测试成功");
                } else {
                    result.put("message", "Dify连接测试失败: " + response.code() + " " + response.message());
                }
            }

        } catch (Exception e) {
            log.error("Dify连接测试失败: {}", e.getMessage(), e);
            result.put("message", "Dify连接测试失败: " + e.getMessage());
        }

        return result;
    }



    /**
     * 测试Dify连接（使用当前配置）
     *
     * @return 测试结果
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "");

        try {
            if (!difyConfig.getEnabled()) {
                result.put("message", "Dify服务未启用");
                return result;
            }

            Request request = new Request.Builder()
                    .url(difyConfig.getApiUrl()+"/info")
                    .addHeader("Authorization", "Bearer " + difyConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    result.put("success", true);
                    result.put("message", "Dify连接测试成功");
                } else {
                    result.put("message", "Dify连接测试失败: " + response.code() + " " + response.message());
                }
            }

        } catch (Exception e) {
            log.error("Dify连接测试失败: {}", e.getMessage(), e);
            result.put("message", "Dify连接测试失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取合同AI提取结果
     *
     * @param contractId 合同ID
     * @return 提取结果
     */
    public ContractAiExtract getExtractResult(Long contractId) {
        return contractAiExtractMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ContractAiExtract>()
                        .eq(ContractAiExtract::getContractId, contractId)
                        .orderByDesc(ContractAiExtract::getCreateTime)
                        .last("LIMIT 1")
        );
    }

    /**
     * 重试失败的提取任务
     *
     * @param extractId 提取记录ID
     * @return 重试结果
     */
    public ContractAiExtract retryExtract(Long extractId) {
        ContractAiExtract extract = contractAiExtractMapper.selectById(extractId);
        if (extract == null) {
            throw new RuntimeException("提取记录不存在");
        }

        // 读取PDF内容（这里需要从附件服务获取）
        // 暂时使用空内容，实际需要从文件系统读取
        // String pdfContent = "";
        String pdfContent = extract.getPdfContent();

        return extractContractData(extract.getContractId(), extract.getAttachmentId(), pdfContent);
    }
}
