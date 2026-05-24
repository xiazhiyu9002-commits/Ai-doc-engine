package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.template.TemplateConfig;
import com.aidoc.engine.model.dto.template.TemplateCreateRequest;
import com.aidoc.engine.model.dto.template.TemplateQueryRequest;
import com.aidoc.engine.model.dto.template.TemplateUpdateRequest;
import com.aidoc.engine.model.entity.TemplateEntity;
import com.aidoc.engine.model.entity.TemplateVersionEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.template.TemplateListResponse;
import com.aidoc.engine.model.vo.template.TemplateVersionVO;
import com.aidoc.engine.model.vo.template.TemplateVO;
import com.aidoc.engine.repository.TemplateRepository;
import com.aidoc.engine.repository.TemplateVersionRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.TemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    
    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public TemplateListResponse getTemplateList() {
        List<TemplateEntity> templates = templateRepository.findAllByOrderByCreatedAtDesc();
        
        List<TemplateVO> templateVOs = templates.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return TemplateListResponse.builder()
                .templates(templateVOs)
                .build();
    }
    
    @Override
    public PageResult<TemplateVO> getTemplates(TemplateQueryRequest request) {
        log.info("分页查询模板: page={}, size={}", request.getPage(), request.getSize());
        
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        
        Page<TemplateEntity> templatePage = templateRepository.findByConditions(
                request.getUserId(),
                request.getTemplateType(),
                request.getIsDefault(),
                request.getIsPublic(),
                request.getKeyword(),
                pageable
        );
        
        // 批量获取用户信息
        List<Long> userIds = templatePage.getContent().stream()
                .map(TemplateEntity::getUserId)
                .distinct()
                .toList();
        
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        List<TemplateVO> items = templatePage.getContent().stream()
                .map(entity -> convertToVOWithUser(entity, userMap.get(entity.getUserId())))
                .toList();
        
        return PageResult.<TemplateVO>builder()
                .items(items)
                .total(templatePage.getTotalElements())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
    
    @Override
    public TemplateVO getTemplateById(Long id) {
        log.info("获取模板: id={}", id);
        
        TemplateEntity template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在"));
        
        return convertToVO(template);
    }
    
    @Override
    @Transactional
    public TemplateVO createTemplate(Long userId, TemplateCreateRequest request) {
        log.info("创建模板: userId={}, name={}", userId, request.getName());
        
        // 检查名称是否重复
        if (templateRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板名称已存在");
        }
        
        try {
            String configJson = objectMapper.writeValueAsString(request.getConfig());
            
            TemplateEntity template = TemplateEntity.builder()
                    .userId(userId)
                    .name(request.getName())
                    .description(request.getDescription())
                    .templateType(request.getTemplateType())
                    .configJson(configJson)
                    .isDefault(request.getIsDefault())
                    .isPublic(request.getIsPublic())
                    .useCount(0)
                    .build();
            
            template = templateRepository.save(template);
            
            // 创建初始版本
            createVersion(template.getId(), template, userId, "初始版本", null);
            
            log.info("模板创建成功: id={}", template.getId());
            
            return convertToVO(template);
            
        } catch (JsonProcessingException e) {
            log.error("模板配置序列化失败", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板配置格式错误");
        }
    }
    
    @Override
    @Transactional
    public TemplateVO updateTemplate(Long id, Long userId, TemplateUpdateRequest request) {
        log.info("更新模板: id={}, userId={}", id, userId);
        
        TemplateEntity template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在"));
        
        // 检查权限
        if (!template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权修改此模板");
        }
        
        try {
            String configJson = objectMapper.writeValueAsString(request.getConfig());
            
            template.setName(request.getName());
            template.setDescription(request.getDescription());
            template.setConfigJson(configJson);
            
            if (request.getIsDefault() != null) {
                template.setIsDefault(request.getIsDefault());
            }
            if (request.getIsPublic() != null) {
                template.setIsPublic(request.getIsPublic());
            }
            
            template = templateRepository.save(template);
            
            // 创建新版本
            createVersion(id, template, userId, null, request.getChangeNote());
            
            log.info("模板更新成功: id={}", id);
            
            return convertToVO(template);
            
        } catch (JsonProcessingException e) {
            log.error("模板配置序列化失败", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板配置格式错误");
        }
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long id, Long userId) {
        log.info("删除模板: id={}, userId={}", id, userId);
        
        TemplateEntity template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在"));
        
        // 检查权限
        if (!template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除此模板");
        }
        
        // 删除模板的所有版本
        templateVersionRepository.deleteByTemplateId(id);
        
        // 删除模板
        templateRepository.delete(template);
        
        log.info("模板删除成功: id={}", id);
    }
    
    @Override
    @Transactional
    public TemplateVO copyTemplate(Long id, Long userId, String newName) {
        log.info("复制模板: id={}, userId={}, newName={}", id, userId, newName);
        
        TemplateEntity source = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "源模板不存在"));
        
        // 检查名称是否重复
        if (templateRepository.existsByNameAndUserId(newName, userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板名称已存在");
        }
        
        TemplateEntity copy = TemplateEntity.builder()
                .userId(userId)
                .name(newName)
                .description(source.getDescription())
                .templateType("custom")
                .configJson(source.getConfigJson())
                .isDefault(false)
                .isPublic(false)
                .useCount(0)
                .build();
        
        copy = templateRepository.save(copy);
        
        // 创建初始版本
        createVersion(copy.getId(), copy, userId, "复制自: " + source.getName(), null);
        
        log.info("模板复制成功: newId={}", copy.getId());
        
        return convertToVO(copy);
    }
    
    @Override
    @Transactional
    public void setDefaultTemplate(Long id, Long userId) {
        log.info("设置默认模板: id={}, userId={}", id, userId);
        
        TemplateEntity template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在"));
        
        // 检查权限
        if (!template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此模板");
        }
        
        // 取消原有默认模板
        templateRepository.findByIsDefaultTrue().ifPresent(defaultTemplate -> {
            defaultTemplate.setIsDefault(false);
            templateRepository.save(defaultTemplate);
        });
        
        // 设置新的默认模板
        template.setIsDefault(true);
        templateRepository.save(template);
        
        log.info("默认模板设置成功: id={}", id);
    }
    
    @Override
    public List<TemplateVersionVO> getTemplateVersions(Long templateId) {
        log.info("获取模板版本列表: templateId={}", templateId);
        
        // 检查模板是否存在
        if (!templateRepository.existsById(templateId)) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在");
        }
        
        List<TemplateVersionEntity> versions = templateVersionRepository.findByTemplateIdOrderByVersionDesc(templateId);
        
        // 批量获取用户信息
        List<Long> userIds = versions.stream()
                .map(TemplateVersionEntity::getCreatedBy)
                .distinct()
                .toList();
        
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        return versions.stream()
                .map(v -> convertVersionToVO(v, userMap.get(v.getCreatedBy())))
                .toList();
    }
    
    @Override
    public TemplateVersionVO getTemplateVersion(Long templateId, Integer version) {
        log.info("获取模板版本: templateId={}, version={}", templateId, version);
        
        TemplateVersionEntity versionEntity = templateVersionRepository
                .findByTemplateIdAndVersion(templateId, version)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "版本不存在"));
        
        UserEntity user = userRepository.findById(versionEntity.getCreatedBy()).orElse(null);
        
        return convertVersionToVO(versionEntity, user);
    }
    
    @Override
    @Transactional
    public TemplateVO rollbackToVersion(Long templateId, Integer version, Long userId) {
        log.info("回滚模板版本: templateId={}, version={}, userId={}", templateId, version, userId);
        
        TemplateEntity template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在"));
        
        // 检查权限
        if (!template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此模板");
        }
        
        TemplateVersionEntity versionEntity = templateVersionRepository
                .findByTemplateIdAndVersion(templateId, version)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "版本不存在"));
        
        // 回滚到指定版本
        template.setName(versionEntity.getName());
        template.setDescription(versionEntity.getDescription());
        template.setConfigJson(versionEntity.getConfigJson());
        
        template = templateRepository.save(template);
        
        // 创建新版本记录回滚操作
        createVersion(templateId, template, userId, null, "回滚到版本 " + version);
        
        log.info("模板回滚成功: templateId={}, toVersion={}", templateId, version);
        
        return convertToVO(template);
    }
    
    @Override
    public List<TemplateVO> getUserTemplates(Long userId) {
        log.info("获取用户模板列表: userId={}", userId);
        
        List<TemplateEntity> templates = templateRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return templates.stream()
                .map(this::convertToVO)
                .toList();
    }
    
    @Override
    public List<TemplateVO> getPublicTemplates() {
        log.info("获取公开模板列表");
        
        List<TemplateEntity> templates = templateRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        
        return templates.stream()
                .map(this::convertToVO)
                .toList();
    }
    
    @Override
    @Transactional
    public void incrementUseCount(Long id) {
        log.info("增加模板使用次数: id={}", id);
        
        TemplateEntity template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND, "模板不存在"));
        
        template.setUseCount(template.getUseCount() + 1);
        templateRepository.save(template);
    }
    
    /**
     * 创建模板版本
     */
    private void createVersion(Long templateId, TemplateEntity template, Long userId, String versionName, String changeNote) {
        Integer maxVersion = templateVersionRepository.findMaxVersionByTemplateId(templateId);
        int newVersion = (maxVersion != null ? maxVersion : 0) + 1;
        
        TemplateVersionEntity version = TemplateVersionEntity.builder()
                .templateId(templateId)
                .version(newVersion)
                .versionName(versionName != null ? versionName : "v" + newVersion)
                .name(template.getName())
                .description(template.getDescription())
                .configJson(template.getConfigJson())
                .changeNote(changeNote)
                .createdBy(userId)
                .build();
        
        templateVersionRepository.save(version);
        
        log.info("创建模板版本: templateId={}, version={}", templateId, newVersion);
    }
    
    /**
     * 转换为VO
     */
    private TemplateVO convertToVO(TemplateEntity entity) {
        try {
            TemplateConfig config = objectMapper.readValue(entity.getConfigJson(), TemplateConfig.class);
            
            return TemplateVO.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .templateType(entity.getTemplateType())
                    .config(config)
                    .isDefault(entity.getIsDefault())
                    .isPublic(entity.getIsPublic())
                    .useCount(entity.getUseCount())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("模板配置反序列化失败", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板配置格式错误");
        }
    }
    
    /**
     * 转换为VO（带用户信息）
     */
    private TemplateVO convertToVOWithUser(TemplateEntity entity, UserEntity user) {
        try {
            TemplateConfig config = objectMapper.readValue(entity.getConfigJson(), TemplateConfig.class);
            
            return TemplateVO.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .templateType(entity.getTemplateType())
                    .config(config)
                    .creatorId(entity.getUserId())
                    .creatorName(user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : null)
                    .useCount(entity.getUseCount())
                    .isDefault(entity.getIsDefault())
                    .isPublic(entity.getIsPublic())
                    .status(1)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("模板配置反序列化失败", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板配置格式错误");
        }
    }
    
    /**
     * 转换版本为VO
     */
    private TemplateVersionVO convertVersionToVO(TemplateVersionEntity entity, UserEntity user) {
        try {
            TemplateConfig config = objectMapper.readValue(entity.getConfigJson(), TemplateConfig.class);
            
            return TemplateVersionVO.builder()
                    .id(entity.getId())
                    .templateId(entity.getTemplateId())
                    .version(entity.getVersion())
                    .versionName(entity.getVersionName())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .config(config)
                    .changeNote(entity.getChangeNote())
                    .createdBy(entity.getCreatedBy())
                    .createdByName(user != null ? user.getNickname() : null)
                    .createdAt(entity.getCreatedAt())
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("模板配置反序列化失败", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板配置格式错误");
        }
    }
}
