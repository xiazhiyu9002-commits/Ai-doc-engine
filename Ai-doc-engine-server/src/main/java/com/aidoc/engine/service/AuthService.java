package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.auth.LoginRequest;
import com.aidoc.engine.model.dto.auth.RegisterRequest;
import com.aidoc.engine.model.dto.auth.SchoolOAuthUserInfo;
import com.aidoc.engine.model.vo.auth.AuthResponse;
import com.aidoc.engine.model.vo.auth.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * 学校统一身份认证登录
     */
    AuthResponse loginBySchoolOAuth(SchoolOAuthUserInfo schoolUser, String ipAddress, String userAgent);
    
    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser();
    
    /**
     * 用户退出登录
     */
    void logout();
    
    /**
     * 忘记密码
     */
    void forgotPassword(String email);
    
    /**
     * 重置密码
     */
    void resetPassword(String token, String newPassword);
}
