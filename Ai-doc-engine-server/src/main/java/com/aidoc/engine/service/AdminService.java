package com.aidoc.engine.service;

import com.aidoc.engine.model.vo.admin.AdminUserVO;
import com.aidoc.engine.model.vo.admin.DashboardTrendVO;
import com.aidoc.engine.model.vo.admin.DashboardVO;
import com.aidoc.engine.model.vo.admin.LoginLogVO;
import com.aidoc.engine.model.vo.admin.PageResult;

public interface AdminService {
    
    DashboardVO getDashboard();
    
    DashboardTrendVO getTrend(String range);
    
    PageResult<AdminUserVO> getUsers(int page, int size, String keyword);
    
    AdminUserVO getUserById(Long id);
    
    AdminUserVO updateUserStatus(Long id, Integer status);
    
    AdminUserVO updateUserRole(Long id, String role);
    
    void deleteUser(Long id);
    
    PageResult<LoginLogVO> getLoginLogs(int page, int size, String keyword);
    
    boolean isAdmin(Long userId);
}
