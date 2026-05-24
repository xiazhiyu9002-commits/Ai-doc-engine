package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.ocr.OcrRecordQueryRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.ocr.OcrRecordVO;
import com.aidoc.engine.model.vo.ocr.OcrStatsVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AdminService;
import com.aidoc.engine.service.OcrRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * OCR日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/ocr")
@RequiredArgsConstructor
public class OcrRecordController {
    
    private final OcrRecordService ocrRecordService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminService adminService;
    
    /**
     * 分页查询OCR记录
     */
    @GetMapping("/records")
    public ApiResponse<PageResult<OcrRecordVO>> getOcrRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletRequest request) {
        checkAdmin(request);
        
        OcrRecordQueryRequest queryRequest = OcrRecordQueryRequest.builder()
                .page(page)
                .size(size)
                .userId(userId)
                .status(status)
                .keyword(keyword)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        
        PageResult<OcrRecordVO> result = ocrRecordService.getOcrRecords(queryRequest);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取OCR记录详情
     */
    @GetMapping("/records/{id}")
    public ApiResponse<OcrRecordVO> getOcrRecord(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        OcrRecordVO record = ocrRecordService.getOcrRecordById(id);
        
        return ApiResponse.success(record);
    }
    
    /**
     * 获取OCR统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<OcrStatsVO> getOcrStats(HttpServletRequest request) {
        checkAdmin(request);
        
        OcrStatsVO stats = ocrRecordService.getOcrStats();
        
        return ApiResponse.success(stats);
    }
    
    /**
     * 获取指定用户的OCR记录
     */
    @GetMapping("/users/{userId}/records")
    public ApiResponse<PageResult<OcrRecordVO>> getUserOcrRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        checkAdmin(request);
        
        PageResult<OcrRecordVO> result = ocrRecordService.getOcrRecordsByUserId(userId, page, size);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 删除OCR记录
     */
    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteOcrRecord(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        ocrRecordService.deleteOcrRecord(id);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 批量删除OCR记录
     */
    @DeleteMapping("/records")
    public ApiResponse<Void> deleteOcrRecords(@RequestBody Map<String, List<Long>> body, HttpServletRequest request) {
        checkAdmin(request);
        
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要删除的记录");
        }
        
        ocrRecordService.deleteOcrRecords(ids);
        
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
        if (!isAdmin(userId)) {
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
    
    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(Long userId) {
        return adminService.isAdmin(userId);
    }
}
