package com.aidoc.engine.renderer;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.UdmBlock;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.model.udm.block.*;
import com.aidoc.engine.renderer.impl.CodeBlockRenderer;
import com.aidoc.engine.renderer.impl.FlowchartRenderer;
import com.aidoc.engine.renderer.impl.FormulaRenderer;
import com.aidoc.engine.renderer.impl.HeadingRenderer;
import com.aidoc.engine.renderer.impl.ListRenderer;
import com.aidoc.engine.renderer.impl.ParagraphRenderer;
import com.aidoc.engine.renderer.impl.TableRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.springframework.stereotype.Component;

/**
 * 文档渲染器
 * 遍历 UDM 文档并调用对应的 Block 渲染器
 * 禁止静默降级，确保每种 Block 类型都有对应的渲染器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentRenderer {
    
    private final HeadingRenderer headingRenderer;
    private final ParagraphRenderer paragraphRenderer;
    private final TableRenderer tableRenderer;
    private final FormulaRenderer formulaRenderer;
    private final FlowchartRenderer flowchartRenderer;
    private final ListRenderer listRenderer;
    private final CodeBlockRenderer codeBlockRenderer;
    private final TemplateApplier templateApplier;
    
    /**
     * 渲染 UDM 文档到 Word 包（不使用模板）
     * 
     * @param document UDM 文档
     * @param wordPackage Word 文档包
     */
    public void render(UdmDocument document, WordprocessingMLPackage wordPackage) {
        render(document, wordPackage, null);
    }
    
    /**
     * 渲染 UDM 文档到 Word 包（使用模板配置）
     * 
     * @param document UDM 文档
     * @param wordPackage Word 文档包
     * @param templateConfig 模板配置
     */
    public void render(UdmDocument document, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        log.info("开始渲染文档，Block 数量: {}, 使用模板: {}",
                document.getBlocks().size(), 
                templateConfig != null ? "是" : "否");
        
        // 应用模板配置（页面设置、页眉页脚等）
        if (templateConfig != null) {
            log.info("  → 应用模板配置到文档");
            templateApplier.applyTemplate(wordPackage, templateConfig);
        } else {
            log.warn("未提供模板配置，使用默认格式");
        }
        
        // 渲染各个Block
        int blockIndex = 0;
        for (UdmBlock block : document.getBlocks()) {
            blockIndex++;
            log.debug("  → 渲染 Block {}/{}: type={}", blockIndex, document.getBlocks().size(), block.getType());
            
            P paragraph = renderBlock(block, wordPackage, templateConfig);
            
            if (paragraph != null) {
                // 添加段落到文档主体
                wordPackage.getMainDocumentPart().getContent().add(paragraph);
            }
        }
        
        log.info("文档渲染完成，共渲染 {} 个 Block", document.getBlocks().size());
    }
    
    /**
     * 根据 Block 类型渲染对应的段落
     * 禁止静默降级，未知类型记录错误日志
     * 
     * @param block UDM Block
     * @param wordPackage Word 文档包
     * @param templateConfig 模板配置
     * @return Word 段落对象
     */
    private P renderBlock(UdmBlock block, WordprocessingMLPackage wordPackage, TemplateConfig templateConfig) {
        if (block == null) {
            log.error("Block 为 null，跳过渲染");
            return null;
        }
        
        String blockType = block.getType();
        log.debug("渲染 Block: type={}", blockType);
        
        if (block instanceof HeadingBlock) {
            return headingRenderer.render((HeadingBlock) block, wordPackage, templateConfig);
        } else if (block instanceof ParagraphBlock) {
            return paragraphRenderer.render((ParagraphBlock) block, wordPackage, templateConfig);
        } else if (block instanceof ListBlock) {
            return listRenderer.render((ListBlock) block, wordPackage, templateConfig);
        } else if (block instanceof TableBlock) {
            return tableRenderer.render((TableBlock) block, wordPackage, templateConfig);
        } else if (block instanceof FormulaBlock) {
            return formulaRenderer.render((FormulaBlock) block, wordPackage, templateConfig);
        } else if (block instanceof FlowchartBlock) {
            return flowchartRenderer.render((FlowchartBlock) block, wordPackage, templateConfig);
        } else if (block instanceof CodeBlock) {
            return codeBlockRenderer.render((CodeBlock) block, wordPackage, templateConfig);
        } else {
            // 未知 Block 类型，记录错误并返回占位符
            log.error("未知的 Block 类型: {}, class={}", blockType, block.getClass().getSimpleName());
            return createUnknownBlockPlaceholder(blockType);
        }
    }
    
    /**
     * 创建未知 Block 类型的占位符
     */
    private P createUnknownBlockPlaceholder(String blockType) {
        org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
        P paragraph = factory.createP();
        
        org.docx4j.wml.R run = factory.createR();
        org.docx4j.wml.Text text = factory.createText();
        text.setValue("[未知内容类型: " + blockType + "]");
        text.setSpace("preserve");
        run.getContent().add(text);
        paragraph.getContent().add(run);
        
        return paragraph;
    }
}
