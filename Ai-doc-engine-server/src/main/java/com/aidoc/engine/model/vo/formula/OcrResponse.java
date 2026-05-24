package com.aidoc.engine.model.vo.formula;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OCR 识别响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
    
    /**
     * LaTeX 格式或Mermaid代码
     */
    private String latex;
    
    /**
     * 置信度
     */
    private BigDecimal confidence;
    
    /**
     * 内容类型: formula(公式) 或 flowchart(流程图)
     */
    private String contentType;
}
