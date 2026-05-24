package com.aidoc.engine.service.impl;

import com.aidoc.engine.model.udm.content.FlowchartContent;
import com.aidoc.engine.service.MermaidParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mermaid 语法解析服务实现
 * 简单地将 Mermaid 代码包装为 FlowchartContent
 */
@Slf4j
@Service
public class MermaidParserServiceImpl implements MermaidParserService {

    @Override
    public FlowchartContent parse(String mermaidCode) {
        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            log.warn("Mermaid 代码为空");
            return FlowchartContent.builder()
                .rawSource("")
                .sourceType("mermaid")
                .build();
        }

        log.debug("解析 Mermaid 代码，长度: {}", mermaidCode.length());
        
        return FlowchartContent.builder()
            .rawSource(mermaidCode)
            .sourceType("mermaid")
            .build();
    }
}
