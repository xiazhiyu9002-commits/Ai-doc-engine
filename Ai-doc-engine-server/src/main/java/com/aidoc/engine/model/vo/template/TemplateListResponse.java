package com.aidoc.engine.model.vo.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateListResponse {
    
    /**
     * 模板列表
     */
    private List<TemplateVO> templates;
}
