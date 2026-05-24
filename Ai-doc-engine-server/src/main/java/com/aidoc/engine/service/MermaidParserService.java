package com.aidoc.engine.service;

import com.aidoc.engine.model.udm.content.FlowchartContent;

/**
 * Mermaid 语法解析服务
 * 将 Mermaid 语法解析为 FlowchartContent
 */
public interface MermaidParserService {

    /**
     * 解析 Mermaid 代码
     * @param mermaidCode Mermaid 源码
     * @return 解析后的流程图内容
     */
    FlowchartContent parse(String mermaidCode);
}
