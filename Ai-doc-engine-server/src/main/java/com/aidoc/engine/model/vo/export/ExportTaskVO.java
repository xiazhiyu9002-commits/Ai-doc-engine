package com.aidoc.engine.model.vo.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出任务记录视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskVO {
    
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    private Long templateId;
    
    private String templateName;
    
    @JsonProperty("status")
    private String taskStatus;
    
    private String statusDesc;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileSizeFormatted;
    
    private Integer charCount;
    
    private String errorMessage;
    
    @JsonProperty("createdAt")
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @JsonProperty("duration")
    private Integer processingTimeMs;
    
    private String processingTimeFormatted;
}
