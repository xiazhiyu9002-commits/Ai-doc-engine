package com.aidoc.engine.model.dto.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密码重置令牌查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenQueryRequest {
    
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
     * 邮箱
     */
    private String email;
    
    /**
     * 是否已使用
     */
    private Boolean used;
    
    /**
     * 是否过期
     */
    private Boolean expired;
    
    /**
     * 关键词（用户名/邮箱）
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
