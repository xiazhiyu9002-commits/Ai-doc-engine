package com.aidoc.engine.renderer.impl;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.block.FlowchartBlock;
import com.aidoc.engine.model.udm.content.FlowchartContent;
import com.aidoc.engine.renderer.BlockRenderer;
import com.aidoc.engine.service.MermaidRenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 流程图渲染器
 * 将 Mermaid 流程图渲染为图片并嵌入 Word 文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowchartRenderer implements BlockRenderer<FlowchartBlock> {
    
    private final MermaidRenderService mermaidRenderService;
    
    @Override
    public P render(FlowchartBlock block, WordprocessingMLPackage wordPackage) {
        return render(block, wordPackage, null);
    }
    
    @Override
    public P render(FlowchartBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        FlowchartContent content = block.getContent();
        
        if (content == null) {
            log.warn("流程图内容为空");
            return createPlaceholder("[流程图占位符]");
        }
        
        String rawSource = content.getRawSource();
        if (rawSource == null || rawSource.trim().isEmpty()) {
            log.warn("流程图源码为空");
            return createPlaceholder("[流程图占位符：源码为空]");
        }
        
        log.debug("渲染流程图，使用模板={}", templateConfig != null);
        
        try {
            // 渲染 Mermaid 为图片
            byte[] imageBytes;
            
            // 优先使用已有的 base64 图片
            if (content.getImageBase64() != null && !content.getImageBase64().isEmpty()) {
                imageBytes = Base64.getDecoder().decode(content.getImageBase64());
                log.debug("使用已有的 Base64 图片");
            } else {
                // 使用 Mermaid 渲染服务生成图片
                imageBytes = mermaidRenderService.renderToPng(rawSource);
                log.debug("使用 Mermaid 渲染服务生成图片");
            }
            
            // 将图片嵌入到 Word 文档
            return createImageParagraph(imageBytes, wordPackage);
            
        } catch (Exception e) {
            log.error("流程图渲染失败: {}", e.getMessage(), e);
            return createPlaceholder("[流程图渲染失败: " + e.getMessage() + "]");
        }
    }
    
    /**
     * 创建包含图片的段落
     */
    private P createImageParagraph(byte[] imageBytes, WordprocessingMLPackage wordPackage) throws Exception {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();
        
        // 设置居中对齐
        PPr pPr = factory.createPPr();
        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.CENTER);
        pPr.setJc(jc);
        paragraph.setPPr(pPr);
        
        // 添加图片到文档
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, imageBytes);
        
        // 创建 Inline 图片
        Inline inline = imagePart.createImageInline(
            "Flowchart", 
            "Mermaid Flowchart", 
            0,  // id
            1,  // id
            false
        );
        
        // 创建 Drawing 对象
        org.docx4j.wml.Drawing drawing = factory.createDrawing();
        drawing.getAnchorOrInline().add(inline);
        
        // 添加到 Run
        R run = factory.createR();
        run.getContent().add(drawing);
        paragraph.getContent().add(run);
        
        log.debug("流程图图片渲染成功，图片大小: {} bytes", imageBytes.length);
        
        return paragraph;
    }
    
    /**
     * 创建占位符段落
     */
    private P createPlaceholder(String placeholder) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();
        
        // 设置居中
        PPr pPr = factory.createPPr();
        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.CENTER);
        pPr.setJc(jc);
        paragraph.setPPr(pPr);
        
        // 添加占位符文本
        R run = factory.createR();
        RPr rPr = factory.createRPr();
        
        // 设置蓝色字体
        Color color = factory.createColor();
        color.setVal("0066CC");
        rPr.setColor(color);
        
        run.setRPr(rPr);
        
        Text text = factory.createText();
        text.setValue(placeholder);
        text.setSpace("preserve");
        run.getContent().add(text);
        
        paragraph.getContent().add(run);
        
        return paragraph;
    }
}
