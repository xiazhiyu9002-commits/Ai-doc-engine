package com.aidoc.engine.model.dto.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出任务查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskQueryRequest {
    
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
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 任务状态：PENDING, PROCESSING, SUCCESS, FAILED
     */
    private String taskStatus;
    
    /**
     * 关键词（用户名/文件名）
     */
    private String keyword;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
