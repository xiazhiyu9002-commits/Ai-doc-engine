package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.ParagraphBlock;
import com.aidoc.engine.model.udm.content.ParagraphContent;
import com.aidoc.engine.model.udm.content.RichText;
import com.aidoc.engine.renderer.BlockRenderer;
import com.aidoc.engine.service.FormulaConvertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.math.CTOMath;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.docx4j.XmlUtils;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;

/**
 * 段落渲染器
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ParagraphRenderer implements BlockRenderer<ParagraphBlock> {
    
    private final FormulaConvertService formulaConvertService;
    
    private static final String MATH_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/math";
    private static final org.docx4j.math.ObjectFactory MATH_FACTORY = new org.docx4j.math.ObjectFactory();
    
    @Override
    public P render(ParagraphBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(ParagraphBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        ParagraphContent content = block.getContent();
        
        if (content == null) {
            log.warn("段落内容为空");
            return null;
        }
        
        ObjectFactory factory = new ObjectFactory();
        P paragraph = factory.createP();
        
        // 应用段落格式
        if (templateConfig != null && templateConfig.getParagraphSettings() != null) {
            PPr pPr = applyParagraphFormat(factory, templateConfig.getParagraphSettings());
            paragraph.setPPr(pPr);
        }
        
        List<RichText> segments = content.getEffectiveSegments();
        
        if (segments.isEmpty()) {
            if (content.getText() != null) {
                R run = createTextRun(factory, content.getText(), templateConfig);
                paragraph.getContent().add(run);
            }
        } else {
            for (RichText segment : segments) {
                Object runOrMath = createRichTextContent(segment, factory, templateConfig);
                paragraph.getContent().add(runOrMath);
            }
        }
        
        log.debug("渲染段落: segments={}, 使用模板={}", segments.size(), templateConfig != null);
        return paragraph;
    }
    
    /**
     * 应用段落格式
     */
    private PPr applyParagraphFormat(ObjectFactory factory, TemplateConfig.ParagraphSettings settings) {
        PPr pPr = factory.createPPr();
        
        // 行距
        if (settings.getLineSpacing() != null) {
            PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
            // 如果行距大于10，认为是固定值（磅），否则是倍数
            if (settings.getLineSpacing() > 10) {
                // 固定值：磅转twips (1磅 = 20 twips)
                spacing.setLine(BigInteger.valueOf((long)(settings.getLineSpacing() * 20)));
                spacing.setLineRule(STLineSpacingRule.EXACT);
                log.debug("应用固定行距: {} 磅 = {} twips", settings.getLineSpacing(), settings.getLineSpacing() * 20);
            } else {
                // 倍数：转换为240单位 (1倍 = 240)
                spacing.setLine(BigInteger.valueOf((long)(settings.getLineSpacing() * 240)));
                spacing.setLineRule(STLineSpacingRule.AUTO);
                log.debug("应用倍数行距: {} 倍 = {} 单位", settings.getLineSpacing(), settings.getLineSpacing() * 240);
            }
            pPr.setSpacing(spacing);
        }
        
        // 段落间距
        if (settings.getParagraphSpacing() != null) {
            PPrBase.Spacing spacing = pPr.getSpacing();
            if (spacing == null) {
                spacing = factory.createPPrBaseSpacing();
                pPr.setSpacing(spacing);
            }
            if (settings.getParagraphSpacing().getBefore() != null) {
                // 磅转twips (1磅 = 20 twips)
                spacing.setBefore(BigInteger.valueOf(settings.getParagraphSpacing().getBefore() * 20L));
                log.debug("应用段前间距: {} 磅", settings.getParagraphSpacing().getBefore());
            }
            if (settings.getParagraphSpacing().getAfter() != null) {
                // 磅转twips (1磅 = 20 twips)
                spacing.setAfter(BigInteger.valueOf(settings.getParagraphSpacing().getAfter() * 20L));
                log.debug("应用段后间距: {} 磅", settings.getParagraphSpacing().getAfter());
            }
        }
        
        // 首行缩进
        if (settings.getFirstLineIndent() != null && settings.getFirstLineIndent() > 0) {
            PPrBase.Ind ind = factory.createPPrBaseInd();
            // 中文字符缩进：1字符 ≈ 2个英文字符宽度
            // 使用字号的2倍作为字符宽度：12号字 = 240 twips/字符
            // 2字符缩进 = 480 twips
            ind.setFirstLine(BigInteger.valueOf(settings.getFirstLineIndent() * 240L));
            pPr.setInd(ind);
            log.debug("应用首行缩进: {} 字符 = {} twips", settings.getFirstLineIndent(), settings.getFirstLineIndent() * 240);
        }
        
        // 对齐方式
        if (settings.getAlignment() != null) {
            Jc jc = factory.createJc();
            switch (settings.getAlignment().toLowerCase()) {
                case "center" -> jc.setVal(JcEnumeration.CENTER);
                case "right" -> jc.setVal(JcEnumeration.RIGHT);
                case "justify" -> jc.setVal(JcEnumeration.BOTH);
                default -> jc.setVal(JcEnumeration.LEFT);
            }
            pPr.setJc(jc);
            log.debug("应用对齐方式: {}", settings.getAlignment());
        }
        
        return pPr;
    }
    
    /**
     * 创建普通文本运行
     */
    private R createTextRun(ObjectFactory factory, String textContent, TemplateConfig templateConfig) {
        R run = factory.createR();
        
        // 应用字体设置
        if (templateConfig != null && templateConfig.getFontSettings() != null) {
            RPr rPr = applyFontSettings(factory, templateConfig.getFontSettings());
            run.setRPr(rPr);
        }
        
        Text text = factory.createText();
        text.setValue(textContent);
        text.setSpace("preserve");
        run.getContent().add(text);
        
        return run;
    }
    
    /**
     * 应用字体设置
     */
    private RPr applyFontSettings(ObjectFactory factory, TemplateConfig.FontSettings fontSettings) {
        RPr rPr = factory.createRPr();
        
        // 字体
        if (fontSettings.getFontFamily() != null) {
            RFonts rFonts = factory.createRFonts();
            rFonts.setAscii(fontSettings.getFontFamily());
            rFonts.setHAnsi(fontSettings.getFontFamily());
            rFonts.setEastAsia(fontSettings.getFontFamily());
            rPr.setRFonts(rFonts);
        }
        
        // 字号 (半磅为单位)
        if (fontSettings.getFontSize() != null) {
            HpsMeasure fontSize = factory.createHpsMeasure();
            fontSize.setVal(BigInteger.valueOf(fontSettings.getFontSize() * 2L));
            rPr.setSz(fontSize);
            rPr.setSzCs(fontSize);
        }
        
        return rPr;
    }
    
    private Object createRichTextContent(RichText segment, ObjectFactory factory, TemplateConfig templateConfig) {
        if (segment.getInlineFormula() != null && !segment.getInlineFormula().isEmpty()) {
            return createInlineFormulaRun(segment, factory);
        }
        
        return createRichTextRun(segment, factory, templateConfig);
    }
    
    private R createRichTextRun(RichText segment, ObjectFactory factory, TemplateConfig templateConfig) {
        R run = factory.createR();
        
        RPr rPr = factory.createRPr();
        boolean hasFormat = false;
        
        // 应用模板字体设置
        if (templateConfig != null && templateConfig.getFontSettings() != null) {
            RPr templateRPr = applyFontSettings(factory, templateConfig.getFontSettings());
            if (templateRPr.getRFonts() != null) {
                rPr.setRFonts(templateRPr.getRFonts());
                hasFormat = true;
            }
            if (templateRPr.getSz() != null) {
                rPr.setSz(templateRPr.getSz());
                rPr.setSzCs(templateRPr.getSzCs());
                hasFormat = true;
            }
        }
        
        if (Boolean.TRUE.equals(segment.getBold())) {
            rPr.setB(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
        if (Boolean.TRUE.equals(segment.getItalic())) {
            rPr.setI(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
        if (Boolean.TRUE.equals(segment.getStrikethrough())) {
            rPr.setStrike(factory.createBooleanDefaultTrue());
            hasFormat = true;
        }
        
        if (Boolean.TRUE.equals(segment.getCode())) {
            RFonts fonts = factory.createRFonts();
            String codeFont = "Consolas";
            if (templateConfig != null && templateConfig.getFontSettings() != null 
                && templateConfig.getFontSettings().getCodeFontFamily() != null) {
                codeFont = templateConfig.getFontSettings().getCodeFontFamily();
            }
            fonts.setAscii(codeFont);
            fonts.setHAnsi(codeFont);
            fonts.setCs(codeFont);
            rPr.setRFonts(fonts);
            
            Highlight highlight = factory.createHighlight();
            highlight.setVal("lightGray");
            rPr.setHighlight(highlight);
            hasFormat = true;
        }
        
        if (hasFormat) {
            run.setRPr(rPr);
        }
        
        String textContent = segment.getText();
        if (textContent != null && !textContent.isEmpty()) {
            if ("\n".equals(textContent)) {
                Br br = factory.createBr();
                run.getContent().add(br);
            } else {
                Text text = factory.createText();
                text.setValue(textContent);
                text.setSpace("preserve");
                run.getContent().add(text);
            }
        }
        
        if (segment.getLinkUrl() != null && !segment.getLinkUrl().isEmpty()) {
            if (textContent == null || textContent.isEmpty()) {
                Text text = factory.createText();
                text.setValue(segment.getLinkUrl());
                text.setSpace("preserve");
                run.getContent().add(text);
            }
        }
        
        return run;
    }
    
    private Object createInlineFormulaRun(RichText segment, ObjectFactory factory) {
        String latex = segment.getInlineFormula();
        log.info("渲染行内公式: {}", latex);
        
        try {
            String ommlString = formulaConvertService.convertLatexToOmml(latex, true);
            
            if (ommlString == null || ommlString.trim().isEmpty()) {
                log.warn("行内公式转换失败，降级为文本: {}", latex);
                return createFallbackFormulaRun(latex, factory);
            }
            
            CTOMath omath = parseOmmlToCTOMath(ommlString);
            if (omath == null) {
                log.warn("行内公式 OMML 解析失败，降级为文本: {}", latex);
                return createFallbackFormulaRun(latex, factory);
            }
            
            return MATH_FACTORY.createOMath(omath);
            
        } catch (Exception e) {
            log.error("行内公式渲染异常: {}", e.getMessage(), e);
            return createFallbackFormulaRun(latex, factory);
        }
    }
    
    private CTOMath parseOmmlToCTOMath(String ommlString) {
        try {
            String wrappedOmml = ommlString;
            
            if (!ommlString.contains("<m:oMath") && !ommlString.contains("<oMath")) {
                wrappedOmml = "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\">" + ommlString + "</m:oMath>";
            }
            
            if (!wrappedOmml.contains("xmlns:m=")) {
                wrappedOmml = wrappedOmml.replace("<m:oMath", "<m:oMath xmlns:m=\"" + MATH_NAMESPACE + "\"");
            }
            
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
    
    private R createFallbackFormulaRun(String latex, ObjectFactory factory) {
        R run = factory.createR();
        RPr rPr = factory.createRPr();
        
        BooleanDefaultTrue italic = factory.createBooleanDefaultTrue();
        rPr.setI(italic);
        
        Color color = factory.createColor();
        color.setVal("0066CC");
        rPr.setColor(color);
        
        run.setRPr(rPr);
        
        Text text = factory.createText();
        text.setValue("$" + latex + "$");
        text.setSpace("preserve");
        run.getContent().add(text);
        
        return run;
    }
}
