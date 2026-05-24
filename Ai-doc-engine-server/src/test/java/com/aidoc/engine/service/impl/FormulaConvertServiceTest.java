package com.aidoc.engine.service.impl;

import com.aidoc.engine.service.FormulaConvertService;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.XmlUtils;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.xml.bind.JAXBElement;

import java.io.FileOutputStream;

@Slf4j
@SpringBootTest
public class FormulaConvertServiceTest {

    @Autowired
    private FormulaConvertService formulaConvertService;

    private static final String MATH_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/math";
    private static final ObjectFactory MATH_FACTORY = new ObjectFactory();

    @Test
    public void testFullConversionChain() throws Exception {
        String latex = "x^2";
        
        log.info("========== 测试 LaTeX 转 MathML ==========");
        String mathml = formulaConvertService.convertLatexToMathml(latex);
        log.info("MathML 输出:\n{}", mathml);
        
        log.info("========== 测试 MathML 转 OMML ==========");
        String omml = formulaConvertService.convertMathmlToOmml(mathml);
        log.info("OMML 输出:\n{}", omml);

        org.junit.jupiter.api.Assertions.assertFalse(omml.contains("\u2061"), "OMML should not contain U+2061 (FUNCTION APPLICATION)");
        org.junit.jupiter.api.Assertions.assertFalse(omml.contains("\u2062"), "OMML should not contain U+2062 (INVISIBLE TIMES)");
        org.junit.jupiter.api.Assertions.assertFalse(omml.contains("\u2063"), "OMML should not contain U+2063 (INVISIBLE SEPARATOR)");
        org.junit.jupiter.api.Assertions.assertFalse(omml.contains("&#x2061;"), "OMML should not contain &#x2061; entity");
        org.junit.jupiter.api.Assertions.assertFalse(omml.contains("&#x2062;"), "OMML should not contain &#x2062; entity");
        org.junit.jupiter.api.Assertions.assertFalse(omml.contains("&#x2063;"), "OMML should not contain &#x2063; entity");
        
            log.info("========== 测试 OMML 解析为 CTOMath ==========");
        CTOMath ctomath = parseOmmlToCTOMath(omml);
        if (ctomath != null) {
            log.info("CTOMath 解析成功");
            // 使用 MATH_FACTORY 包裹以避免 @XmlRootElement 缺失错误
            String ctomathXml = XmlUtils.marshaltoString(MATH_FACTORY.createOMath(ctomath), true, true);
            log.info("CTOMath XML:\n{}", ctomathXml);
        } else {
            log.error("CTOMath 解析失败");
        }
        
        log.info("========== 测试创建 Word 文档 ==========");
        P paragraph = createMathParagraph(ctomath);
        String pXml = XmlUtils.marshaltoString(paragraph, true, true);
        log.info("段落 XML:\n{}", pXml);
        
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
        wordPackage.getMainDocumentPart().addObject(paragraph);
        wordPackage.save(new FileOutputStream("test_formula_output.docx"));
        log.info("Word 文档已保存到 test_formula_output.docx");
    }

    @Test
    public void testSummationConversion() throws Exception {
        String latex = "\\sum_{i=1}^{m} x_i";
        
        log.info("========== 测试求和公式 LaTeX 转 MathML ==========");
        String mathml = formulaConvertService.convertLatexToMathml(latex);
        log.info("MathML 输出:\n{}", mathml);
        
        log.info("========== 测试 MathML 转 OMML ==========");
        String omml = formulaConvertService.convertMathmlToOmml(mathml);
        log.info("OMML 输出:\n{}", omml);
        
        CTOMath ctomath = parseOmmlToCTOMath(omml);
        org.junit.jupiter.api.Assertions.assertNotNull(ctomath, "CTOMath should not be null");
        
        // 打印 CTOMath 的 XML 以检查 nary 结构
        String ctomathXml = XmlUtils.marshaltoString(MATH_FACTORY.createOMath(ctomath), true, true);
        log.info("CTOMath XML:\n{}", ctomathXml);
    }

    @Test
    public void testValidateWithDollarDelimiters() {
        String latex = "$$x^2$$";

        String mathml = formulaConvertService.convertLatexToMathml(latex);
        org.junit.jupiter.api.Assertions.assertNotNull(mathml, "MathML should not be null for $$...$$ input");

        String omml = formulaConvertService.convertLatexToOmml(latex);
        org.junit.jupiter.api.Assertions.assertNotNull(omml, "OMML should not be null for $$...$$ input");
    }

    @Test
    public void testUserProvidedFormulasWithDollarDelimiters() {
        String[] formulas = new String[] {
                "$$r_{im} = -\\left[ \\frac{\\partial L(y_i, F(x_i))}{\\partial F(x_i)} \\right]_{F(x)=F_{m-1}(x)}$$",
                "$$a^{(l)}_j = g\\left( \\sum_{k} \\theta^{(l-1)}_{jk} a^{(l-1)}_k + b^{(l)}_j \\right)$$",
                "$$J(\\theta) = -\\frac{1}{m} \\sum_{i=1}^{m} \\left[ y^{(i)} \\log(h_\\theta(x^{(i)})) + (1 - y^{(i)}) \\log(1 - h_\\theta(x^{(i)})) \\right]$$"
        };

        for (String latex : formulas) {
            String mathml = formulaConvertService.convertLatexToMathml(latex);
            org.junit.jupiter.api.Assertions.assertNotNull(mathml, "MathML should not be null for: " + latex);

            String omml = formulaConvertService.convertLatexToOmml(latex);
            org.junit.jupiter.api.Assertions.assertNotNull(omml, "OMML should not be null for: " + latex);

            org.junit.jupiter.api.Assertions.assertFalse(omml.contains("\u2061"), "OMML should not contain U+2061 (FUNCTION APPLICATION)");
            org.junit.jupiter.api.Assertions.assertFalse(omml.contains("\u2062"), "OMML should not contain U+2062 (INVISIBLE TIMES)");
            org.junit.jupiter.api.Assertions.assertFalse(omml.contains("\u2063"), "OMML should not contain U+2063 (INVISIBLE SEPARATOR)");
            org.junit.jupiter.api.Assertions.assertFalse(omml.contains("&#x2061;"), "OMML should not contain &#x2061; entity");
            org.junit.jupiter.api.Assertions.assertFalse(omml.contains("&#x2062;"), "OMML should not contain &#x2062; entity");
            org.junit.jupiter.api.Assertions.assertFalse(omml.contains("&#x2063;"), "OMML should not contain &#x2063; entity");
        }
    }

    @Test
    public void testNestedSummationConversion() throws Exception {
        String latex = "\\sum_{i=1}^{m} \\sum_{k=1}^{K} r_{ik} \\lVert x^{(i)} - \\mu_k \\rVert^2";

        log.info("========== 测试嵌套求和公式 LaTeX 转 MathML ==========");
        String mathml = formulaConvertService.convertLatexToMathml(latex);
        log.info("MathML 输出:\n{}", mathml);

        log.info("========== 测试 MathML 转 OMML ==========");
        String omml = formulaConvertService.convertMathmlToOmml(mathml);
        log.info("OMML 输出:\n{}", omml);

        CTOMath ctomath = parseOmmlToCTOMath(omml);
        org.junit.jupiter.api.Assertions.assertNotNull(ctomath, "CTOMath should not be null");

        String ctomathXml = XmlUtils.marshaltoString(MATH_FACTORY.createOMath(ctomath), true, true);
        log.info("CTOMath XML:\n{}", ctomathXml);
    }

    @Test
    public void testChemicalEquationsWithConditions() throws Exception {
        // 测试化学方程式：带反应条件的箭头
        String[] chemicalEquations = new String[] {
                "\\ce{4P + 5O2 \\xlongequal{\\text{点燃}} 2P2O5}",
                "\\ce{S + O2 \\xlongequal{\\text{点燃}} SO2}",
                "\\ce{C + O2 \\xlongequal{\\text{点燃}} CO2}",
                "\\ce{3Fe + 2O2 \\xlongequal{\\text{点燃}} Fe3O4}",
                "\\ce{4NH3 + 5O2 \\xlongequal[\\Delta]{\\text{催化剂}} 4NO + 6H2O}"
        };

        for (String latex : chemicalEquations) {
            log.info("========== 测试化学方程式: {} ==========", latex);
            
            String mathml = formulaConvertService.convertLatexToMathml(latex);
            log.info("MathML 输出:\n{}", mathml);
            org.junit.jupiter.api.Assertions.assertNotNull(mathml, "MathML should not be null for: " + latex);
            
            // 验证MathML包含箭头和文本
            org.junit.jupiter.api.Assertions.assertTrue(mathml.contains("→") || mathml.contains("&rarr;"), 
                "MathML should contain arrow symbol");
            org.junit.jupiter.api.Assertions.assertTrue(mathml.contains("<mtext>") || mathml.contains("mover") || mathml.contains("munderover"), 
                "MathML should contain text or over/underover elements");
            
            String omml = formulaConvertService.convertLatexToOmml(latex);
            log.info("OMML 输出:\n{}", omml);
            org.junit.jupiter.api.Assertions.assertNotNull(omml, "OMML should not be null for: " + latex);
        }
    }

    private CTOMath parseOmmlToCTOMath(String ommlString) {
        try {
            String wrappedOmml;
            if (ommlString.contains("<m:oMath")) {
                wrappedOmml = ommlString;
            } else {
                wrappedOmml = "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\">" + ommlString + "</m:oMath>";
            }
            
            log.debug("准备解析的 OMML: {}", wrappedOmml);
            
            Object unmarshalled = XmlUtils.unmarshalString(wrappedOmml);
            
            if (unmarshalled instanceof CTOMath) {
                return (CTOMath) unmarshalled;
            } else if (unmarshalled instanceof JAXBElement) {
                JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshalled;
                Object value = jaxbElement.getValue();
                if (value instanceof CTOMath) {
                    return (CTOMath) value;
                }
            }
            
            log.error("无法将 OMML 解析为 CTOMath，实际类型: {}", 
                unmarshalled != null ? unmarshalled.getClass().getName() : "null");
            return null;
            
        } catch (Exception e) {
            log.error("解析 OMML 到 CTOMath 异常: {}", e.getMessage(), e);
            return null;
        }
    }

    private P createMathParagraph(CTOMath omath) {
        P p = new P();
        
        CTOMathPara mathPara = MATH_FACTORY.createCTOMathPara();
        mathPara.getOMath().add(omath);
        
        JAXBElement<CTOMathPara> mathParaElement = MATH_FACTORY.createOMathPara(mathPara);
        
        p.getContent().add(mathParaElement);
        
        return p;
    }
}
