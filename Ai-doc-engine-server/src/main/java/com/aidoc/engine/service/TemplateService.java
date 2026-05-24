package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.template.TemplateCreateRequest;
import com.aidoc.engine.model.dto.template.TemplateQueryRequest;
import com.aidoc.engine.model.dto.template.TemplateUpdateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.template.TemplateListResponse;
import com.aidoc.engine.model.vo.template.TemplateVersionVO;
import com.aidoc.engine.model.vo.template.TemplateVO;

import java.util.List;

/**
 * 模板服务接口
 */
public interface TemplateService {
    
    /**
     * 获取模板列表
     */
    TemplateListResponse getTemplateList();
    
    /**
     * 分页查询模板
     */
    PageResult<TemplateVO> getTemplates(TemplateQueryRequest request);
    
    /**
     * 根据 ID 获取模板
     */
    TemplateVO getTemplateById(Long id);
    
    /**
     * 创建模板
     */
    TemplateVO createTemplate(Long userId, TemplateCreateRequest request);
    
    /**
     * 更新模板
     */
    TemplateVO updateTemplate(Long id, Long userId, TemplateUpdateRequest request);
    
    /**
     * 删除模板
     */
    void deleteTemplate(Long id, Long userId);
    
    /**
     * 复制模板
     */
    TemplateVO copyTemplate(Long id, Long userId, String newName);
    
    /**
     * 设置默认模板
     */
    void setDefaultTemplate(Long id, Long userId);
    
    /**
     * 获取模板版本列表
     */
    List<TemplateVersionVO> getTemplateVersions(Long templateId);
    
    /**
     * 获取指定版本的模板
     */
    TemplateVersionVO getTemplateVersion(Long templateId, Integer version);
    
    /**
     * 回滚到指定版本
     */
    TemplateVO rollbackToVersion(Long templateId, Integer version, Long userId);
    
    /**
     * 获取用户的模板列表
     */
    List<TemplateVO> getUserTemplates(Long userId);
    
    /**
     * 获取公开模板列表
     */
    List<TemplateVO> getPublicTemplates();
    
    /**
     * 增加模板使用次数
     */
    void incrementUseCount(Long id);
}
