package com.aidoc.engine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 模板版本实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doc_template_version")
public class TemplateVersionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板ID
     */
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version;
    
    /**
     * 版本名称
     */
    @Column(name = "version_name", length = 100)
    private String versionName;
    
    /**
     * 模板名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 模板描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 模板配置JSON
     */
    @Column(name = "config_json", nullable = false, columnDefinition = "JSON")
    private String configJson;
    
    /**
     * 变更说明
     */
    @Column(name = "change_note", length = 500)
    private String changeNote;
    
    /**
     * 创建人ID
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
