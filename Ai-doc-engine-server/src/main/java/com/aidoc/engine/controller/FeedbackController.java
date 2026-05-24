package com.aidoc.engine.controller;

import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.feedback.FeedbackSubmitRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.feedback.FeedbackDetailVO;
import com.aidoc.engine.model.vo.feedback.FeedbackVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/submit")
    public ApiResponse<FeedbackVO> submitFeedback(
            @Valid @RequestPart("request") FeedbackSubmitRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId(httpRequest);
        String ip = getClientIp(httpRequest);
        
        FeedbackVO result = feedbackService.submitFeedback(request, images, userId, ip);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/my")
    public ApiResponse<PageResult<FeedbackVO>> getMyFeedbacks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId(httpRequest);
        PageResult<FeedbackVO> result = feedbackService.getUserFeedbacks(userId, page, size);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<FeedbackDetailVO> getFeedbackDetail(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId(httpRequest);
        FeedbackDetailVO result = feedbackService.getFeedbackDetail(id, userId, false);
        return ApiResponse.success(result);
    }
    
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("未登录");
        }
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
