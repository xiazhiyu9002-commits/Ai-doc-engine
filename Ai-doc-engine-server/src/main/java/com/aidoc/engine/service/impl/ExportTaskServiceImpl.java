package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.export.ExportTaskQueryRequest;
import com.aidoc.engine.model.entity.ExportTaskEntity;
import com.aidoc.engine.model.entity.TemplateEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.enums.ExportTaskStatus;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.export.ExportTaskStatsVO;
import com.aidoc.engine.model.vo.export.ExportTaskVO;
import com.aidoc.engine.repository.ExportTaskRepository;
import com.aidoc.engine.repository.TemplateRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.ExportTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导出任务日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportTaskServiceImpl implements ExportTaskService {
    
    private final ExportTaskRepository exportTaskRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    
    @Override
    public PageResult<ExportTaskVO> getExportTasks(ExportTaskQueryRequest request) {
        log.info("查询导出任务: page={}, size={}, userId={}, status={}", 
                request.getPage(), request.getSize(), request.getUserId(), request.getTaskStatus());
        
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        
        Page<ExportTaskEntity> taskPage = exportTaskRepository.findByConditions(
                request.getUserId(),
                request.getTemplateId(),
                request.getTaskStatus(),
                request.getStartTime(),
                request.getEndTime(),
                pageable
        );
        
        // 批量获取用户信息
        List<Long> userIds = taskPage.getContent().stream()
                .map(ExportTaskEntity::getUserId)
                .distinct()
                .toList();
        
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        // 批量获取模板信息
        List<Long> templateIds = taskPage.getContent().stream()
                .map(ExportTaskEntity::getTemplateId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        
        Map<Long, TemplateEntity> templateMap = templateIds.isEmpty() 
                ? Map.of() 
                : templateRepository.findAllById(templateIds).stream()
                    .collect(Collectors.toMap(TemplateEntity::getId, t -> t));
        
        List<ExportTaskVO> items = taskPage.getContent().stream()
                .map(entity -> convertToVO(entity, 
                        userMap.get(entity.getUserId()), 
                        templateMap.get(entity.getTemplateId())))
                .toList();
        
        return PageResult.<ExportTaskVO>builder()
                .items(items)
                .total(taskPage.getTotalElements())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
    
    @Override
    public ExportTaskVO getExportTaskById(Long id) {
        log.info("获取导出任务详情: id={}", id);
        
        ExportTaskEntity entity = exportTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在"));
        
        UserEntity user = userRepository.findById(entity.getUserId()).orElse(null);
        TemplateEntity template = entity.getTemplateId() != null 
                ? templateRepository.findById(entity.getTemplateId()).orElse(null) 
                : null;
        
        return convertToVO(entity, user, template);
    }
    
    @Override
    public ExportTaskStatsVO getExportTaskStats() {
        log.info("获取导出任务统计信息");
        
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekAgoStart = todayStart.minusDays(7);
        
        Long totalCount = exportTaskRepository.count();
        Long pendingCount = exportTaskRepository.countByTaskStatus(ExportTaskStatus.PENDING.getCode());
        Long processingCount = exportTaskRepository.countByTaskStatus(ExportTaskStatus.PROCESSING.getCode());
        Long successCount = exportTaskRepository.countByTaskStatus(ExportTaskStatus.SUCCESS.getCode());
        Long failedCount = exportTaskRepository.countByTaskStatus(ExportTaskStatus.FAILED.getCode());
        
        Double successRate = totalCount > 0 
                ? Math.round((double) successCount / totalCount * 10000.0) / 100.0 
                : 0.0;
        
        Double avgProcessingTime = exportTaskRepository.calculateAvgProcessingTime();
        if (avgProcessingTime != null) {
            avgProcessingTime = Math.round(avgProcessingTime * 100.0) / 100.0;
        }
        
        Long totalFileSize = exportTaskRepository.calculateTotalFileSize();
        Long totalCharCount = exportTaskRepository.calculateTotalCharCount();
        
        Long todayCount = exportTaskRepository.countByStartTimeAfter(todayStart);
        Long weekCount = exportTaskRepository.countByStartTimeAfter(weekAgoStart);
        
        return ExportTaskStatsVO.builder()
                .totalCount(totalCount)
                .pendingCount(pendingCount)
                .processingCount(processingCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .successRate(successRate)
                .avgProcessingTime(avgProcessingTime)
                .totalFileSize(totalFileSize != null ? totalFileSize : 0L)
                .totalCharCount(totalCharCount != null ? totalCharCount : 0L)
                .todayCount(todayCount)
                .weekCount(weekCount)
                .build();
    }
    
    @Override
    public PageResult<ExportTaskVO> getExportTasksByUserId(Long userId, int page, int size) {
        log.info("获取用户导出任务: userId={}, page={}, size={}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ExportTaskEntity> taskPage = exportTaskRepository.findByUserIdOrderByStartTimeDesc(userId, pageable);
        
        UserEntity user = userRepository.findById(userId).orElse(null);
        
        List<ExportTaskVO> items = taskPage.getContent().stream()
                .map(entity -> convertToVO(entity, user, null))
                .toList();
        
        return PageResult.<ExportTaskVO>builder()
                .items(items)
                .total(taskPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    public PageResult<ExportTaskVO> getExportTasksByTemplateId(Long templateId, int page, int size) {
        log.info("获取模板导出任务: templateId={}, page={}, size={}", templateId, page, size);
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ExportTaskEntity> taskPage = exportTaskRepository.findByTemplateIdOrderByStartTimeDesc(templateId, pageable);
        
        TemplateEntity template = templateRepository.findById(templateId).orElse(null);
        
        List<ExportTaskVO> items = taskPage.getContent().stream()
                .map(entity -> {
                    UserEntity user = userRepository.findById(entity.getUserId()).orElse(null);
                    return convertToVO(entity, user, template);
                })
                .toList();
        
        return PageResult.<ExportTaskVO>builder()
                .items(items)
                .total(taskPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    @Transactional
    public void deleteExportTask(Long id) {
        log.info("删除导出任务: id={}", id);
        
        ExportTaskEntity entity = exportTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在"));
        
        exportTaskRepository.delete(entity);
        
        log.info("导出任务删除成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void deleteExportTasks(List<Long> ids) {
        log.info("批量删除导出任务: ids={}", ids);
        
        List<ExportTaskEntity> entities = exportTaskRepository.findAllById(ids);
        exportTaskRepository.deleteAll(entities);
        
        log.info("批量删除导出任务成功: count={}", entities.size());
    }
    
    @Override
    @Transactional
    public void retryFailedTask(Long id) {
        log.info("重试失败任务: id={}", id);
        
        ExportTaskEntity entity = exportTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在"));
        
        if (!ExportTaskStatus.FAILED.getCode().equals(entity.getTaskStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能重试失败的任务");
        }
        
        // 重置任务状态为待处理
        entity.setTaskStatus(ExportTaskStatus.PENDING.getCode());
        entity.setErrorMessage(null);
        entity.setStartTime(null);
        entity.setEndTime(null);
        entity.setProcessingTimeMs(null);
        
        exportTaskRepository.save(entity);
        
        log.info("任务重试成功: id={}", id);
    }
    
    /**
     * 转换为VO
     */
    private ExportTaskVO convertToVO(ExportTaskEntity entity, UserEntity user, TemplateEntity template) {
        ExportTaskStatus status = ExportTaskStatus.fromCode(entity.getTaskStatus());
        
        return ExportTaskVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .templateId(entity.getTemplateId())
                .templateName(template != null ? template.getName() : null)
                .taskStatus(entity.getTaskStatus())
                .statusDesc(status != null ? status.getDescription() : entity.getTaskStatus())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .fileSizeFormatted(formatFileSize(entity.getFileSize()))
                .charCount(entity.getCharCount())
                .errorMessage(entity.getErrorMessage())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .processingTimeMs(entity.getProcessingTimeMs())
                .processingTimeFormatted(formatProcessingTime(entity.getProcessingTimeMs()))
                .build();
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "-";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 格式化处理时间
     */
    private String formatProcessingTime(Integer processingTimeMs) {
        if (processingTimeMs == null || processingTimeMs <= 0) {
            return "-";
        }
        
        if (processingTimeMs < 1000) {
            return processingTimeMs + " ms";
        } else if (processingTimeMs < 60000) {
            return String.format("%.2f s", processingTimeMs / 1000.0);
        } else {
            int minutes = processingTimeMs / 60000;
            int seconds = (processingTimeMs % 60000) / 1000;
            return minutes + " m " + seconds + " s";
        }
    }
}
