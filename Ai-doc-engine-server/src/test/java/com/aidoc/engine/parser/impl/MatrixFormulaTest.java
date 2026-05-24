package com.aidoc.engine.parser.impl;

import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.FormulaBlock;
import com.aidoc.engine.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class MatrixFormulaTest {

    @Autowired
    private DocumentParser documentParser;

    @Test
    public void testMatrixFormulaWithBackslashBrackets() {
        String markdown = "### 1. 上三角分块\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "A & B \\\\\n" +
                "O & C\n" +
                "\\end{vmatrix}\n" +
                "=|A||C|\n" +
                "\\]";

        log.info("测试 Markdown:\n{}", markdown);

        UdmDocument doc = documentParser.parse(markdown);
        assertNotNull(doc);
        assertFalse(doc.getBlocks().isEmpty());

        log.info("解析后的 Block 数量: {}", doc.getBlocks().size());
        for (int i = 0; i < doc.getBlocks().size(); i++) {
            UdmBlock block = doc.getBlocks().get(i);
            log.info("Block {}: type={}", i, block.getType());
            if (block instanceof FormulaBlock) {
                FormulaBlock formulaBlock = (FormulaBlock) block;
                log.info("  公式内容: {}", formulaBlock.getContent().getLatex());
            }
        }

        // 应该有至少一个公式块
        long formulaCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .count();
        
        assertTrue(formulaCount > 0, "应该识别到至少一个公式块");

        // 验证公式内容
        FormulaBlock formulaBlock = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .map(b -> (FormulaBlock) b)
                .findFirst()
                .orElse(null);

        assertNotNull(formulaBlock, "应该找到公式块");
        String latex = formulaBlock.getContent().getLatex();
        assertTrue(latex.contains("\\begin{vmatrix}"), "公式应包含 vmatrix 环境");
        assertTrue(latex.contains("A & B"), "公式应包含矩阵元素");
    }

    @Test
    public void testMultipleMatrixFormulas() {
        String markdown = "### 1. 上三角分块\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "A & B \\\\\n" +
                "O & C\n" +
                "\\end{vmatrix}\n" +
                "=|A||C|\n" +
                "\\]\n\n" +
                "### 2. 下三角分块\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "A & O \\\\\n" +
                "D & C\n" +
                "\\end{vmatrix}\n" +
                "=|A||C|\n" +
                "\\]";

        log.info("测试多个矩阵公式");

        UdmDocument doc = documentParser.parse(markdown);
        assertNotNull(doc);

        long formulaCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .count();

        log.info("识别到 {} 个公式块", formulaCount);
        assertEquals(2, formulaCount, "应该识别到2个公式块");
    }
}
