package com.aidoc.engine.model.udm.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公式内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormulaContent {
    
    /**
     * LaTeX 格式（必需）
     */
    private String latex;
    
    /**
     * MathML 格式（可选）
     */
    private String mathml;
    
    /**
     * 是否行内公式
     */
    @Builder.Default
    private Boolean inline = false;
}
