package com.aidoc.engine.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新通知设置请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationSettingsRequest {
    
    /**
     * 系统通知开关
     */
    @NotNull(message = "系统通知开关不能为空")
    private Boolean systemNotification;
    
    /**
     * 邮件通知开关
     */
    @NotNull(message = "邮件通知开关不能为空")
    private Boolean emailNotification;
    
    /**
     * 导出完成通知开关
     */
    @NotNull(message = "导出完成通知开关不能为空")
    private Boolean exportCompleteNotification;
    
    /**
     * 公告通知开关
     */
    @NotNull(message = "公告通知开关不能为空")
    private Boolean announcementNotification;
    
    /**
     * 反馈回复通知开关
     */
    @NotNull(message = "反馈回复通知开关不能为空")
    private Boolean feedbackReplyNotification;
}
