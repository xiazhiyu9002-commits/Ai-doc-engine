package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.CodeBlock;
import com.aidoc.engine.model.udm.content.CodeBlockContent;
import com.aidoc.engine.renderer.BlockRenderer;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.springframework.stereotype.Component;

/**
 * 代码块渲染器
 * 将 CodeBlock 渲染为 Word 代码块（灰色背景、等宽字体）
 */
@Slf4j
@Component
public class CodeBlockRenderer implements BlockRenderer<CodeBlock> {
    
    @Override
    public P render(CodeBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(CodeBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        CodeBlockContent content = block.getContent();
        
        if (content == null) {
            log.warn("代码块内容为空");
            return createEmptyCodeBlock(templateConfig);
        }
        
        String language = content.getLanguage();
        String code = content.getCode();
        
        if (code == null || code.trim().isEmpty()) {
            log.warn("代码块内容为空");
            return createEmptyCodeBlock(templateConfig);
        }
        
        log.debug("渲染代码块: language={}, length={}, 使用模板={}", language, code.length(), templateConfig != null);
        
        // 渲染实际的代码内容
        return createCodeBlock(code, language, templateConfig);
    }
    
    /**
     * 创建代码块段落
     */
    private P createCodeBlock(String code, String language, TemplateConfig templateConfig) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();
        
        // 设置段落属性
        PPr pPr = factory.createPPr();
        
        // 设置段落边框和背景（灰色背景）
        PPrBase.PBdr pBdr = factory.createPPrBasePBdr();
        CTBorder border = factory.createCTBorder();
        border.setVal(STBorder.SINGLE);
        border.setSz(java.math.BigInteger.valueOf(4));
        border.setColor("D0D0D0");
        border.setSpace(java.math.BigInteger.valueOf(1));
        pBdr.setTop(border);
        pBdr.setBottom(border);
        pBdr.setLeft(border);
        pBdr.setRight(border);
        pPr.setPBdr(pBdr);
        
        // 设置段落底纹（背景色）
        CTShd shd = factory.createCTShd();
        shd.setVal(STShd.CLEAR);
        shd.setColor("auto");
        shd.setFill("F6F8FA");  // 浅灰色背景
        pPr.setShd(shd);
        
        // 设置段落间距
        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setBefore(java.math.BigInteger.valueOf(120));  // 6pt
        spacing.setAfter(java.math.BigInteger.valueOf(120));   // 6pt
        spacing.setLine(java.math.BigInteger.valueOf(276));    // 1.15倍行距
        spacing.setLineRule(STLineSpacingRule.AUTO);
        pPr.setSpacing(spacing);
        
        // 设置段落缩进
        PPrBase.Ind ind = factory.createPPrBaseInd();
        ind.setLeft(java.math.BigInteger.valueOf(144));   // 左缩进 0.25英寸
        ind.setRight(java.math.BigInteger.valueOf(144));  // 右缩进 0.25英寸
        pPr.setInd(ind);
        
        paragraph.setPPr(pPr);
        
        // 添加语言标签（如果有）
        if (language != null && !language.isEmpty()) {
            R labelRun = factory.createR();
            RPr labelRPr = factory.createRPr();
            
            // 设置小字体
            HpsMeasure fontSize = factory.createHpsMeasure();
            fontSize.setVal(java.math.BigInteger.valueOf(18));  // 9pt
            labelRPr.setSz(fontSize);
            labelRPr.setSzCs(fontSize);
            
            // 设置颜色
            Color labelColor = factory.createColor();
            labelColor.setVal("0066CC");
            labelRPr.setColor(labelColor);
            
            // 设置字体
            RFonts labelFonts = factory.createRFonts();
            labelFonts.setAscii("Consolas");
            labelFonts.setHAnsi("Consolas");
            labelRPr.setRFonts(labelFonts);
            
            labelRun.setRPr(labelRPr);
            
            Text labelText = factory.createText();
            labelText.setValue("[" + language + "]");
            labelText.setSpace("preserve");
            labelRun.getContent().add(labelText);
            
            paragraph.getContent().add(labelRun);
            
            // 添加换行
            Br br = factory.createBr();
            R brRun = factory.createR();
            brRun.getContent().add(br);
            paragraph.getContent().add(brRun);
        }
        
        // 处理代码内容：按行分割并添加换行符
        String[] lines = code.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // 创建代码行的 Run
            R codeRun = factory.createR();
            RPr codeRPr = factory.createRPr();
            
            // 设置等宽字体（优先使用模板配置）
            RFonts fonts = factory.createRFonts();
            String codeFont = "Consolas";
            if (templateConfig != null && templateConfig.getFontSettings() != null 
                && templateConfig.getFontSettings().getCodeFontFamily() != null) {
                codeFont = templateConfig.getFontSettings().getCodeFontFamily();
            }
            fonts.setAscii(codeFont);
            fonts.setHAnsi(codeFont);
            fonts.setCs(codeFont);
            codeRPr.setRFonts(fonts);
            
            // 设置字体大小（优先使用模板配置）
            HpsMeasure fontSize = factory.createHpsMeasure();
            int codeFontSize = 10;  // 默认10pt
            if (templateConfig != null && templateConfig.getFontSettings() != null 
                && templateConfig.getFontSettings().getCodeFontSize() != null) {
                codeFontSize = templateConfig.getFontSettings().getCodeFontSize();
            }
            fontSize.setVal(java.math.BigInteger.valueOf(codeFontSize * 2));  // Word使用半磅
            codeRPr.setSz(fontSize);
            codeRPr.setSzCs(fontSize);
            
            // 设置字体颜色
            Color color = factory.createColor();
            color.setVal("24292E");  // 深灰色
            codeRPr.setColor(color);
            
            codeRun.setRPr(codeRPr);
            
            // 添加代码文本（保留空格）
            Text codeText = factory.createText();
            codeText.setValue(line);
            codeText.setSpace("preserve");  // 保留空格
            codeRun.getContent().add(codeText);
            
            paragraph.getContent().add(codeRun);
            
            // 如果不是最后一行，添加换行符
            if (i < lines.length - 1) {
                R brRun = factory.createR();
                RPr brRPr = factory.createRPr();
                
                // 换行符也需要设置字体，否则行高可能不一致
                RFonts brFonts = factory.createRFonts();
                brFonts.setAscii("Consolas");
                brFonts.setHAnsi("Consolas");
                brRPr.setRFonts(brFonts);
                
                HpsMeasure brFontSize = factory.createHpsMeasure();
                brFontSize.setVal(java.math.BigInteger.valueOf(20));
                brRPr.setSz(brFontSize);
                brRPr.setSzCs(brFontSize);
                
                brRun.setRPr(brRPr);
                
                Br br = factory.createBr();
                brRun.getContent().add(br);
                paragraph.getContent().add(brRun);
            }
        }
        
        return paragraph;
    }
    
    /**
     * 创建空代码块
     */
    private P createEmptyCodeBlock(TemplateConfig templateConfig) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();
        
        R run = factory.createR();
        RPr rPr = factory.createRPr();
        
        Color color = factory.createColor();
        color.setVal("999999");
        rPr.setColor(color);
        
        run.setRPr(rPr);
        
        Text text = factory.createText();
        text.setValue("[空代码块]");
        text.setSpace("preserve");
        run.getContent().add(text);
        
        paragraph.getContent().add(run);
        
        return paragraph;
    }
}
