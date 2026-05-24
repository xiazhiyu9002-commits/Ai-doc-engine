package com.aidoc.engine.model.dto.ocr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OCR记录查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrRecordQueryRequest {
    
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
     * 状态：RECOGNIZED, FAILED
     */
    private String status;
    
    /**
     * 关键词（用户名/昵称）
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
