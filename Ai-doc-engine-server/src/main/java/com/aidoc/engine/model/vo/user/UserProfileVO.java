package com.aidoc.engine.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户详细资料VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private String nickname;
    
    private String avatarUrl;
    
    private String department;
    
    private String role;
    
    private String status;
    
    private LocalDateTime lastLoginAt;
    
    private String lastLoginIp;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
