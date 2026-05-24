package com.aidoc.engine.model.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板创建请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateRequest {
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 模板类型：system, custom
     */
    @Builder.Default
    private String templateType = "custom";
    
    /**
     * 模板配置
     */
    private TemplateConfig config;
    
    /**
     * 是否默认模板
     */
    @Builder.Default
    private Boolean isDefault = false;
    
    /**
     * 是否公开
     */
    @Builder.Default
    private Boolean isPublic = false;
}
