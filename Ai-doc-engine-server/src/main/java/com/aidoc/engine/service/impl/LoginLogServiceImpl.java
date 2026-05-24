package com.aidoc.engine.service.impl;

import com.aidoc.engine.model.entity.LoginLogEntity;
import com.aidoc.engine.repository.LoginLogRepository;
import com.aidoc.engine.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {
    
    private final LoginLogRepository loginLogRepository;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;
    
    @Override
    @Transactional
    public void logLoginSuccess(Long userId, String username, String ipAddress, String userAgent) {
        logLoginSuccess(userId, username, ipAddress, userAgent, null);
    }

    @Override
    @Transactional
    public void logLoginSuccess(Long userId, String username, String ipAddress, String userAgent, String browserType) {
        LoginLogEntity loginLog = LoginLogEntity.builder()
                .userId(userId)
                .usernameOrEmail(username)
                .loginResult("success")
                .browserType(browserType != null ? browserType : detectBrowser(userAgent))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        
        loginLogRepository.save(loginLog);
        log.info("记录登录成功日志: userId={}, username={}, ip={}, browser={}", userId, username, ipAddress, browserType);
    }
    
    @Override
    @Transactional
    public void logLoginFailure(String usernameOrEmail, String ipAddress, String userAgent, String failReason) {
        logLoginFailure(usernameOrEmail, ipAddress, userAgent, failReason, null);
    }

    @Override
    @Transactional
    public void logLoginFailure(String usernameOrEmail, String ipAddress, String userAgent, String failReason, String browserType) {
        LoginLogEntity loginLog = LoginLogEntity.builder()
                .usernameOrEmail(usernameOrEmail)
                .loginResult("failed")
                .browserType(browserType != null ? browserType : detectBrowser(userAgent))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failReason(failReason)
                .build();
        
        loginLogRepository.save(loginLog);
        log.warn("记录登录失败日志: username={}, ip={}, browser={}, reason={}", usernameOrEmail, ipAddress, browserType, failReason);
    }
    
    @Override
    public boolean shouldLockAccount(String usernameOrEmail, String ipAddress) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(LOCK_DURATION_MINUTES);
        
        long failedAttempts = loginLogRepository.countFailedLoginsSince(usernameOrEmail, startTime);
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            log.warn("账号登录失败次数过多，需要锁定: username={}, attempts={}", usernameOrEmail, failedAttempts);
            return true;
        }
        
        long ipFailedAttempts = loginLogRepository.countFailedLoginsByIpSince(ipAddress, startTime);
        
        if (ipFailedAttempts >= MAX_FAILED_ATTEMPTS * 2) {
            log.warn("IP登录失败次数过多，需要锁定: ip={}, attempts={}", ipAddress, ipFailedAttempts);
            return true;
        }
        
        return false;
    }
    
    private String detectBrowser(String userAgent) {
        if (userAgent == null) {
            return "Other";
        }
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("edg/") || userAgent.contains("edge")) {
            return "Edge";
        } else if (userAgent.contains("chrome/") && !userAgent.contains("edg/")) {
            return "Chrome";
        } else if (userAgent.contains("firefox/") || userAgent.contains("fxios")) {
            return "Firefox";
        } else if (userAgent.contains("safari/") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("opera") || userAgent.contains("opr/")) {
            return "Opera";
        } else {
            return "Other";
        }
    }
}
