package com.aidoc.engine.model.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板更新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUpdateRequest {
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 模板配置
     */
    private TemplateConfig config;
    
    /**
     * 是否默认模板
     */
    private Boolean isDefault;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
    
    /**
     * 变更说明（用于版本记录）
     */
    private String changeNote;
}
