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
public class ComplexMatrixTest {

    @Autowired
    private DocumentParser documentParser;

    @Test
    public void testVandermondeMatrix() {
        String markdown = "### 1. 范德蒙德行列式\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "1 & x_1 & x_1^2 & \\dots & x_1^{n-1}\\\\\n" +
                "1 & x_2 & x_2^2 & \\dots & x_2^{n-1}\\\\\n" +
                "\\vdots & \\vdots & \\vdots & & \\vdots\\\\\n" +
                "1 & x_n & x_n^2 & \\dots & x_n^{n-1}\n" +
                "\\end{vmatrix}\n" +
                "=\\prod_{1\\le j<i\\le n}(x_i-x_j)\n" +
                "\\]";

        log.info("测试范德蒙德行列式");
        UdmDocument doc = documentParser.parse(markdown);
        
        assertNotNull(doc);
        
        long formulaCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .count();
        
        log.info("识别到 {} 个公式块", formulaCount);
        
        // 打印所有块的类型
        for (int i = 0; i < doc.getBlocks().size(); i++) {
            UdmBlock block = doc.getBlocks().get(i);
            log.info("Block {}: type={}", i, block.getType());
            if (block instanceof FormulaBlock) {
                FormulaBlock fb = (FormulaBlock) block;
                log.info("  公式内容: {}", fb.getContent().getLatex());
            }
        }
        
        assertTrue(formulaCount > 0, "应该识别到至少一个公式块");
    }

    @Test
    public void testMultipleFormulasInDocument() {
        String markdown = "## 三、特殊结构行列式（复杂经典型）\n\n" +
                "### 1. 范德蒙德行列式\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "1 & x_1 & x_1^2 & \\dots & x_1^{n-1}\\\\\n" +
                "1 & x_2 & x_2^2 & \\dots & x_2^{n-1}\\\\\n" +
                "\\vdots & \\vdots & \\vdots & & \\vdots\\\\\n" +
                "1 & x_n & x_n^2 & \\dots & x_n^{n-1}\n" +
                "\\end{vmatrix}\n" +
                "=\\prod_{1\\le j<i\\le n}(x_i-x_j)\n" +
                "\\]\n\n" +
                "### 2. 三对角行列式（递推复杂）\n\n" +
                "\\[\n" +
                "D_n=\\begin{vmatrix}\n" +
                "a & b & & \\\\\n" +
                "c & a & b & \\\\\n" +
                "& c & \\ddots & \\ddots\\\\\n" +
                "& & \\ddots & a\n" +
                "\\end{vmatrix}\n" +
                "\\]";

        log.info("测试多个复杂行列式");
        UdmDocument doc = documentParser.parse(markdown);
        
        assertNotNull(doc);
        
        long formulaCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .count();
        
        log.info("识别到 {} 个公式块", formulaCount);
        
        // 打印所有块的类型
        for (int i = 0; i < doc.getBlocks().size(); i++) {
            UdmBlock block = doc.getBlocks().get(i);
            log.info("Block {}: type={}", i, block.getType());
            if (block instanceof FormulaBlock) {
                FormulaBlock fb = (FormulaBlock) block;
                log.info("  公式内容前50字符: {}", 
                    fb.getContent().getLatex().substring(0, Math.min(50, fb.getContent().getLatex().length())));
            }
        }
        
        assertEquals(2, formulaCount, "应该识别到2个公式块");
    }

    @Test
    public void testCauchyDeterminant() {
        String markdown = "### 2. 柯西行列式\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "\\dfrac{1}{x_1+y_1}&\\cdots&\\dfrac{1}{x_1+y_n}\\\\\n" +
                "\\vdots&&\\vdots\\\\\n" +
                "\\dfrac{1}{x_n+y_1}&\\cdots&\\dfrac{1}{x_n+y_n}\n" +
                "\\end{vmatrix}\n" +
                "=\\frac{\\prod_{i<j}(x_i-x_j)(y_i-y_j)}{\\prod_{i,j}(x_i+y_j)}\n" +
                "\\]";

        log.info("测试柯西行列式");
        UdmDocument doc = documentParser.parse(markdown);
        
        assertNotNull(doc);
        
        long formulaCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .count();
        
        log.info("识别到 {} 个公式块", formulaCount);
        assertTrue(formulaCount > 0, "应该识别到至少一个公式块");
    }
}
