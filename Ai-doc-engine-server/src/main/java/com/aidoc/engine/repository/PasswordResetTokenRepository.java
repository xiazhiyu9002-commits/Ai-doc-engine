package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.PasswordResetTokenEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 密码重置令牌数据访问层
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    
    /**
     * 根据 token 查找
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);
    
    /**
     * 根据 token 和未使用状态查找
     */
    Optional<PasswordResetTokenEntity> findByTokenAndUsed(String token, Boolean used);
    
    /**
     * 删除过期的 token
     */
    void deleteByExpireAtBefore(LocalDateTime now);
    
    /**
     * 按用户ID查询
     */
    List<PasswordResetTokenEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 按用户ID分页查询
     */
    Page<PasswordResetTokenEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 按邮箱查询
     */
    List<PasswordResetTokenEntity> findByEmailOrderByCreatedAtDesc(String email);
    
    /**
     * 按使用状态查询
     */
    Page<PasswordResetTokenEntity> findByUsedOrderByCreatedAtDesc(Boolean used, Pageable pageable);
    
    /**
     * 统计指定时间之后的记录数
     */
    @Query("SELECT COUNT(t) FROM PasswordResetTokenEntity t WHERE t.createdAt >= :startDate")
    Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 统计时间范围内的记录数
     */
    @Query("SELECT COUNT(t) FROM PasswordResetTokenEntity t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 按使用状态统计
     */
    Long countByUsed(Boolean used);
    
    /**
     * 综合查询
     */
    @Query(value = "SELECT * FROM sys_password_reset_token t WHERE " +
           "(CAST(:userId AS bigint) IS NULL OR t.user_id = :userId) AND " +
           "(CAST(:email AS varchar) IS NULL OR t.email LIKE '%' || :email || '%') AND " +
           "(CAST(:used AS boolean) IS NULL OR t.used = :used) AND " +
           "(CAST(:startTime AS timestamp) IS NULL OR t.created_at >= :startTime) AND " +
           "(CAST(:endTime AS timestamp) IS NULL OR t.created_at < :endTime) " +
           "ORDER BY t.created_at DESC", nativeQuery = true)
    Page<PasswordResetTokenEntity> findByConditions(@Param("userId") Long userId,
                                                      @Param("email") String email,
                                                      @Param("used") Boolean used,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      Pageable pageable);
    
    /**
     * 查询过期的令牌
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.expireAt < :now AND t.used = false ORDER BY t.createdAt DESC")
    List<PasswordResetTokenEntity> findExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * 统计过期且未使用的令牌数
     */
    @Query("SELECT COUNT(t) FROM PasswordResetTokenEntity t WHERE t.expireAt < :now AND t.used = false")
    Long countExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * 查询有效令牌
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.expireAt > :now AND t.used = false ORDER BY t.createdAt DESC")
    List<PasswordResetTokenEntity> findValidTokens(@Param("now") LocalDateTime now);
    
    /**
     * 按用户ID和邮箱查询有效令牌
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.userId = :userId AND t.email = :email AND t.expireAt > :now AND t.used = false")
    Optional<PasswordResetTokenEntity> findValidTokenByUserAndEmail(@Param("userId") Long userId, 
                                                                      @Param("email") String email, 
                                                                      @Param("now") LocalDateTime now);
    
    /**
     * 删除用户的所有令牌
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除用户指定邮箱的所有令牌
     */
    void deleteByUserIdAndEmail(Long userId, String email);
}
