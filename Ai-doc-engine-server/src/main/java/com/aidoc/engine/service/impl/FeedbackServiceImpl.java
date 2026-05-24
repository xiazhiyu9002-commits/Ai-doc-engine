package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.config.FileStorageProperties;
import com.aidoc.engine.model.dto.feedback.FeedbackQueryRequest;
import com.aidoc.engine.model.dto.feedback.FeedbackSubmitRequest;
import com.aidoc.engine.model.dto.feedback.FeedbackUpdateRequest;
import com.aidoc.engine.model.entity.FeedbackEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.enums.FeedbackPriority;
import com.aidoc.engine.model.enums.FeedbackStatus;
import com.aidoc.engine.model.enums.FeedbackType;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.feedback.FeedbackDetailVO;
import com.aidoc.engine.model.vo.feedback.FeedbackStatsVO;
import com.aidoc.engine.model.vo.feedback.FeedbackVO;
import com.aidoc.engine.repository.FeedbackRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.EmailService;
import com.aidoc.engine.service.FeedbackService;
import com.aidoc.engine.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public FeedbackVO submitFeedback(FeedbackSubmitRequest request, List<MultipartFile> images, Long userId, String ip) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        List<String> imageUrls = null;
        if (images != null && !images.isEmpty()) {
            imageUrls = fileStorageService.storeFiles(images, FileStorageProperties.SubDir.FEEDBACK);
        }
        
        FeedbackEntity feedback = FeedbackEntity.builder()
                .userId(userId)
                .content(request.getContent())
                .feedbackType(FeedbackType.fromCode(request.getFeedbackType()).getCode())
                .status(FeedbackStatus.PENDING.getCode())
                .priority(FeedbackPriority.NORMAL.getCode())
                .imageUrls(imageUrls)
                .build();
        
        feedback = feedbackRepository.save(feedback);
        
        log.info("用户提交反馈成功: feedbackId={}, userId={}", feedback.getId(), userId);
        
        return convertToVO(feedback, user);
    }
    
    @Override
    public PageResult<FeedbackVO> getFeedbackList(FeedbackQueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created_at");
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
        
        Page<FeedbackEntity> page = feedbackRepository.findByConditions(
                request.getStatus(),
                request.getFeedbackType(),
                request.getPriority(),
                request.getUserId(),
                request.getKeyword(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );
        
        List<FeedbackVO> items = page.getContent().stream()
                .map(feedback -> {
                    UserEntity user = userRepository.findById(feedback.getUserId()).orElse(null);
                    return convertToVO(feedback, user);
                })
                .collect(Collectors.toList());
        
        return PageResult.<FeedbackVO>builder()
                .items(items)
                .total(page.getTotalElements())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
    
    @Override
    public FeedbackDetailVO getFeedbackDetail(Long id, Long currentUserId, boolean isAdmin) {
        FeedbackEntity feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "反馈不存在"));
        
        if (!isAdmin && !feedback.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问此反馈");
        }
        
        UserEntity user = userRepository.findById(feedback.getUserId()).orElse(null);
        FeedbackDetailVO detailVO = FeedbackDetailVO.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .username(user != null ? user.getUsername() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .content(feedback.getContent())
                .feedbackType(feedback.getFeedbackType())
                .status(feedback.getStatus())
                .priority(feedback.getPriority())
                .imageUrls(feedback.getImageUrls())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
        
        if (feedback.getProcessedBy() != null) {
            UserEntity processor = userRepository.findById(feedback.getProcessedBy()).orElse(null);
            detailVO.setProcessedByName(processor != null ? processor.getUsername() : null);
        }
        
        return detailVO;
    }
    
    @Override
    @Transactional
    public FeedbackVO updateFeedbackStatus(Long id, FeedbackUpdateRequest request, Long operatorId, String ip) {
        FeedbackEntity feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "反馈不存在"));
        
        if (request.getStatus() != null) {
            feedback.setStatus(request.getStatus());
            
            if (FeedbackStatus.RESOLVED.getCode().equals(request.getStatus())) {
                feedback.setProcessedBy(operatorId);
            }
        }
        
        if (request.getPriority() != null) {
            feedback.setPriority(request.getPriority());
        }
        
        feedback = feedbackRepository.save(feedback);
        
        if (FeedbackStatus.RESOLVED.getCode().equals(feedback.getStatus())) {
            sendFeedbackNotification(feedback);
        }
        
        log.info("更新反馈状态: feedbackId={}, operatorId={}, request={}", id, operatorId, request);
        
        UserEntity user = userRepository.findById(feedback.getUserId()).orElse(null);
        return convertToVO(feedback, user);
    }
    
    @Override
    @Transactional
    public FeedbackVO replyFeedback(Long id, String reply, Long operatorId, String ip) {
        FeedbackEntity feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "反馈不存在"));
        
        feedback.setProcessedBy(operatorId);
        
        if (FeedbackStatus.PENDING.getCode().equals(feedback.getStatus())) {
            feedback.setStatus(FeedbackStatus.PROCESSING.getCode());
        }
        
        feedback = feedbackRepository.save(feedback);
        
        log.info("回复反馈: feedbackId={}, operatorId={}", id, operatorId);
        
        UserEntity user = userRepository.findById(feedback.getUserId()).orElse(null);
        return convertToVO(feedback, user);
    }
    
    @Override
    public PageResult<FeedbackVO> getUserFeedbacks(Long userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        Page<FeedbackEntity> pageResult = feedbackRepository.findByUserId(userId, pageable);
        
        UserEntity user = userRepository.findById(userId).orElse(null);
        List<FeedbackVO> items = pageResult.getContent().stream()
                .map(feedback -> convertToVO(feedback, user))
                .collect(Collectors.toList());
        
        return PageResult.<FeedbackVO>builder()
                .items(items)
                .total(pageResult.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    public FeedbackStatsVO getFeedbackStats() {
        return FeedbackStatsVO.builder()
                .totalFeedbacks(feedbackRepository.count())
                .pendingFeedbacks(feedbackRepository.countByStatus(FeedbackStatus.PENDING.getCode()))
                .processingFeedbacks(feedbackRepository.countByStatus(FeedbackStatus.PROCESSING.getCode()))
                .resolvedFeedbacks(feedbackRepository.countByStatus(FeedbackStatus.RESOLVED.getCode()))
                .closedFeedbacks(feedbackRepository.countByStatus(FeedbackStatus.CLOSED.getCode()))
                .todayFeedbacks(feedbackRepository.countByCreatedAtAfter(LocalDateTime.now().toLocalDate().atStartOfDay()))
                .weekFeedbacks(feedbackRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7)))
                .monthFeedbacks(feedbackRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)))
                .bugCount(feedbackRepository.countByFeedbackType(FeedbackType.BUG.getCode()))
                .suggestionCount(feedbackRepository.countByFeedbackType(FeedbackType.SUGGESTION.getCode()))
                .featureCount(feedbackRepository.countByFeedbackType(FeedbackType.FEATURE.getCode()))
                .highPriorityCount(feedbackRepository.countHighPriorityPending())
                .build();
    }
    
    @Override
    @Transactional
    public void deleteFeedback(Long id, Long operatorId, String ip) {
        FeedbackEntity feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "反馈不存在"));
        
        if (feedback.getImageUrls() != null) {
            for (String imageUrl : feedback.getImageUrls()) {
                fileStorageService.deleteFile(imageUrl);
            }
        }
        
        feedbackRepository.delete(feedback);
        
        log.info("删除反馈: feedbackId={}, operatorId={}", id, operatorId);
    }
    
    private void sendFeedbackNotification(FeedbackEntity feedback) {
        try {
            UserEntity user = userRepository.findById(feedback.getUserId()).orElse(null);
            if (user == null) {
                return;
            }
            
            emailService.sendFeedbackResolvedEmail(
                    user.getEmail(),
                    "反馈处理通知",
                    "感谢您的反馈，问题已处理完成。",
                    feedback.getId().toString()
            );
            
            log.info("发送反馈通知邮件: feedbackId={}, email={}", feedback.getId(), user.getEmail());
        } catch (Exception e) {
            log.warn("发送反馈通知邮件失败", e);
        }
    }
    
    private FeedbackVO convertToVO(FeedbackEntity feedback, UserEntity user) {
        FeedbackVO vo = FeedbackVO.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .username(user != null ? user.getUsername() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .content(feedback.getContent())
                .feedbackType(feedback.getFeedbackType())
                .status(feedback.getStatus())
                .priority(feedback.getPriority())
                .imageUrls(feedback.getImageUrls())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
        
        if (feedback.getProcessedBy() != null) {
            UserEntity processor = userRepository.findById(feedback.getProcessedBy()).orElse(null);
            vo.setProcessedByName(processor != null ? processor.getUsername() : null);
        }
        
        return vo;
    }
}
