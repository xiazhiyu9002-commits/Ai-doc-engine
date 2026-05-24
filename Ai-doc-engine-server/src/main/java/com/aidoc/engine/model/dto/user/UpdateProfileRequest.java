package com.aidoc.engine.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户资料请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128位")
    private String email;
    
    @Size(max = 64, message = "昵称长度不能超过64位")
    private String nickname;
    
    @Size(max = 255, message = "头像URL长度不能超过255位")
    private String avatarUrl;
    
    @Size(max = 100, message = "部门长度不能超过100位")
    private String department;
}
