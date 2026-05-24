package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.announcement.AnnouncementCreateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.announcement.AnnouncementVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AdminService;
import com.aidoc.engine.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @GetMapping("/api/announcement/list")
    public ApiResponse<List<AnnouncementVO>> getPublishedAnnouncements() {
        List<AnnouncementVO> result = announcementService.getPublishedAnnouncements();
        return ApiResponse.success(result);
    }
    
    @GetMapping("/api/announcement/{id}")
    public ApiResponse<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementVO result = announcementService.getAnnouncementById(id);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/api/admin/announcement")
    public ApiResponse<PageResult<AnnouncementVO>> getAnnouncementList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isPublished,
            HttpServletRequest request) {
        
        checkAdmin(request);
        PageResult<AnnouncementVO> result = announcementService.getAnnouncementList(page, size, type, isPublished);
        return ApiResponse.success(result);
    }
    
    @PostMapping("/api/admin/announcement")
    public ApiResponse<AnnouncementVO> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequest announcementRequest,
            HttpServletRequest request) {
        
        checkAdmin(request);
        Long userId = getCurrentUserId(request);
        AnnouncementVO result = announcementService.createAnnouncement(announcementRequest, userId);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/api/admin/announcement/{id}")
    public ApiResponse<AnnouncementVO> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementCreateRequest announcementRequest,
            HttpServletRequest request) {
        
        checkAdmin(request);
        AnnouncementVO result = announcementService.updateAnnouncement(id, announcementRequest);
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/api/admin/announcement/{id}")
    public ApiResponse<Void> deleteAnnouncement(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        checkAdmin(request);
        announcementService.deleteAnnouncement(id);
        return ApiResponse.success(null);
    }
    
    @PutMapping("/api/admin/announcement/{id}/publish")
    public ApiResponse<AnnouncementVO> publishAnnouncement(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        checkAdmin(request);
        AnnouncementVO result = announcementService.publishAnnouncement(id);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/api/admin/announcement/{id}/unpublish")
    public ApiResponse<AnnouncementVO> unpublishAnnouncement(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        checkAdmin(request);
        AnnouncementVO result = announcementService.unpublishAnnouncement(id);
        return ApiResponse.success(result);
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
}
