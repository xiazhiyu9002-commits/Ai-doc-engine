package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.password.PasswordResetTokenQueryRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.password.PasswordResetStatsVO;
import com.aidoc.engine.model.vo.password.PasswordResetTokenVO;

import java.util.List;

/**
 * 密码重置令牌服务接口
 */
public interface PasswordResetTokenService {
    
    /**
     * 分页查询密码重置令牌
     */
    PageResult<PasswordResetTokenVO> getPasswordResetTokens(PasswordResetTokenQueryRequest request);
    
    /**
     * 根据ID获取密码重置令牌详情
     */
    PasswordResetTokenVO getPasswordResetTokenById(Long id);
    
    /**
     * 获取密码重置统计信息
     */
    PasswordResetStatsVO getPasswordResetStats();
    
    /**
     * 获取指定用户的密码重置令牌
     */
    PageResult<PasswordResetTokenVO> getPasswordResetTokensByUserId(Long userId, int page, int size);
    
    /**
     * 删除密码重置令牌
     */
    void deletePasswordResetToken(Long id);
    
    /**
     * 批量删除密码重置令牌
     */
    void deletePasswordResetTokens(List<Long> ids);
    
    /**
     * 清理过期令牌
     */
    void cleanupExpiredTokens();
    
    /**
     * 使令牌失效
     */
    void invalidateToken(Long id);
}
