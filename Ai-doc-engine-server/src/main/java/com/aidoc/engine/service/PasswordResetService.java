package com.aidoc.engine.service;

/**
 * 密码重置服务接口
 */
public interface PasswordResetService {
    
    /**
     * 请求密码重置
     * 
     * @param email 用户邮箱
     */
    void requestPasswordReset(String email);
    
    /**
     * 重置密码
     * 
     * @param token 重置令牌
     * @param newPassword 新密码
     */
    void resetPassword(String token, String newPassword);
    
    /**
     * 验证重置令牌
     * 
     * @param token 重置令牌
     * @return 是否有效
     */
    boolean validateResetToken(String token);
}
