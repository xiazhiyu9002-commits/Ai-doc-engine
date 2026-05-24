package com.aidoc.engine.model.vo.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码重置统计信息视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetStatsVO {
    
    /**
     * 总令牌数
     */
    private Long totalCount;
    
    /**
     * 已使用令牌数
     */
    private Long usedCount;
    
    /**
     * 未使用令牌数
     */
    private Long unusedCount;
    
    /**
     * 已过期令牌数
     */
    private Long expiredCount;
    
    /**
     * 使用率（百分比）
     */
    private Double usageRate;
    
    /**
     * 今日生成数
     */
    private Long todayCount;
    
    /**
     * 本周生成数
     */
    private Long weekCount;
}
