package com.aidoc.engine.model.vo.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板版本视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersionVO {
    
    /**
     * 版本ID
     */
    private Long id;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 版本名称
     */
    private String versionName;
    
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
    private com.aidoc.engine.model.dto.template.TemplateConfig config;
    
    /**
     * 变更说明
     */
    private String changeNote;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建人名称
     */
    private String createdByName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
