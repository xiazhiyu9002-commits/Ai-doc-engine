package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.TemplateVersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模板版本数据访问层
 */
@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersionEntity, Long> {
    
    /**
     * 按模板ID查询所有版本（按版本号降序）
     */
    List<TemplateVersionEntity> findByTemplateIdOrderByVersionDesc(Long templateId);
    
    /**
     * 按模板ID分页查询版本
     */
    Page<TemplateVersionEntity> findByTemplateIdOrderByVersionDesc(Long templateId, Pageable pageable);
    
    /**
     * 查询模板的最新版本号
     */
    @Query("SELECT MAX(v.version) FROM TemplateVersionEntity v WHERE v.templateId = :templateId")
    Integer findMaxVersionByTemplateId(@Param("templateId") Long templateId);
    
    /**
     * 按模板ID和版本号查询
     */
    Optional<TemplateVersionEntity> findByTemplateIdAndVersion(Long templateId, Integer version);
    
    /**
     * 查询模板的最新版本
     */
    @Query("SELECT v FROM TemplateVersionEntity v WHERE v.templateId = :templateId ORDER BY v.version DESC LIMIT 1")
    Optional<TemplateVersionEntity> findLatestVersion(@Param("templateId") Long templateId);
    
    /**
     * 统计模板的版本数量
     */
    Long countByTemplateId(Long templateId);
    
    /**
     * 删除模板的所有版本
     */
    void deleteByTemplateId(Long templateId);
}
