package com.htguanl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htguanl.common.PageRequest;
import com.htguanl.common.Result;
import com.htguanl.entity.ContractAttachment;
import com.htguanl.entity.ContractAiExtract;
import com.htguanl.service.ContractAiExtractService;
import com.htguanl.service.ContractAttachmentService;
import com.htguanl.service.FileUploadService;
import com.htguanl.service.PdfParserService;
import com.htguanl.service.DifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * 合同附件控制器
 * 提供PDF文件上传、解析和AI提取功能
 */
@Slf4j
@RestController
@RequestMapping("/api/contract-attachment")
public class ContractAttachmentController {

    @Autowired
    private ContractAttachmentService contractAttachmentService;

    @Autowired
    private ContractAiExtractService contractAiExtractService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private DifyService difyService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传PDF文件
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "contractId", required = false) Long contractId) {
        try {
            // 验证文件类型
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return Result.error("仅支持PDF格式文件");
            }

            // // 验证文件大小（默认50MB）
            // long maxSize = 50 * 1024 * 1024 * 1024;
            // if (file.getSize() > maxSize) {
            //     return Result.error("文件大小不能超过50MB");
            // }

            // 上传文件
            String filePath = fileUploadService.uploadFile(file);
            if (filePath == null) {
                return Result.error("文件上传失败");
            }

            // 创建附件记录
            ContractAttachment attachment = new ContractAttachment();
            attachment.setContractId(contractId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType("PDF");
            attachment.setUploadStatus("SUCCESS");
            attachment.setAiExtractStatus("PENDING");

            contractAttachmentService.save(attachment);

            // 异步处理PDF解析和AI提取
            asyncProcessPdf(attachment);

            Map<String, Object> data = new HashMap<>();
            data.put("attachmentId", attachment.getId());
            data.put("fileName", attachment.getFileName());
            data.put("filePath", filePath);
            data.put("fileSize", attachment.getFileSize());

            return Result.success(data);
        } catch (Exception e) {
            log.error("PDF上传失败", e);
            return Result.error("PDF上传失败: " + e.getMessage());
        }
    }

    /**
     * 异步处理PDF解析和AI提取
     */
    private void asyncProcessPdf(ContractAttachment attachment) {
        new Thread(() -> {
            try {
                // 更新状态为处理中
                attachment.setAiExtractStatus("PROCESSING");
                contractAttachmentService.updateById(attachment);

                // 解析PDF内容
                String pdfContent = pdfParserService.parsePdf(attachment.getFilePath());
                if (pdfContent == null || pdfContent.isEmpty()) {
                    attachment.setAiExtractStatus("FAILED");
                    attachment.setErrorMessage("PDF解析失败");
                    contractAttachmentService.updateById(attachment);
                    return;
                }

                // 调用Dify工作流进行AI提取
                ContractAiExtract extractResult = difyService.extractContractData(
                    attachment.getContractId(), attachment.getId(), pdfContent);

                // 更新附件状态
                if (extractResult != null && "SUCCESS".equals(extractResult.getExtractStatus())) {
                    attachment.setAiExtractStatus("SUCCESS");
                } else {
                    attachment.setAiExtractStatus("FAILED");
                    attachment.setErrorMessage(extractResult != null ?
                            extractResult.getErrorMessage() : "AI提取失败");
                }

                contractAttachmentService.updateById(attachment);

            } catch (Exception e) {
                log.error("PDF处理失败", e);
                attachment.setAiExtractStatus("FAILED");
                attachment.setErrorMessage("处理失败: " + e.getMessage());
                contractAttachmentService.updateById(attachment);
            }
        }).start();
    }

    /**
     * 获取附件详情
     */
    @GetMapping("/{id}")
    public Result<ContractAttachment> getDetail(@PathVariable Long id) {
        try {
            ContractAttachment attachment = contractAttachmentService.getById(id);
            if (attachment == null) {
                return Result.error("附件不存在");
            }
            return Result.success(attachment);
        } catch (Exception e) {
            log.error("获取附件详情失败", e);
            return Result.error("获取附件详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取附件列表
     */
    @PostMapping("/list")
    public Result<Page<ContractAttachment>> list(@RequestBody PageRequest pageRequest) {
        try {
            Page<ContractAttachment> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
            LambdaQueryWrapper<ContractAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ContractAttachment::getCreateTime);
            Page<ContractAttachment> result = contractAttachmentService.page(page, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取附件列表失败", e);
            return Result.error("获取附件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有附件列表
     */
    @GetMapping("/all")
    public Result<java.util.List<ContractAttachment>> getAllAttachments() {
        try {
            LambdaQueryWrapper<ContractAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ContractAttachment::getCreateTime);
            java.util.List<ContractAttachment> list = contractAttachmentService.list(wrapper);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取附件列表失败", e);
            return Result.error("获取附件列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据合同ID获取附件列表
     */
    @GetMapping("/contract/{contractId}")
    public Result<java.util.List<ContractAttachment>> getByContractId(@PathVariable Long contractId) {
        try {
            LambdaQueryWrapper<ContractAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContractAttachment::getContractId, contractId);
            wrapper.orderByDesc(ContractAttachment::getCreateTime);
            java.util.List<ContractAttachment> list = contractAttachmentService.list(wrapper);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取合同附件失败", e);
            return Result.error("获取合同附件失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI提取结果
     */
    @GetMapping("/extract/{attachmentId}")
    public Result<ContractAiExtract> getExtractResult(@PathVariable Long attachmentId) {
        try {
            LambdaQueryWrapper<ContractAiExtract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContractAiExtract::getAttachmentId, attachmentId);
            wrapper.orderByDesc(ContractAiExtract::getCreateTime);
            wrapper.last("LIMIT 1");
            ContractAiExtract result = contractAiExtractService.getOne(wrapper);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取AI提取结果失败", e);
            return Result.error("获取AI提取结果失败: " + e.getMessage());
        }
    }

    /**
     * 下载附件
     */
    @GetMapping("/{id}/download")
    public void download(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) {
        try {
            ContractAttachment attachment = contractAttachmentService.getById(id);
            if (attachment == null) {
                response.setStatus(404);
                return;
            }

            File file = new File(attachment.getFilePath());
            if (!file.exists()) {
                response.setStatus(404);
                return;
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + java.net.URLEncoder.encode(attachment.getFileName(), "UTF-8") + "\"");
            response.setContentLengthLong(file.length());

            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 java.io.OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("下载附件失败", e);
            response.setStatus(500);
        }
    }

    /**
     * 删除附件
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            ContractAttachment attachment = contractAttachmentService.getById(id);
            if (attachment == null) {
                return Result.error("附件不存在");
            }

            // 删除物理文件
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                file.delete();
            }

            // 删除数据库记录
            contractAttachmentService.removeById(id);

            // 删除关联的AI提取记录
            LambdaQueryWrapper<ContractAiExtract> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContractAiExtract::getAttachmentId, id);
            contractAiExtractService.remove(wrapper);

            return Result.success();
        } catch (Exception e) {
            log.error("删除附件失败", e);
            return Result.error("删除附件失败: " + e.getMessage());
        }
    }

    /**
     * 重新触发AI提取
     */
    @PostMapping("/re-extract/{id}")
    public Result<Void> reExtract(@PathVariable Long id) {
        try {
            ContractAttachment attachment = contractAttachmentService.getById(id);
            if (attachment == null) {
                return Result.error("附件不存在");
            }

            // 异步重新处理
            asyncProcessPdf(attachment);

            return Result.success();
        } catch (Exception e) {
            log.error("重新提取失败", e);
            return Result.error("重新提取失败: " + e.getMessage());
        }
    }
}
