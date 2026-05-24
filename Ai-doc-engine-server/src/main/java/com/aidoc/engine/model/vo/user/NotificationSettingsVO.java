package com.aidoc.engine.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知设置VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsVO {
    
    private Long id;
    
    /**
     * 系统通知开关
     */
    private Boolean systemNotification;
    
    /**
     * 邮件通知开关
     */
    private Boolean emailNotification;
    
    /**
     * 导出完成通知开关
     */
    private Boolean exportCompleteNotification;
    
    /**
     * 公告通知开关
     */
    private Boolean announcementNotification;
    
    /**
     * 反馈回复通知开关
     */
    private Boolean feedbackReplyNotification;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
