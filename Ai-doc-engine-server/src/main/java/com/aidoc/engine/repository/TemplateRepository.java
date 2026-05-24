package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.TemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模板数据访问层
 */
@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {
    
    /**
     * 查询所有模板（按创建时间降序）
     */
    List<TemplateEntity> findAllByOrderByCreatedAtDesc();
    
    /**
     * 按用户ID查询模板（按创建时间降序）
     */
    List<TemplateEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 按用户ID分页查询模板
     */
    Page<TemplateEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 按模板类型查询
     */
    List<TemplateEntity> findByTemplateTypeOrderByCreatedAtDesc(String templateType);
    
    /**
     * 查询公开模板
     */
    List<TemplateEntity> findByIsPublicTrueOrderByCreatedAtDesc();
    
    /**
     * 查询默认模板
     */
    Optional<TemplateEntity> findByIsDefaultTrue();
    
    /**
     * 按用户ID和模板类型查询
     */
    Page<TemplateEntity> findByUserIdAndTemplateTypeOrderByCreatedAtDesc(Long userId, String templateType, Pageable pageable);
    
    /**
     * 综合查询
     */
    @Query("SELECT t FROM TemplateEntity t WHERE " +
           "(:userId IS NULL OR t.userId = :userId) AND " +
           "(:templateType IS NULL OR t.templateType = :templateType) AND " +
           "(:isDefault IS NULL OR t.isDefault = :isDefault) AND " +
           "(:isPublic IS NULL OR t.isPublic = :isPublic) AND " +
           "(:keyword IS NULL OR t.name LIKE %:keyword% OR t.description LIKE %:keyword%) " +
           "ORDER BY t.createdAt DESC")
    Page<TemplateEntity> findByConditions(@Param("userId") Long userId,
                                           @Param("templateType") String templateType,
                                           @Param("isDefault") Boolean isDefault,
                                           @Param("isPublic") Boolean isPublic,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);
    
    /**
     * 按用户ID统计模板数量
     */
    Long countByUserId(Long userId);
    
    /**
     * 检查模板名称是否存在
     */
    boolean existsByNameAndUserId(String name, Long userId);
    
    /**
     * 增加使用次数
     */
    @Query("UPDATE TemplateEntity t SET t.useCount = t.useCount + 1 WHERE t.id = :id")
    void incrementUseCount(@Param("id") Long id);
}
