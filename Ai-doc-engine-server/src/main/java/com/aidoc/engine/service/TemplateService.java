package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.template.TemplateCreateRequest;
import com.aidoc.engine.model.dto.template.TemplateQueryRequest;
import com.aidoc.engine.model.dto.template.TemplateUpdateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.template.TemplateListResponse;
import com.aidoc.engine.model.vo.template.TemplateVO;

import java.util.List;

public interface TemplateService {
    
    TemplateListResponse getTemplateList();
    
    PageResult<TemplateVO> getTemplates(TemplateQueryRequest request);
    
    TemplateVO getTemplateById(Long id);
    
    TemplateVO createTemplate(Long userId, TemplateCreateRequest request);
    
    TemplateVO updateTemplate(Long id, Long userId, TemplateUpdateRequest request);
    
    void deleteTemplate(Long id, Long userId);
    
    TemplateVO copyTemplate(Long id, Long userId, String newName);
    
    void setDefaultTemplate(Long id, Long userId);
    
    List<TemplateVO> getUserTemplates(Long userId);
    
    List<TemplateVO> getPublicTemplates();
    
    void incrementUseCount(Long id);
}
