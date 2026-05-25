package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE " +
           "(u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    Page<UserEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(String status);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt >= :startDate")
    Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.ocrCount = u.ocrCount + 1 WHERE u.id = :userId")
    void incrementOcrCount(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE UserEntity u SET u.exportCount = u.exportCount + 1 WHERE u.id = :userId")
    void incrementExportCount(@Param("userId") Long userId);
}
