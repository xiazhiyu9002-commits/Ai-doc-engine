package com.aidoc.engine.exporter;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.udm.UdmDocument;

/**
 * Word 导出器接口
 */
public interface WordExporter {
    
    /**
     * 导出 UDM 文档为 Word 字节数组
     * 
     * @param document UDM 文档
     * @return Word 文档字节数组
     */
    byte[] export(UdmDocument document);
    
    /**
     * 导出 UDM 文档为 Word 字节数组（使用指定模板配置）
     * 
     * @param document UDM 文档
     * @param templateConfig 模板配置
     * @return Word 文档字节数组
     */
    byte[] export(UdmDocument document, TemplateConfig templateConfig);
}
