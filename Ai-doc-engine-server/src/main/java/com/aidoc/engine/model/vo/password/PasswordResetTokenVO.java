package com.aidoc.engine.model.vo.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密码重置令牌视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 重置令牌（脱敏）
     */
    private String token;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireAt;
    
    /**
     * 是否已使用
     */
    private Boolean used;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否过期
     */
    private Boolean expired;
}
