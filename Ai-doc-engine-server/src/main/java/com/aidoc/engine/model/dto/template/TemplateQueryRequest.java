package com.aidoc.engine.model.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateQueryRequest {
    
    /**
     * 页码（从1开始）
     */
    @Builder.Default
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 10;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 模板类型：system, custom
     */
    private String templateType;
    
    /**
     * 关键词（名称/描述）
     */
    private String keyword;
    
    /**
     * 是否默认模板
     */
    private Boolean isDefault;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
}
