package com.aidoc.engine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户通知设置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_notification_settings",
        indexes = {
            @Index(name = "idx_notification_user", columnList = "user_id")
        })
public class NotificationSettingsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 系统通知开关
     */
    @Column(name = "system_notification", nullable = false)
    @Builder.Default
    private Boolean systemNotification = true;
    
    /**
     * 邮件通知开关
     */
    @Column(name = "email_notification", nullable = false)
    @Builder.Default
    private Boolean emailNotification = true;
    
    /**
     * 导出完成通知开关
     */
    @Column(name = "export_complete_notification", nullable = false)
    @Builder.Default
    private Boolean exportCompleteNotification = true;
    
    /**
     * 公告通知开关
     */
    @Column(name = "announcement_notification", nullable = false)
    @Builder.Default
    private Boolean announcementNotification = true;
    
    /**
     * 反馈回复通知开关
     */
    @Column(name = "feedback_reply_notification", nullable = false)
    @Builder.Default
    private Boolean feedbackReplyNotification = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
