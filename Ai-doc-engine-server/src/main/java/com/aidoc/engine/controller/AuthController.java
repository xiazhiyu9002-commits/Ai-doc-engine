package com.aidoc.engine.controller;

import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.auth.ForgotPasswordRequest;
import com.aidoc.engine.model.dto.auth.LoginRequest;
import com.aidoc.engine.model.dto.auth.RegisterRequest;
import com.aidoc.engine.model.dto.auth.ResetPasswordRequest;
import com.aidoc.engine.model.vo.auth.AuthResponse;
import com.aidoc.engine.model.vo.auth.UserVO;
import com.aidoc.engine.service.AuthService;
import com.aidoc.engine.service.OAuthLoginTicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final OAuthLoginTicketService oauthLoginTicketService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: username={}", request.getUsername());
        
        AuthResponse response = authService.register(request);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        log.info("收到登录请求: username={}, ip={}", request.getUsername(), ipAddress);
        
        AuthResponse response = authService.login(request, ipAddress, userAgent);
        
        return ApiResponse.success(response);
    }

    /**
     * 使用 OAuth2 回调签发的一次性票据换取系统 JWT
     */
    @GetMapping("/oauth/exchange")
    public ApiResponse<AuthResponse> exchangeOAuthTicket(@RequestParam("ticket") String ticket) {
        AuthResponse response = oauthLoginTicketService.consume(ticket);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser() {
        log.info("获取当前用户信息");
        
        UserVO user = authService.getCurrentUser();
        
        return ApiResponse.success(user);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        log.info("用户退出登录");
        
        authService.logout();
        
        return ApiResponse.success(null);
    }
    
    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("收到忘记密码请求: email={}", request.getEmail());
        
        authService.forgotPassword(request.getEmail());
        
        return ApiResponse.success(null);
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("收到重置密码请求: token={}", request.getToken());
        
        authService.resetPassword(request.getToken(), request.getNewPassword());
        
        return ApiResponse.success(null);
    }
}
