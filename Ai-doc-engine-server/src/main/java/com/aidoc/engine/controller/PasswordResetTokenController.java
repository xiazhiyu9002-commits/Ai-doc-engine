package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.password.PasswordResetTokenQueryRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.password.PasswordResetStatsVO;
import com.aidoc.engine.model.vo.password.PasswordResetTokenVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AdminService;
import com.aidoc.engine.service.PasswordResetTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 密码重置令牌管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/password")
@RequiredArgsConstructor
public class PasswordResetTokenController {
    
    private final PasswordResetTokenService passwordResetTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminService adminService;
    
    /**
     * 分页查询密码重置令牌
     */
    @GetMapping("/tokens")
    public ApiResponse<PageResult<PasswordResetTokenVO>> getPasswordResetTokens(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean used,
            @RequestParam(required = false) Boolean expired,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletRequest request) {
        checkAdmin(request);
        
        PasswordResetTokenQueryRequest queryRequest = PasswordResetTokenQueryRequest.builder()
                .page(page)
                .size(size)
                .userId(userId)
                .email(email)
                .used(used)
                .expired(expired)
                .keyword(keyword)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        
        PageResult<PasswordResetTokenVO> result = passwordResetTokenService.getPasswordResetTokens(queryRequest);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取密码重置令牌详情
     */
    @GetMapping("/tokens/{id}")
    public ApiResponse<PasswordResetTokenVO> getPasswordResetToken(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        PasswordResetTokenVO token = passwordResetTokenService.getPasswordResetTokenById(id);
        
        return ApiResponse.success(token);
    }
    
    /**
     * 获取密码重置统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<PasswordResetStatsVO> getPasswordResetStats(HttpServletRequest request) {
        checkAdmin(request);
        
        PasswordResetStatsVO stats = passwordResetTokenService.getPasswordResetStats();
        
        return ApiResponse.success(stats);
    }
    
    /**
     * 获取指定用户的密码重置令牌
     */
    @GetMapping("/users/{userId}/tokens")
    public ApiResponse<PageResult<PasswordResetTokenVO>> getUserPasswordResetTokens(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        checkAdmin(request);
        
        PageResult<PasswordResetTokenVO> result = passwordResetTokenService.getPasswordResetTokensByUserId(userId, page, size);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 删除密码重置令牌
     */
    @DeleteMapping("/tokens/{id}")
    public ApiResponse<Void> deletePasswordResetToken(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        passwordResetTokenService.deletePasswordResetToken(id);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 批量删除密码重置令牌
     */
    @DeleteMapping("/tokens")
    public ApiResponse<Void> deletePasswordResetTokens(@RequestBody Map<String, List<Long>> body, HttpServletRequest request) {
        checkAdmin(request);
        
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要删除的记录");
        }
        
        passwordResetTokenService.deletePasswordResetTokens(ids);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 清理过期令牌
     */
    @PostMapping("/tokens/cleanup")
    public ApiResponse<Void> cleanupExpiredTokens(HttpServletRequest request) {
        checkAdmin(request);
        
        passwordResetTokenService.cleanupExpiredTokens();
        
        return ApiResponse.success(null);
    }
    
    /**
     * 使令牌失效
     */
    @PostMapping("/tokens/{id}/invalidate")
    public ApiResponse<Void> invalidateToken(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        passwordResetTokenService.invalidateToken(id);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 检查管理员权限
     */
    private void checkAdmin(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        if (!adminService.isAdmin(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无管理员权限");
        }
    }
    
    /**
     * 提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
