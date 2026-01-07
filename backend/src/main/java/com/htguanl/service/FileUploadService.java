package com.htguanl.service;

import com.htguanl.entity.ContractAttachment;
import com.htguanl.mapper.ContractAttachmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务类
 * 处理PDF文件上传和存储
 */
@Slf4j
@Service
public class FileUploadService {

    String currentDir = System.getProperty("user.dir");
    //@Value("${file.upload.path:./uploads}")
    private String uploadPath = currentDir+"/uploads";

    @Value("${file.upload.maxFileSize:10485760}")
    private long maxFileSize;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("application/pdf");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf");

    private final ContractAttachmentMapper contractAttachmentMapper;

    public FileUploadService(ContractAttachmentMapper contractAttachmentMapper) {
        this.contractAttachmentMapper = contractAttachmentMapper;
    }

    /**
     * 上传PDF文件
     *
     * @param file 上传的文件
     * @param contractId 合同ID
     * @return 附件实体
     * @throws IOException IO异常
     * @throws IllegalArgumentException 参数异常
     */
    public ContractAttachment uploadPdfFile(MultipartFile file, Long contractId) throws IOException, IllegalArgumentException {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = "contracts/" + datePath;
        String absolutePath = uploadPath + File.separator + relativePath;

        Path uploadDir = Paths.get(absolutePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + extension;
        String filePath = absolutePath + File.separator + newFilename;
        String relativeFilePath = relativePath + "/" + newFilename;

        // 保存文件
        file.transferTo(new File(filePath));

        // 创建附件记录
        ContractAttachment attachment = new ContractAttachment();
        attachment.setContractId(contractId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(relativeFilePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setUploadStatus("SUCCESS");
        attachment.setAiExtractStatus("PENDING");
        attachment.setCreateBy(getCurrentUserId());
        attachment.setCreateTime(LocalDateTime.now());
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setUploadBy(getCurrentUserId());

        contractAttachmentMapper.insert(attachment);

        log.info("文件上传成功: fileName={}, filePath={}, fileSize={}", 
                originalFilename, relativeFilePath, file.getSize());

        return attachment;
    }

    /**
     * 验证文件
     *
     * @param file 上传的文件
     * @throws IllegalArgumentException 参数异常
     */
    private void validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许" + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持PDF文件");
        }

        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件扩展名，仅支持.pdf文件");
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前登录用户ID
        return 1L;
    }

    /**
     * 删除文件
     *
     * @param attachmentId 附件ID
     * @return 是否删除成功
     */
    public boolean deleteFile(Long attachmentId) {
        ContractAttachment attachment = contractAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            return false;
        }

        try {
            // 删除物理文件
            String absolutePath = uploadPath + File.separator + attachment.getFilePath();
            Path filePath = Paths.get(absolutePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // 删除数据库记录
            contractAttachmentMapper.deleteById(attachmentId);

            log.info("文件删除成功: attachmentId={}, fileName={}", attachmentId, attachment.getFileName());
            return true;
        } catch (IOException e) {
            log.error("文件删除失败: attachmentId={}, error={}", attachmentId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取文件绝对路径
     *
     * @param relativeFilePath 相对路径
     * @return 绝对路径
     */
    public String getAbsolutePath(String relativeFilePath) {
        return uploadPath + File.separator + relativeFilePath;
    }

    /**
     * 上传文件并返回文件路径
     *
     * @param file 上传的文件
     * @return 文件路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = "contracts/" + datePath;
        String absolutePath = uploadPath + File.separator + relativePath;

        Path uploadDir = Paths.get(absolutePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + extension;
        String filePath = absolutePath + File.separator + newFilename;

        // 保存文件
        file.transferTo(new File(filePath));

        log.info("文件上传成功: fileName={}, filePath={}, fileSize={}",
                originalFilename, filePath, file.getSize());

        return filePath;
    }
}
