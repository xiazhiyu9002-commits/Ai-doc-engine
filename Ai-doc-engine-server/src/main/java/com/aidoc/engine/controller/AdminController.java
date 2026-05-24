package com.aidoc.engine.controller;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.vo.admin.AdminUserVO;
import com.aidoc.engine.model.vo.admin.DashboardTrendVO;
import com.aidoc.engine.model.vo.admin.DashboardVO;
import com.aidoc.engine.model.vo.admin.LoginLogVO;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> getDashboard(HttpServletRequest request) {
        checkAdmin(request);
        return ApiResponse.success(adminService.getDashboard());
    }

    @GetMapping("/dashboard/trend")
    public ApiResponse<DashboardTrendVO> getDashboardTrend(
            @RequestParam(defaultValue = "week") String range,
            HttpServletRequest request) {
        checkAdmin(request);
        return ApiResponse.success(adminService.getTrend(range));
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<AdminUserVO>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        checkAdmin(request);
        return ApiResponse.success(adminService.getUsers(page, size, keyword));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserVO> getUserById(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return ApiResponse.success(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<AdminUserVO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> body,
            HttpServletRequest request) {
        checkAdmin(request);
        Integer status = body.get("status");
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值不能为空");
        }
        return ApiResponse.success(adminService.updateUserStatus(id, status));
    }

    @PutMapping("/users/{id}/role")
    public ApiResponse<AdminUserVO> updateUserRole(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            HttpServletRequest request) {
        checkAdmin(request);
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不能为空");
        }
        return ApiResponse.success(adminService.updateUserRole(id, role));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        adminService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/login-logs")
    public ApiResponse<PageResult<LoginLogVO>> getLoginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        checkAdmin(request);
        return ApiResponse.success(adminService.getLoginLogs(page, size, keyword));
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

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
