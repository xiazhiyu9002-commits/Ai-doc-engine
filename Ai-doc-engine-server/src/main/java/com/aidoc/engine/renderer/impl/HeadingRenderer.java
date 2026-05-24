package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.HeadingBlock;
import com.aidoc.engine.model.udm.content.HeadingContent;
import com.aidoc.engine.renderer.BlockRenderer;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * 标题渲染器
 * 将 HeadingBlock 渲染为 Word 标题段落
 */
@Slf4j
@Component
public class HeadingRenderer implements BlockRenderer<HeadingBlock> {
    
    @Override
    public P render(HeadingBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(HeadingBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        HeadingContent content = block.getContent();
        
        if (content == null || content.getText() == null) {
            log.warn("标题内容为空");
            return null;
        }
        
        ObjectFactory factory = new ObjectFactory();
        
        // 创建段落
        P paragraph = factory.createP();
        
        // 创建段落属性
        PPr paragraphProperties = factory.createPPr();
        
        // 设置标题样式
        PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
        String styleName = getHeadingStyleName(content.getLevel());
        pStyle.setVal(styleName);
        paragraphProperties.setPStyle(pStyle);
        
        paragraph.setPPr(paragraphProperties);
        
        // 创建文本运行
        R run = factory.createR();
        
        // 应用模板字体配置
        if (templateConfig != null && templateConfig.getFontSettings() != null) {
            RPr runProperties = applyHeadingFont(factory, content.getLevel(), templateConfig.getFontSettings());
            if (runProperties != null) {
                run.setRPr(runProperties);
            }
        }
        
        Text text = factory.createText();
        text.setValue(content.getText());
        text.setSpace("preserve");
        run.getContent().add(text);
        
        // 添加运行到段落
        paragraph.getContent().add(run);
        
        log.debug("渲染标题: level={}, text={}, 使用模板={}", 
                content.getLevel(), content.getText(), templateConfig != null);
        
        return paragraph;
    }
    
    /**
     * 应用标题字体配置
     */
    private RPr applyHeadingFont(ObjectFactory factory, Integer level, TemplateConfig.FontSettings fontSettings) {
        if (fontSettings.getHeadingFonts() == null) {
            return null;
        }
        
        TemplateConfig.HeadingFont headingFont = getHeadingFontByLevel(level, fontSettings.getHeadingFonts());
        if (headingFont == null) {
            return null;
        }
        
        RPr rPr = factory.createRPr();
        
        // 设置字体
        if (headingFont.getFamily() != null) {
            RFonts rFonts = factory.createRFonts();
            rFonts.setAscii(headingFont.getFamily());
            rFonts.setHAnsi(headingFont.getFamily());
            rFonts.setEastAsia(headingFont.getFamily());
            rPr.setRFonts(rFonts);
        }
        
        // 设置字号 (半磅为单位，所以要乘以2)
        if (headingFont.getSize() != null) {
            HpsMeasure fontSize = factory.createHpsMeasure();
            fontSize.setVal(BigInteger.valueOf(headingFont.getSize() * 2L));
            rPr.setSz(fontSize);
            rPr.setSzCs(fontSize);
        }
        
        // 设置加粗
        if (Boolean.TRUE.equals(headingFont.getBold())) {
            BooleanDefaultTrue bold = factory.createBooleanDefaultTrue();
            bold.setVal(true);
            rPr.setB(bold);
            rPr.setBCs(bold);
        }
        
        return rPr;
    }
    
    /**
     * 根据级别获取标题字体配置
     */
    private TemplateConfig.HeadingFont getHeadingFontByLevel(Integer level, TemplateConfig.HeadingFonts headingFonts) {
        if (level == null) {
            return headingFonts.getH1();
        }
        
        return switch (level) {
            case 1 -> headingFonts.getH1();
            case 2 -> headingFonts.getH2();
            case 3 -> headingFonts.getH3();
            case 4 -> headingFonts.getH4();
            case 5 -> headingFonts.getH5();
            case 6 -> headingFonts.getH6();
            default -> headingFonts.getH1();
        };
    }
    
    /**
     * 获取标题样式名称
     * 
     * @param level 标题级别（1-6）
     * @return Word 标题样式名称
     */
    private String getHeadingStyleName(Integer level) {
        if (level == null || level < 1 || level > 6) {
            level = 1;
        }
        return "Heading" + level;
    }
}
