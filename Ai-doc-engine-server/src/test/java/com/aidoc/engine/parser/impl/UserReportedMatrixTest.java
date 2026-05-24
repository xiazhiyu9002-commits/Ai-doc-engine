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
public class UserReportedMatrixTest {

    @Autowired
    private DocumentParser documentParser;

    @Test
    public void testUserReportedFormulas() {
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
                "\\]\n\n" +
                "### 3. 循环矩阵行列式\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "a_0 & a_1 & a_2 & \\dots & a_{n-1}\\\\\n" +
                "a_{n-1} & a_0 & a_1 & \\dots & a_{n-2}\\\\\n" +
                "\\vdots & \\vdots & \\vdots & & \\vdots\\\\\n" +
                "a_1 & a_2 & a_3 & \\dots & a_0\n" +
                "\\end{vmatrix}\n" +
                "=\\prod_{k=0}^{n-1}\\sum_{j=0}^{n-1}a_j\\omega^{jk}\n" +
                "\\]\n\n" +
                "\\(\\omega=e^{2\\pi i/n}\\) 为n次单位根。\n\n" +
                "---\n\n" +
                "### 2. 柯西行列式\n\n" +
                "\\[\n" +
                "\\begin{vmatrix}\n" +
                "\\dfrac{1}{x_1+y_1}&\\cdots&\\dfrac{1}{x_1+y_n}\\\\\n" +
                "\\vdots&&\\vdots\\\\\n" +
                "\\dfrac{1}{x_n+y_1}&\\cdots&\\dfrac{1}{x_n+y_n}\n" +
                "\\end{vmatrix}\n" +
                "=\\frac{\\prod_{i<j}(x_i-x_j)(y_i-y_j)}{\\prod_{i,j}(x_i+y_j)}\n" +
                "\\]\n\n" +
                "### 3. 雅可比行列式（多元微积分）\n\n" +
                "\\[\n" +
                "J=\\frac{\\partial(F_1,\\dots,F_n)}{\\partial(x_1,\\dots,x_n)}=\\begin{vmatrix}\n" +
                "\\frac{\\partial F_1}{\\partial x_1}&\\dots&\\frac{\\partial F_1}{\\partial x_n}\\\\\n" +
                "\\vdots&&\\vdots\\\\\n" +
                "\\frac{\\partial F_n}{\\partial x_1}&\\dots&\\frac{\\partial F_n}{\\partial x_n}\n" +
                "\\end{vmatrix}\n" +
                "\\]";

        log.info("测试用户报告的所有行列式公式");
        UdmDocument doc = documentParser.parse(markdown);
        
        assertNotNull(doc);
        
        long formulaCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .count();
        
        log.info("识别到 {} 个公式块", formulaCount);
        
        // 打印所有块的类型
        int formulaIndex = 0;
        for (int i = 0; i < doc.getBlocks().size(); i++) {
            UdmBlock block = doc.getBlocks().get(i);
            log.info("Block {}: type={}", i, block.getType());
            if (block instanceof FormulaBlock) {
                FormulaBlock fb = (FormulaBlock) block;
                formulaIndex++;
                log.info("  公式 #{} 前80字符: {}", formulaIndex, 
                    fb.getContent().getLatex().substring(0, Math.min(80, fb.getContent().getLatex().length())));
            }
        }
        
        // 应该识别到5个块级公式
        assertEquals(5, formulaCount, "应该识别到5个块级公式");
        
        // 验证每个公式都包含 vmatrix 环境
        long vmatrixCount = doc.getBlocks().stream()
                .filter(b -> b instanceof FormulaBlock)
                .map(b -> (FormulaBlock) b)
                .filter(fb -> fb.getContent().getLatex().contains("\\begin{vmatrix}"))
                .count();
        
        assertEquals(5, vmatrixCount, "所有5个公式都应该包含 vmatrix 环境");
    }
}
