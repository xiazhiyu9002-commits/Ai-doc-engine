package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.ocr.OcrRecordQueryRequest;
import com.aidoc.engine.model.entity.FormulaOcrRecordEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.enums.OcrStatus;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.ocr.OcrRecordVO;
import com.aidoc.engine.model.vo.ocr.OcrStatsVO;
import com.aidoc.engine.repository.FormulaOcrRecordRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.OcrRecordService;
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
 * OCR日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrRecordServiceImpl implements OcrRecordService {
    
    private final FormulaOcrRecordRepository ocrRecordRepository;
    private final UserRepository userRepository;
    
    @Override
    public PageResult<OcrRecordVO> getOcrRecords(OcrRecordQueryRequest request) {
        log.info("查询OCR记录: page={}, size={}, userId={}, status={}", 
                request.getPage(), request.getSize(), request.getUserId(), request.getStatus());
        
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        
        Page<FormulaOcrRecordEntity> recordPage = ocrRecordRepository.findByConditions(
                request.getUserId(),
                request.getStatus(),
                request.getStartTime(),
                request.getEndTime(),
                pageable
        );
        
        // 批量获取用户信息
        List<Long> userIds = recordPage.getContent().stream()
                .map(FormulaOcrRecordEntity::getUserId)
                .distinct()
                .toList();
        
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        List<OcrRecordVO> items = recordPage.getContent().stream()
                .map(entity -> convertToVO(entity, userMap.get(entity.getUserId())))
                .toList();
        
        return PageResult.<OcrRecordVO>builder()
                .items(items)
                .total(recordPage.getTotalElements())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
    
    @Override
    public OcrRecordVO getOcrRecordById(Long id) {
        log.info("获取OCR记录详情: id={}", id);
        
        FormulaOcrRecordEntity entity = ocrRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "OCR记录不存在"));
        
        UserEntity user = userRepository.findById(entity.getUserId()).orElse(null);
        
        return convertToVO(entity, user);
    }
    
    @Override
    public OcrStatsVO getOcrStats() {
        log.info("获取OCR统计信息");
        
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekAgoStart = todayStart.minusDays(7);
        
        Long totalCount = ocrRecordRepository.count();
        Long successCount = ocrRecordRepository.countByStatus(OcrStatus.RECOGNIZED.getCode());
        Long failedCount = ocrRecordRepository.countByStatus(OcrStatus.FAILED.getCode());
        
        Double successRate = totalCount > 0 
                ? Math.round((double) successCount / totalCount * 10000.0) / 100.0 
                : 0.0;
        
        Double avgProcessingTime = ocrRecordRepository.calculateAvgProcessingTime();
        if (avgProcessingTime != null) {
            avgProcessingTime = Math.round(avgProcessingTime * 100.0) / 100.0;
        }
        
        Long todayCount = ocrRecordRepository.countByCreatedAtAfter(todayStart);
        Long weekCount = ocrRecordRepository.countByCreatedAtAfter(weekAgoStart);
        
        return OcrStatsVO.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .successRate(successRate)
                .avgProcessingTime(avgProcessingTime)
                .todayCount(todayCount)
                .weekCount(weekCount)
                .build();
    }
    
    @Override
    public PageResult<OcrRecordVO> getOcrRecordsByUserId(Long userId, int page, int size) {
        log.info("获取用户OCR记录: userId={}, page={}, size={}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<FormulaOcrRecordEntity> recordPage = ocrRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        UserEntity user = userRepository.findById(userId).orElse(null);
        
        List<OcrRecordVO> items = recordPage.getContent().stream()
                .map(entity -> convertToVO(entity, user))
                .toList();
        
        return PageResult.<OcrRecordVO>builder()
                .items(items)
                .total(recordPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    @Transactional
    public void deleteOcrRecord(Long id) {
        log.info("删除OCR记录: id={}", id);
        
        FormulaOcrRecordEntity entity = ocrRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "OCR记录不存在"));
        
        ocrRecordRepository.delete(entity);
        
        log.info("OCR记录删除成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void deleteOcrRecords(List<Long> ids) {
        log.info("批量删除OCR记录: ids={}", ids);
        
        List<FormulaOcrRecordEntity> entities = ocrRecordRepository.findAllById(ids);
        ocrRecordRepository.deleteAll(entities);
        
        log.info("批量删除OCR记录成功: count={}", entities.size());
    }
    
    /**
     * 转换为VO
     */
    private OcrRecordVO convertToVO(FormulaOcrRecordEntity entity, UserEntity user) {
        OcrStatus status = OcrStatus.fromCode(entity.getStatus());
        
        return OcrRecordVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .status(entity.getStatus())
                .statusDesc(status != null ? status.getDescription() : entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .processingTimeMs(entity.getProcessingTimeMs())
                .inputContent(entity.getInputContent())
                .outputContent(entity.getOutputContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
