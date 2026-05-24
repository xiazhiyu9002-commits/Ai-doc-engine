package com.aidoc.engine.service;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {
    
    /**
     * 记录登录成功
     */
    void logLoginSuccess(Long userId, String username, String ipAddress, String userAgent);

    /**
     * 记录指定登录方式的登录成功
     */
    void logLoginSuccess(Long userId, String username, String ipAddress, String userAgent, String loginType);
    
    /**
     * 记录登录失败
     */
    void logLoginFailure(String usernameOrEmail, String ipAddress, String userAgent, String failReason);

    /**
     * 记录指定登录方式的登录失败
     */
    void logLoginFailure(String usernameOrEmail, String ipAddress, String userAgent, String failReason, String loginType);
    
    /**
     * 检查是否需要锁定账号（登录失败次数过多）
     */
    boolean shouldLockAccount(String usernameOrEmail, String ipAddress);
}
