package com.aidoc.engine.service.impl;

import com.aidoc.engine.service.FormulaConvertService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class VandermondeMatrixTest {

    @Autowired
    private FormulaConvertService formulaConvertService;

    @Test
    public void testVandermondeMatrix() {
        String latex = "\\begin{vmatrix}\n" +
                "1 & x_1 & x_1^2 & \\dots & x_1^{n-1}\\\\\n" +
                "1 & x_2 & x_2^2 & \\dots & x_2^{n-1}\\\\\n" +
                "\\vdots & \\vdots & \\vdots & & \\vdots\\\\\n" +
                "1 & x_n & x_n^2 & \\dots & x_n^{n-1}\n" +
                "\\end{vmatrix}\n" +
                "=\\prod_{1\\le j<i\\le n}(x_i-x_j)";

        log.info("测试范德蒙德行列式");
        log.info("输入 LaTeX:\n{}", latex);

        String mathml = formulaConvertService.convertLatexToMathml(latex);
        assertNotNull(mathml, "MathML should not be null");
        log.info("MathML 输出:\n{}", mathml);

        String omml = formulaConvertService.convertLatexToOmml(latex);
        assertNotNull(omml, "OMML should not be null");
        log.info("OMML 输出:\n{}", omml);

        // 验证 OMML 中包含矩阵元素
        assertTrue(omml.contains("<m:m>") || omml.contains("m:m"), "OMML should contain matrix element");
        
        // 验证包含 dots（省略号）
        assertTrue(omml.contains("dots") || omml.contains("⋯") || omml.contains("⋮"), 
            "OMML should contain dots");
    }

    @Test
    public void testSimpleMatrix() {
        String latex = "\\begin{vmatrix}\n" +
                "a & b \\\\\n" +
                "c & d\n" +
                "\\end{vmatrix}";

        log.info("测试简单2x2矩阵");
        log.info("输入 LaTeX:\n{}", latex);

        String mathml = formulaConvertService.convertLatexToMathml(latex);
        assertNotNull(mathml, "MathML should not be null");
        log.info("MathML 输出:\n{}", mathml);

        String omml = formulaConvertService.convertLatexToOmml(latex);
        assertNotNull(omml, "OMML should not be null");
        log.info("OMML 输出:\n{}", omml);
    }
}
