package com.aidoc.engine.model.vo.template;

import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    @JsonProperty("type")
    private String templateType;
    
    private TemplateConfig config;
    
    private Long creatorId;
    
    private String creatorName;
    
    @JsonProperty("usageCount")
    private Integer useCount;
    
    private Boolean isDefault;
    
    private Boolean isPublic;
    
    private Integer status;
    
    private String content;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
