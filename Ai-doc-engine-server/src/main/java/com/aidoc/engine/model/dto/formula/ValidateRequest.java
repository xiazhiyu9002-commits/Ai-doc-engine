package com.aidoc.engine.model.dto.formula;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公式校验请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRequest {
    
    @NotBlank(message = "LaTeX公式不能为空")
    private String latex;
}
