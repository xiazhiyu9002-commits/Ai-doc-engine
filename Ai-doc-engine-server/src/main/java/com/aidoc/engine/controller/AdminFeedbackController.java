package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.feedback.FeedbackQueryRequest;
import com.aidoc.engine.model.dto.feedback.FeedbackUpdateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.feedback.FeedbackDetailVO;
import com.aidoc.engine.model.vo.feedback.FeedbackStatsVO;
import com.aidoc.engine.model.vo.feedback.FeedbackVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AdminService;
import com.aidoc.engine.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {
    
    private final FeedbackService feedbackService;
    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @GetMapping
    public ApiResponse<PageResult<FeedbackVO>> getFeedbackList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        
        checkAdmin(request);
        
        FeedbackQueryRequest queryRequest = FeedbackQueryRequest.builder()
                .page(page)
                .size(size)
                .status(status)
                .feedbackType(feedbackType)
                .priority(priority)
                .keyword(keyword)
                .userId(userId)
                .build();
        
        PageResult<FeedbackVO> result = feedbackService.getFeedbackList(queryRequest);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/stats")
    public ApiResponse<FeedbackStatsVO> getFeedbackStats(HttpServletRequest request) {
        checkAdmin(request);
        FeedbackStatsVO result = feedbackService.getFeedbackStats();
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<FeedbackDetailVO> getFeedbackDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        checkAdmin(request);
        Long userId = getCurrentUserId(request);
        FeedbackDetailVO result = feedbackService.getFeedbackDetail(id, userId, true);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}/status")
    public ApiResponse<FeedbackVO> updateFeedbackStatus(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackUpdateRequest updateRequest,
            HttpServletRequest request) {
        
        checkAdmin(request);
        Long operatorId = getCurrentUserId(request);
        String ip = getClientIp(request);
        
        FeedbackVO result = feedbackService.updateFeedbackStatus(id, updateRequest, operatorId, ip);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}/reply")
    public ApiResponse<FeedbackVO> replyFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        
        checkAdmin(request);
        
        String reply = body.get("reply");
        if (reply == null || reply.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "回复内容不能为空");
        }
        
        Long operatorId = getCurrentUserId(request);
        String ip = getClientIp(request);
        
        FeedbackVO result = feedbackService.replyFeedback(id, reply, operatorId, ip);
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFeedback(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        checkAdmin(request);
        Long operatorId = getCurrentUserId(request);
        String ip = getClientIp(request);
        
        feedbackService.deleteFeedback(id, operatorId, ip);
        return ApiResponse.success(null);
    }
    
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
    
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtTokenProvider.getUserIdFromToken(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
