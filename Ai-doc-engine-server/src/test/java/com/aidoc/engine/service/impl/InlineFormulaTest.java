package com.aidoc.engine.service.impl;

import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.content.ParagraphContent;
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
 * 测试行内公式的处理
 */
@Slf4j
@SpringBootTest
public class InlineFormulaTest {

    @Autowired
    private DocumentService documentService;

    @Test
    public void testInlineFormula() throws IOException {
        log.info("========== 测试行内公式 ==========");
        
        // 读取测试文档
        String markdown = Files.readString(Paths.get("test_inline_formula.md"));
        log.info("文档长度: {} 字符", markdown.length());
        
        // 解析为 UDM
        UdmDocument udm = documentService.parseMarkdown(markdown);
        log.info("解析完成，Block 数量: {}", udm.getBlocks().size());
        
        // 检查行内公式
        udm.getBlocks().stream()
            .filter(block -> block instanceof com.aidoc.engine.model.udm.block.ParagraphBlock)
            .map(block -> (com.aidoc.engine.model.udm.block.ParagraphBlock) block)
            .forEach(block -> {
                ParagraphContent content = block.getContent();
                if (content != null && content.getSegments() != null) {
                    long formulaCount = content.getSegments().stream()
                        .filter(seg -> seg.getInlineFormula() != null && !seg.getInlineFormula().isEmpty())
                        .count();
                    if (formulaCount > 0) {
                        log.info("段落包含 {} 个行内公式", formulaCount);
                        content.getSegments().stream()
                            .filter(seg -> seg.getInlineFormula() != null)
                            .forEach(seg -> log.info("  - 行内公式: {}", seg.getInlineFormula()));
                    }
                }
            });
        
        // 导出为Word
        byte[] wordBytes = documentService.exportToWord(udm);
        log.info("生成Word文档，大小: {} 字节", wordBytes.length);
        
        // 保存文件
        File outputFile = new File("test_inline_formula_output.docx");
        Files.write(outputFile.toPath(), wordBytes);
        log.info("测试文档已保存到: {}", outputFile.getAbsolutePath());
        log.info("请用Word打开此文档，检查行内公式是否可以编辑");
    }
}
