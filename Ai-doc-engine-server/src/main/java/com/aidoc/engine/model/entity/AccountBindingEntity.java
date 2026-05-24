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
 * 账户绑定实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_account_binding", 
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_binding_type_id", columnNames = {"binding_type", "binding_id"})
        },
        indexes = {
            @Index(name = "idx_binding_user", columnList = "user_id"),
            @Index(name = "idx_binding_type", columnList = "binding_type")
        })
public class AccountBindingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 绑定类型：SCHOOL_OAUTH-学校统一认证, WECHAT-微信, EMAIL-邮箱等
     */
    @Column(name = "binding_type", nullable = false, length = 32)
    private String bindingType;
    
    /**
     * 绑定标识（如学校账号、微信openid等）
     */
    @Column(name = "binding_id", nullable = false, length = 128)
    private String bindingId;
    
    /**
     * 绑定显示名称
     */
    @Column(name = "display_name", length = 128)
    private String displayName;
    
    /**
     * 最后使用时间
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @CreationTimestamp
    @Column(name = "bound_at", nullable = false, updatable = false)
    private LocalDateTime boundAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
