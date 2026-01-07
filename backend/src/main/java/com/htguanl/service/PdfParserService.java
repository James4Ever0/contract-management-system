package com.htguanl.service;

import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
//import java.io.FileInputStream;
import java.io.IOException;

/**
 * PDF解析服务
 * 使用PDFBox解析PDF文件内容
 */
@Service
public class PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserService.class);

    /**
     * 解析PDF文件内容
     * 
     * @param filePath PDF文件路径
     * @return PDF文本内容
     * @throws IOException 解析失败时抛出异常
     */
    public String parsePdf(String filePath) throws IOException {
        log.info("开始解析PDF文件: {}", filePath);

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("PDF文件不存在: " + filePath);
        }

        // 验证文件扩展名
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            throw new IOException("文件格式错误，仅支持PDF格式");
        }

        // try (PDDocument document = PDDocument.load(file)) {
        //     PDFTextStripper textStripper = new PDFTextStripper();
        //     // 设置文本提取范围，从第一页开始到最后一页
        //     textStripper.setStartPage(1);
        //     textStripper.setEndPage(document.getNumberOfPages());

        //     String content = textStripper.getText(document);
        //     log.info("PDF解析完成，共提取 {} 页内容", document.getNumberOfPages());

        //     return content;
        // }
        //load the PDF file bytes and convert to base64 string
            String base64String = filePath;
return base64String;
        // try {
            // byte[] fileBytes = new byte[(int) file.length()];

            // try (FileInputStream fis = new FileInputStream(file)) {
            // fis.read(fileBytes);
            // }catch (IOException e) {
            //     log.error("PDF解析失败 (read bytes): {}", e.getMessage(), e);
            //     throw new IOException("PDF解析失败 (read bytes): " + e.getMessage());
            // }
            // String base64String = new String(fileBytes, "UTF-8");
            // return base64String;
        // }
        // catch (IOException e) {
        //     log.error("PDF解析失败: {}", e.getMessage(), e);
        //     throw new IOException("PDF解析失败: " + e.getMessage(), e);
        // }
    }

    /**
     * 获取PDF文件页数
     * 
     * @param filePath PDF文件路径
     * @return 页数
     * @throws IOException 解析失败时抛出异常
     */
    public int getNumberOfPages(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("PDF文件不存在: " + filePath);
        }

        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            log.error("获取PDF页数失败: {}", e.getMessage(), e);
            throw new IOException("获取PDF页数失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析PDF文件文本内容（别名方法）
     *
     * @param filePath PDF文件路径
     * @return PDF文本内容
     * @throws IOException 解析失败时抛出异常
     */
    public String parsePdfText(String filePath) throws IOException {
        return parsePdf(filePath);
    }

    /**
     * 验证PDF文件是否有效
     *
     * @param filePath PDF文件路径
     * @return 是否有效
     */
    public boolean isValidPdf(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages() > 0;
        } catch (Exception e) {
            log.warn("PDF文件验证失败: {}", e.getMessage());
            return false;
        }
    }
}
