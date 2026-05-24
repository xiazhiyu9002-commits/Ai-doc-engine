package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.FormulaOcrRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OCR识别记录数据访问层
 */
@Repository
public interface FormulaOcrRecordRepository extends JpaRepository<FormulaOcrRecordEntity, Long> {
    
    /**
     * 按状态统计数量
     */
    Long countByStatus(String status);
    
    /**
     * 统计指定时间之后的记录数
     */
    @Query("SELECT COUNT(f) FROM FormulaOcrRecordEntity f WHERE f.createdAt >= :startDate")
    Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 统计时间范围内的记录数
     */
    @Query("SELECT COUNT(f) FROM FormulaOcrRecordEntity f WHERE f.createdAt >= :startDate AND f.createdAt < :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 按用户ID查询（分页）
     */
    Page<FormulaOcrRecordEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 按状态查询（分页）
     */
    Page<FormulaOcrRecordEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * 按用户ID和状态查询（分页）
     */
    Page<FormulaOcrRecordEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);
    
    /**
     * 按创建时间范围查询（分页）
     */
    @Query("SELECT f FROM FormulaOcrRecordEntity f WHERE f.createdAt >= :startTime AND f.createdAt < :endTime ORDER BY f.createdAt DESC")
    Page<FormulaOcrRecordEntity> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime, 
                                                   Pageable pageable);
    
    /**
     * 综合查询（支持用户ID、状态、时间范围）
     */
    @Query(value = "SELECT * FROM formula_ocr_record f WHERE " +
           "(CAST(:userId AS bigint) IS NULL OR f.user_id = :userId) AND " +
           "(CAST(:status AS varchar) IS NULL OR f.status = :status) AND " +
           "(CAST(:startTime AS timestamp) IS NULL OR f.created_at >= :startTime) AND " +
           "(CAST(:endTime AS timestamp) IS NULL OR f.created_at < :endTime) " +
           "ORDER BY f.created_at DESC", nativeQuery = true)
    Page<FormulaOcrRecordEntity> findByConditions(@Param("userId") Long userId,
                                                    @Param("status") String status,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    Pageable pageable);
    
    /**
     * 计算平均处理时间
     */
    @Query("SELECT AVG(f.processingTimeMs) FROM FormulaOcrRecordEntity f WHERE f.processingTimeMs IS NOT NULL")
    Double calculateAvgProcessingTime();
    
    /**
     * 按用户ID统计数量
     */
    Long countByUserId(Long userId);
    
    /**
     * 按用户ID和时间范围统计
     */
    @Query("SELECT COUNT(f) FROM FormulaOcrRecordEntity f WHERE f.userId = :userId AND f.createdAt >= :startTime AND f.createdAt < :endTime")
    Long countByUserIdAndTimeRange(@Param("userId") Long userId, 
                                    @Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取用户最近的OCR记录
     */
    List<FormulaOcrRecordEntity> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 按时间范围和状态统计
     */
    @Query("SELECT COUNT(f) FROM FormulaOcrRecordEntity f WHERE f.status = :status AND f.createdAt >= :startTime AND f.createdAt < :endTime")
    Long countByStatusAndTimeRange(@Param("status") String status,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
}
