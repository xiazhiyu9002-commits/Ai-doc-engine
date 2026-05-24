package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.LoginLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLogEntity, Long> {
    
    @Query("SELECT COUNT(l) FROM LoginLogEntity l WHERE l.usernameOrEmail = :usernameOrEmail " +
           "AND l.loginResult = 'failed' AND l.createdAt >= :startTime")
    long countFailedLoginsSince(@Param("usernameOrEmail") String usernameOrEmail, 
                                @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(l) FROM LoginLogEntity l WHERE l.ipAddress = :ipAddress " +
           "AND l.loginResult = 'failed' AND l.createdAt >= :startTime")
    long countFailedLoginsByIpSince(@Param("ipAddress") String ipAddress, 
                                    @Param("startTime") LocalDateTime startTime);

    Page<LoginLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT l FROM LoginLogEntity l WHERE " +
           "l.usernameOrEmail LIKE %:keyword% OR l.ipAddress LIKE %:keyword%")
    Page<LoginLogEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByLoginResult(String loginResult);

    long countByCreatedAtAfter(LocalDateTime after);

    long countByLoginResultAndCreatedAtAfter(String loginResult, LocalDateTime after);
    
    @Query("SELECT COUNT(DISTINCT l.userId) FROM LoginLogEntity l WHERE l.createdAt >= :startDate AND l.loginResult = 'success'")
    Long countDistinctUserIdByCreatedAtAfterAndLoginResultSuccess(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(DISTINCT l.userId) FROM LoginLogEntity l WHERE l.createdAt >= :startDate AND l.createdAt < :endDate AND l.loginResult = 'success'")
    Long countDistinctUserIdByCreatedAtBetweenAndLoginResultSuccess(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(l) FROM LoginLogEntity l WHERE l.createdAt >= :startDate AND l.createdAt < :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
