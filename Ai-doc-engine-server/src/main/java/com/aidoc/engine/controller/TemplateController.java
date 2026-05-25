package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.template.TemplateCreateRequest;
import com.aidoc.engine.model.dto.template.TemplateQueryRequest;
import com.aidoc.engine.model.dto.template.TemplateUpdateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.template.TemplateListResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class TemplateController {
    
    private final TemplateService templateService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminService adminService;
    
    @GetMapping("/list")
    public ApiResponse<TemplateListResponse> getTemplateList() {
        TemplateListResponse response = templateService.getTemplateList();
        return ApiResponse.success(response);
    }
    
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
    
    @GetMapping("/{id}")
    public ApiResponse<TemplateVO> getTemplate(@PathVariable Long id) {
        log.info("获取模板详情: id={}", id);
        
        TemplateVO template = templateService.getTemplateById(id);
        return ApiResponse.success(template);
    }
    
    @PostMapping
    public ApiResponse<TemplateVO> createTemplate(@RequestBody TemplateCreateRequest request, HttpServletRequest httpRequest) {
        log.info("创建模板: name={}", request.getName());
        
        Long userId = getCurrentUserId(httpRequest);
        TemplateVO template = templateService.createTemplate(userId, request);
        return ApiResponse.success(template);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<TemplateVO> updateTemplate(@PathVariable Long id, @RequestBody TemplateUpdateRequest request, HttpServletRequest httpRequest) {
        log.info("更新模板: id={}", id);
        
        Long userId = getCurrentUserId(httpRequest);
        TemplateVO template = templateService.updateTemplate(id, userId, request);
        return ApiResponse.success(template);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id, HttpServletRequest httpRequest) {
        log.info("删除模板: id={}", id);
        
        Long userId = getCurrentUserId(httpRequest);
        templateService.deleteTemplate(id, userId);
        return ApiResponse.success(null);
    }
    
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
    
    @PostMapping("/{id}/default")
    public ApiResponse<Void> setDefaultTemplate(@PathVariable Long id, HttpServletRequest httpRequest) {
        log.info("设置默认模板: id={}", id);
        
        Long userId = getCurrentUserId(httpRequest);
        templateService.setDefaultTemplate(id, userId);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/my")
    public ApiResponse<List<TemplateVO>> getMyTemplates(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<TemplateVO> templates = templateService.getUserTemplates(userId);
        return ApiResponse.success(templates);
    }
    
    @GetMapping("/public")
    public ApiResponse<List<TemplateVO>> getPublicTemplates() {
        List<TemplateVO> templates = templateService.getPublicTemplates();
        return ApiResponse.success(templates);
    }
    
    @PostMapping("/{id}/use")
    public ApiResponse<Void> incrementUseCount(@PathVariable Long id) {
        templateService.incrementUseCount(id);
        return ApiResponse.success(null);
    }
    
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
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
}
