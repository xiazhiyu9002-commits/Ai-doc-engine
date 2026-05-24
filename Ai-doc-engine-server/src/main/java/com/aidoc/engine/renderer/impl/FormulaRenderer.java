package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.FormulaBlock;
import com.aidoc.engine.model.udm.content.FormulaContent;
import com.aidoc.engine.renderer.BlockRenderer;
import com.aidoc.engine.service.FormulaConvertService;
import com.aidoc.engine.util.OmmlCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.XmlUtils;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.JAXBElement;

/**
 * 公式渲染器
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class FormulaRenderer implements BlockRenderer<FormulaBlock> {
    
    private final FormulaConvertService formulaConvertService;
    
    private static final String MATH_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/math";
    private static final String WML_NAMESPACE = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    
    private static final ObjectFactory MATH_FACTORY = new ObjectFactory();
    
    @Override
    public P render(FormulaBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(FormulaBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        FormulaContent content = block.getContent();
        
        if (content == null || content.getLatex() == null || content.getLatex().trim().isEmpty()) {
            log.warn("公式内容为空");
            return createFallbackParagraph("[空公式]");
        }
        
        String latex = content.getLatex().trim();
        log.info("开始渲染公式: latex={}, 使用模板={}", latex, templateConfig != null);
        
        try {
            String ommlString = formulaConvertService.convertLatexToOmml(latex);
            
            if (ommlString == null || ommlString.trim().isEmpty()) {
                log.error("LaTeX 转 OMML 失败，返回降级段落");
                return createFallbackParagraph(latex);
            }
            
            log.info("原始 OMML 字符串: {}", ommlString);
            
            // 清理不符合 Word 规范的 OMML 元素
            ommlString = OmmlCleaner.clean(ommlString);
            log.info("清理后 OMML 字符串: {}", ommlString);
            
            CTOMath omath = parseOmmlToCTOMath(ommlString);
            if (omath == null) {
                log.error("OMML 解析为 CTOMath 失败，返回降级段落");
                return createFallbackParagraph(latex);
            }

            boolean isEqArray = ommlString.contains("<m:eqArr") || ommlString.contains(":eqArr");
            P paragraph = createMathParagraph(omath, isEqArray);
            
            String paragraphXml = XmlUtils.marshaltoString(paragraph, true, true);
//            log.info("最终段落 XML: {}", paragraphXml);
            
            log.info("公式渲染成功: {}", latex);
            return paragraph;
            
        } catch (Exception e) {
            log.error("公式渲染异常: {}", e.getMessage(), e);
            return createFallbackParagraph(latex);
        }
    }
    
    private CTOMath parseOmmlToCTOMath(String ommlString) {
        try {
            String wrappedOmml = ommlString;
            
            if (!ommlString.contains("<m:oMath") && !ommlString.contains("<oMath")) {
                if (ommlString.contains("xmlns:m=\"")) {
                    wrappedOmml = "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\">" + ommlString + "</m:oMath>";
                } else {
                    wrappedOmml = "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\">" + ommlString + "</m:oMath>";
                }
            }
            
            if (!wrappedOmml.contains("xmlns:m=")) {
                wrappedOmml = wrappedOmml.replace("<m:oMath", "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\"");
            }
            
            log.info("准备解析的 OMML: {}", wrappedOmml);
            
            Object unmarshalled = XmlUtils.unmarshalString(wrappedOmml);
            
            log.info("解析结果类型: {}", unmarshalled != null ? unmarshalled.getClass().getName() : "null");
            
            if (unmarshalled instanceof CTOMath) {
                CTOMath result = (CTOMath) unmarshalled;
                log.info("成功解析为 CTOMath");
                return result;
            } else if (unmarshalled instanceof JAXBElement) {
                JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshalled;
                Object value = jaxbElement.getValue();
                log.info("JAXBElement 值类型: {}", value != null ? value.getClass().getName() : "null");
                
                if (value instanceof CTOMath) {
                    log.info("从 JAXBElement 中提取 CTOMath 成功");
                    return (CTOMath) value;
                } else if (value instanceof CTOMathPara) {
                    // 从 CTOMathPara 中提取第一个 CTOMath
                    CTOMathPara mathPara = (CTOMathPara) value;
                    if (mathPara.getOMath() != null && !mathPara.getOMath().isEmpty()) {
                        CTOMath omath = mathPara.getOMath().get(0);
                        log.info("从 CTOMathPara 中提取 CTOMath 成功");
                        return omath;
                    } else {
                        log.error("CTOMathPara 中没有 CTOMath 对象");
                    }
                }
            } else if (unmarshalled instanceof CTOMathPara) {
                // 直接是 CTOMathPara 的情况
                CTOMathPara mathPara = (CTOMathPara) unmarshalled;
                if (mathPara.getOMath() != null && !mathPara.getOMath().isEmpty()) {
                    CTOMath omath = mathPara.getOMath().get(0);
                    log.info("从 CTOMathPara 中提取 CTOMath 成功");
                    return omath;
                } else {
                    log.error("CTOMathPara 中没有 CTOMath 对象");
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
    
    private P createMathParagraph(CTOMath omath, boolean isEqArray) {
        P p = new P();
        
        // 设置段落属性
        org.docx4j.wml.PPr pPr = new org.docx4j.wml.PPr();
        org.docx4j.wml.Jc jc = new org.docx4j.wml.Jc();
        // 多行对齐(eqArr)在 Word 中更适合左对齐，否则会出现整体居中导致的视觉错位
        jc.setVal(isEqArray ? org.docx4j.wml.JcEnumeration.LEFT : org.docx4j.wml.JcEnumeration.CENTER);
        pPr.setJc(jc);
        p.setPPr(pPr);
        
        CTOMathPara mathPara = MATH_FACTORY.createCTOMathPara();
        mathPara.getOMath().add(omath);
        
        JAXBElement<CTOMathPara> mathParaElement = MATH_FACTORY.createOMathPara(mathPara);
        
        p.getContent().add(mathParaElement);
        
        return p;
    }
    
    private P createFallbackParagraph(String latex) {
        String fallbackXml = 
            "<w:p xmlns:w=\"" + WML_NAMESPACE + "\">" +
                "<w:pPr>" +
                    "<w:jc w:val=\"center\"/>" +
                "</w:pPr>" +
                "<w:r>" +
                    "<w:rPr>" +
                        "<w:i/>" +
                        "<w:color w:val=\"0066CC\"/>" +
                    "</w:rPr>" +
                    "<w:t xml:space=\"preserve\">$$" + escapeXml(latex) + "$$</w:t>" +
                "</w:r>" +
            "</w:p>";
        
        try {
            return (P) XmlUtils.unmarshalString(fallbackXml);
        } catch (Exception e) {
            log.error("降级段落反序列化失败: {}", e.getMessage());
            return null;
        }
    }
    
    private String escapeXml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
