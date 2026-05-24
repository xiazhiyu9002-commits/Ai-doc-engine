package com.aidoc.engine.service.impl;

import com.aidoc.engine.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 测试复杂文档（包含公式、表格、流程图）的处理
 */
@Slf4j
@SpringBootTest
public class ComplexDocumentTest {

    @Autowired
    private DocumentService documentService;

    @Test
    public void testMachineLearningDocument() throws IOException {
        log.info("========== 测试机器学习文档（包含多个公式）==========");
        
        // 读取测试文档
        String markdown = Files.readString(Paths.get("test_ml_document.md"));
        log.info("文档长度: {} 字符", markdown.length());
        
        // 导出为Word
        byte[] wordBytes = documentService.parseAndExportToWord(markdown);
        log.info("生成Word文档，大小: {} 字节", wordBytes.length);
        
        // 保存文件
        File outputFile = new File("test_ml_document_output.docx");
        Files.write(outputFile.toPath(), wordBytes);
        log.info("测试文档已保存到: {}", outputFile.getAbsolutePath());
        log.info("请用Word打开此文档，检查：");
        log.info("1. 块级公式是否正确显示");
        log.info("2. 行内公式是否正确显示");
        log.info("3. 公式是否可以双击编辑");
        log.info("4. 表格是否正确渲染");
        log.info("5. 流程图占位符是否存在");
    }
}
