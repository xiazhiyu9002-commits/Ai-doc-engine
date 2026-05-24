package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.FeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    
    Page<FeedbackEntity> findByUserId(Long userId, Pageable pageable);
    
    Page<FeedbackEntity> findByStatus(String status, Pageable pageable);
    
    Page<FeedbackEntity> findByFeedbackType(String feedbackType, Pageable pageable);
    
    List<FeedbackEntity> findByStatus(String status);
    
    Long countByStatus(String status);
    
    Long countByFeedbackType(String feedbackType);
    
    Long countByPriority(String priority);
    
    @Query("SELECT COUNT(f) FROM FeedbackEntity f WHERE f.createdAt >= :startDate")
    Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(f) FROM FeedbackEntity f WHERE f.priority IN ('high', 'urgent') AND f.status NOT IN ('resolved', 'closed')")
    Long countHighPriorityPending();
    
    @Query(value = "SELECT * FROM user_feedback f WHERE " +
           "(CAST(:status AS VARCHAR) IS NULL OR f.status = :status) AND " +
           "(CAST(:feedbackType AS VARCHAR) IS NULL OR f.feedback_type = :feedbackType) AND " +
           "(CAST(:priority AS VARCHAR) IS NULL OR f.priority = :priority) AND " +
           "(CAST(:userId AS BIGINT) IS NULL OR f.user_id = :userId) AND " +
           "(CAST(:keyword AS VARCHAR) IS NULL OR f.content LIKE CONCAT('%', :keyword, '%')) AND " +
           "(CAST(:startDate AS TIMESTAMP) IS NULL OR f.created_at >= :startDate) AND " +
           "(CAST(:endDate AS TIMESTAMP) IS NULL OR f.created_at <= :endDate)",
           nativeQuery = true)
    Page<FeedbackEntity> findByConditions(
            @Param("status") String status,
            @Param("feedbackType") String feedbackType,
            @Param("priority") String priority,
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
