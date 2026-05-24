package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.ExportTaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导出任务数据访问层
 */
@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTaskEntity, Long> {
    
    /**
     * 按任务状态统计数量
     */
    Long countByTaskStatus(String taskStatus);
    
    /**
     * 统计指定时间之后的记录数
     */
    @Query("SELECT COUNT(e) FROM ExportTaskEntity e WHERE e.startTime >= :startDate")
    Long countByStartTimeAfter(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 统计时间范围内的记录数
     */
    @Query("SELECT COUNT(e) FROM ExportTaskEntity e WHERE e.startTime >= :startDate AND e.startTime < :endDate")
    Long countByStartTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 按用户ID查询（分页）
     */
    Page<ExportTaskEntity> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 按任务状态查询（分页）
     */
    Page<ExportTaskEntity> findByTaskStatusOrderByStartTimeDesc(String taskStatus, Pageable pageable);
    
    /**
     * 按用户ID和任务状态查询（分页）
     */
    Page<ExportTaskEntity> findByUserIdAndTaskStatusOrderByStartTimeDesc(Long userId, String taskStatus, Pageable pageable);
    
    /**
     * 按模板ID查询（分页）
     */
    Page<ExportTaskEntity> findByTemplateIdOrderByStartTimeDesc(Long templateId, Pageable pageable);
    
    /**
     * 综合查询（支持用户ID、模板ID、状态、时间范围）
     */
    @Query(value = "SELECT * FROM doc_export_task e WHERE " +
           "(CAST(:userId AS bigint) IS NULL OR e.user_id = :userId) AND " +
           "(CAST(:templateId AS bigint) IS NULL OR e.template_id = :templateId) AND " +
           "(CAST(:taskStatus AS varchar) IS NULL OR e.task_status = :taskStatus) AND " +
           "(CAST(:startTime AS timestamp) IS NULL OR e.start_time >= :startTime) AND " +
           "(CAST(:endTime AS timestamp) IS NULL OR e.start_time < :endTime) " +
           "ORDER BY e.start_time DESC", nativeQuery = true)
    Page<ExportTaskEntity> findByConditions(@Param("userId") Long userId,
                                              @Param("templateId") Long templateId,
                                              @Param("taskStatus") String taskStatus,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              Pageable pageable);
    
    /**
     * 计算平均处理时间
     */
    @Query("SELECT AVG(e.processingTimeMs) FROM ExportTaskEntity e WHERE e.processingTimeMs IS NOT NULL")
    Double calculateAvgProcessingTime();
    
    /**
     * 计算总文件大小
     */
    @Query("SELECT SUM(e.fileSize) FROM ExportTaskEntity e WHERE e.fileSize IS NOT NULL")
    Long calculateTotalFileSize();
    
    /**
     * 计算总字符数
     */
    @Query("SELECT SUM(e.charCount) FROM ExportTaskEntity e WHERE e.charCount IS NOT NULL")
    Long calculateTotalCharCount();
    
    /**
     * 按用户ID统计数量
     */
    Long countByUserId(Long userId);
    
    /**
     * 按模板ID统计数量
     */
    Long countByTemplateId(Long templateId);
    
    /**
     * 获取用户最近的导出任务
     */
    List<ExportTaskEntity> findTop10ByUserIdOrderByStartTimeDesc(Long userId);
    
    /**
     * 按时间范围和状态统计
     */
    @Query("SELECT COUNT(e) FROM ExportTaskEntity e WHERE e.taskStatus = :taskStatus AND e.startTime >= :startTime AND e.startTime < :endTime")
    Long countByTaskStatusAndTimeRange(@Param("taskStatus") String taskStatus,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询所有待处理的任务
     */
    List<ExportTaskEntity> findByTaskStatusOrderByStartTimeAsc(String taskStatus);
    
    /**
     * 查询超时的任务
     */
    @Query("SELECT e FROM ExportTaskEntity e WHERE e.taskStatus IN ('PENDING', 'PROCESSING') AND e.startTime < :timeoutThreshold")
    List<ExportTaskEntity> findTimeoutTasks(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);
}
