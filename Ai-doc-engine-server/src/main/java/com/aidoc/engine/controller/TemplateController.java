package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.template.TemplateCreateRequest;
import com.aidoc.engine.model.dto.template.TemplateQueryRequest;
import com.aidoc.engine.model.dto.template.TemplateUpdateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.template.TemplateListResponse;
import com.aidoc.engine.model.vo.template.TemplateVersionVO;
import com.aidoc.engine.model.vo.template.TemplateVO;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AdminService;
import com.aidoc.engine.service.TemplateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class TemplateController {
    
    private final TemplateService templateService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminService adminService;
    
    /**
     * 获取模板列表
     */
    @GetMapping("/list")
    public ApiResponse<TemplateListResponse> getTemplateList() {
        TemplateListResponse response = templateService.getTemplateList();
        return ApiResponse.success(response);
    }
    
    /**
     * 分页查询模板
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<TemplateVO>> getTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDefault,
            @RequestParam(required = false) Boolean isPublic) {
        
        TemplateQueryRequest request = TemplateQueryRequest.builder()
                .page(page)
                .size(size)
                .userId(userId)
                .templateType(templateType)
                .keyword(keyword)
                .isDefault(isDefault)
                .isPublic(isPublic)
                .build();
        
        PageResult<TemplateVO> result = templateService.getTemplates(request);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TemplateVO> getTemplate(@PathVariable Long id) {
        log.info("获取模板详情: id={}", id);
        
        TemplateVO template = templateService.getTemplateById(id);
        return ApiResponse.success(template);
    }
    
    /**
     * 创建模板
     */
    @PostMapping
    public ApiResponse<TemplateVO> createTemplate(@RequestBody TemplateCreateRequest request, HttpServletRequest httpRequest) {
        log.info("创建模板: name={}", request.getName());
        
        Long userId = getCurrentUserId(httpRequest);
        TemplateVO template = templateService.createTemplate(userId, request);
        return ApiResponse.success(template);
    }
    
    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public ApiResponse<TemplateVO> updateTemplate(@PathVariable Long id, @RequestBody TemplateUpdateRequest request, HttpServletRequest httpRequest) {
        log.info("更新模板: id={}", id);
        
        Long userId = getCurrentUserId(httpRequest);
        TemplateVO template = templateService.updateTemplate(id, userId, request);
        return ApiResponse.success(template);
    }
    
    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id, HttpServletRequest httpRequest) {
        log.info("删除模板: id={}", id);
        
        Long userId = getCurrentUserId(httpRequest);
        templateService.deleteTemplate(id, userId);
        return ApiResponse.success(null);
    }
    
    /**
     * 复制模板
     */
    @PostMapping("/{id}/copy")
    public ApiResponse<TemplateVO> copyTemplate(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest httpRequest) {
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板名称不能为空");
        }
        
        Long userId = getCurrentUserId(httpRequest);
        TemplateVO template = templateService.copyTemplate(id, userId, newName);
        return ApiResponse.success(template);
    }
    
    /**
     * 设置默认模板
     */
    @PostMapping("/{id}/default")
    public ApiResponse<Void> setDefaultTemplate(@PathVariable Long id, HttpServletRequest httpRequest) {
        log.info("设置默认模板: id={}", id);
        
        Long userId = getCurrentUserId(httpRequest);
        templateService.setDefaultTemplate(id, userId);
        return ApiResponse.success(null);
    }
    
    /**
     * 获取模板版本列表
     */
    @GetMapping("/{id}/versions")
    public ApiResponse<List<TemplateVersionVO>> getTemplateVersions(@PathVariable Long id) {
        log.info("获取模板版本列表: id={}", id);
        
        List<TemplateVersionVO> versions = templateService.getTemplateVersions(id);
        return ApiResponse.success(versions);
    }
    
    /**
     * 获取指定版本的模板
     */
    @GetMapping("/{id}/versions/{version}")
    public ApiResponse<TemplateVersionVO> getTemplateVersion(@PathVariable Long id, @PathVariable Integer version) {
        log.info("获取模板版本: id={}, version={}", id, version);
        
        TemplateVersionVO versionVO = templateService.getTemplateVersion(id, version);
        return ApiResponse.success(versionVO);
    }
    
    /**
     * 回滚到指定版本
     */
    @PostMapping("/{id}/versions/{version}/rollback")
    public ApiResponse<TemplateVO> rollbackToVersion(@PathVariable Long id, @PathVariable Integer version, HttpServletRequest httpRequest) {
        log.info("回滚模板版本: id={}, version={}", id, version);
        
        Long userId = getCurrentUserId(httpRequest);
        TemplateVO template = templateService.rollbackToVersion(id, version, userId);
        return ApiResponse.success(template);
    }
    
    /**
     * 获取当前用户的模板列表
     */
    @GetMapping("/my")
    public ApiResponse<List<TemplateVO>> getMyTemplates(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<TemplateVO> templates = templateService.getUserTemplates(userId);
        return ApiResponse.success(templates);
    }
    
    /**
     * 获取公开模板列表
     */
    @GetMapping("/public")
    public ApiResponse<List<TemplateVO>> getPublicTemplates() {
        List<TemplateVO> templates = templateService.getPublicTemplates();
        return ApiResponse.success(templates);
    }
    
    /**
     * 增加模板使用次数
     */
    @PostMapping("/{id}/use")
    public ApiResponse<Void> incrementUseCount(@PathVariable Long id) {
        templateService.incrementUseCount(id);
        return ApiResponse.success(null);
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return jwtTokenProvider.getUserIdFromToken(token);
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
