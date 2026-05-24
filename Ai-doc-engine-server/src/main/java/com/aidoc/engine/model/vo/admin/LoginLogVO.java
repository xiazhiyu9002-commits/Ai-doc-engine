package com.aidoc.engine.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogVO {
    
    private Long id;
    private Long userId;
    private String usernameOrEmail;
    private String nickname;
    private String loginResult;
    private String browserType;
    private String ipAddress;
    private String userAgent;
    private String failReason;
    private LocalDateTime createdAt;
}
