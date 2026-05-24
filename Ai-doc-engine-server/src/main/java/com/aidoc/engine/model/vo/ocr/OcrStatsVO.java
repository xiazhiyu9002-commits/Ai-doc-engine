package com.aidoc.engine.model.vo.ocr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OCR统计信息视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrStatsVO {
    
    /**
     * 总记录数
     */
    private Long totalCount;
    
    /**
     * 成功记录数
     */
    private Long successCount;
    
    /**
     * 失败记录数
     */
    private Long failedCount;
    
    /**
     * 成功率（百分比）
     */
    private Double successRate;
    
    /**
     * 平均处理时间（毫秒）
     */
    private Double avgProcessingTime;
    
    /**
     * 今日记录数
     */
    private Long todayCount;
    
    /**
     * 本周记录数
     */
    private Long weekCount;
}
