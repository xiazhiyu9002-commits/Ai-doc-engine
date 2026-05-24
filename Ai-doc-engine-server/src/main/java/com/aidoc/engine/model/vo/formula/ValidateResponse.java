package com.aidoc.engine.model.vo.formula;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公式校验响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {
    
    /**
     * 是否有效
     */
    private Boolean valid;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * MathML 格式（可选）
     */
    private String mathml;
}
