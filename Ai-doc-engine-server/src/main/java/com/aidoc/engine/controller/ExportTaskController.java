package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.export.ExportTaskQueryRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.export.ExportTaskStatsVO;
import com.aidoc.engine.model.vo.export.ExportTaskVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.ExportTaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 导出任务日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportTaskController {
    
    private final ExportTaskService exportTaskService;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.aidoc.engine.service.AdminService adminService;
    
    /**
     * 分页查询导出任务
     */
    @GetMapping("/tasks")
    public ApiResponse<PageResult<ExportTaskVO>> getExportTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) String taskStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletRequest request) {
        checkAdmin(request);
        
        ExportTaskQueryRequest queryRequest = ExportTaskQueryRequest.builder()
                .page(page)
                .size(size)
                .userId(userId)
                .templateId(templateId)
                .taskStatus(taskStatus)
                .keyword(keyword)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        
        PageResult<ExportTaskVO> result = exportTaskService.getExportTasks(queryRequest);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取导出任务详情
     */
    @GetMapping("/tasks/{id}")
    public ApiResponse<ExportTaskVO> getExportTask(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        ExportTaskVO task = exportTaskService.getExportTaskById(id);
        
        return ApiResponse.success(task);
    }
    
    /**
     * 获取导出任务统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<ExportTaskStatsVO> getExportTaskStats(HttpServletRequest request) {
        checkAdmin(request);
        
        ExportTaskStatsVO stats = exportTaskService.getExportTaskStats();
        
        return ApiResponse.success(stats);
    }
    
    /**
     * 获取指定用户的导出任务
     */
    @GetMapping("/users/{userId}/tasks")
    public ApiResponse<PageResult<ExportTaskVO>> getUserExportTasks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        checkAdmin(request);
        
        PageResult<ExportTaskVO> result = exportTaskService.getExportTasksByUserId(userId, page, size);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取指定模板的导出任务
     */
    @GetMapping("/templates/{templateId}/tasks")
    public ApiResponse<PageResult<ExportTaskVO>> getTemplateExportTasks(
            @PathVariable Long templateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        checkAdmin(request);
        
        PageResult<ExportTaskVO> result = exportTaskService.getExportTasksByTemplateId(templateId, page, size);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 删除导出任务
     */
    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> deleteExportTask(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        exportTaskService.deleteExportTask(id);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 批量删除导出任务
     */
    @DeleteMapping("/tasks")
    public ApiResponse<Void> deleteExportTasks(@RequestBody Map<String, List<Long>> body, HttpServletRequest request) {
        checkAdmin(request);
        
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要删除的记录");
        }
        
        exportTaskService.deleteExportTasks(ids);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 重试失败的任务
     */
    @PostMapping("/tasks/{id}/retry")
    public ApiResponse<Void> retryFailedTask(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        
        exportTaskService.retryFailedTask(id);
        
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
