package com.aidoc.engine.model.vo.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出任务统计信息视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskStatsVO {
    
    /**
     * 总任务数
     */
    private Long totalCount;
    
    /**
     * 待处理任务数
     */
    private Long pendingCount;
    
    /**
     * 处理中任务数
     */
    private Long processingCount;
    
    /**
     * 成功任务数
     */
    private Long successCount;
    
    /**
     * 失败任务数
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
     * 总文件大小（字节）
     */
    private Long totalFileSize;
    
    /**
     * 总字符数
     */
    private Long totalCharCount;
    
    /**
     * 今日任务数
     */
    private Long todayCount;
    
    /**
     * 本周任务数
     */
    private Long weekCount;
}
