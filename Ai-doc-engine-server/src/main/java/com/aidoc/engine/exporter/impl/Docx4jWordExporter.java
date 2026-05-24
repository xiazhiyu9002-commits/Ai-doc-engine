package com.aidoc.engine.exporter.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.exporter.WordExporter;
import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.UdmDocument;
import com.aidoc.engine.renderer.DocumentRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * 基于 docx4j 的 Word 导出器实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Docx4jWordExporter implements WordExporter {
    
    private final DocumentRenderer documentRenderer;

    /**
     * 导出 Word 文档
     */
    @Override
    public byte[] export(UdmDocument document) {
        return export(document, null);
    }

    /**
     * 导出 Word 文档（使用模板）
     */
    @Override
    public byte[] export(UdmDocument document, TemplateConfig templateConfig) {
        log.info("开始导出 Word 文档，使用模板: {}", templateConfig != null ? "是" : "否");
        
        try {
            // 创建 Word 文档包
            WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
            
            // 渲染文档内容（传递模板配置）
            documentRenderer.render(document, wordPackage, templateConfig);
            
            // 导出为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            wordPackage.save(outputStream);
            byte[] bytes = outputStream.toByteArray();
            
            log.info("Word 文档导出成功，大小: {} bytes", bytes.length);
            
            return bytes;
            
        } catch (Docx4JException e) {
            log.error("Word 文档导出失败", e);
            throw new BusinessException(ErrorCode.EXPORT_FAILED, "Word 文档导出失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Word 文档导出异常", e);
            throw new BusinessException(ErrorCode.EXPORT_FAILED, "Word 文档导出异常: " + e.getMessage());
        }
    }
}
