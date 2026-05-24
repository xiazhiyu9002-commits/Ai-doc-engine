package com.aidoc.engine.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long disabledUsers;
    private Long todayLogins;
    private Long totalLoginLogs;
    private Long failedLoginLogs;
    private Long totalTemplates;
    
    // 新增字段
    private Long totalExports;
    private Long ocrFailedCount;
    private Long totalFeedbacks;
    private Long totalAnnouncements;
    
    // 环比数据（与上一周期比较）
    private Double userGrowthRate;
    private Double exportGrowthRate;
    private Double ocrGrowthRate;
    private Double feedbackGrowthRate;
}
