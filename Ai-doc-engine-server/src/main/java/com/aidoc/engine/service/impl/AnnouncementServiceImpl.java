package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.announcement.AnnouncementCreateRequest;
import com.aidoc.engine.model.entity.AnnouncementEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.enums.AnnouncementType;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.announcement.AnnouncementVO;
import com.aidoc.engine.repository.AnnouncementRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
    
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public AnnouncementVO createAnnouncement(AnnouncementCreateRequest request, Long userId) {
        AnnouncementEntity announcement = AnnouncementEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .announcementType(AnnouncementType.fromCode(request.getAnnouncementType()).getCode())
                .expireAt(request.getExpireAt())
                .createdBy(userId)
                .isPublished(false)
                .build();
        
        announcement = announcementRepository.save(announcement);
        
        log.info("创建公告成功: announcementId={}, title={}, createdBy={}", announcement.getId(), announcement.getTitle(), userId);
        
        return convertToVO(announcement);
    }
    
    @Override
    @Transactional
    public AnnouncementVO updateAnnouncement(Long id, AnnouncementCreateRequest request) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "公告不存在"));
        
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAnnouncementType(AnnouncementType.fromCode(request.getAnnouncementType()).getCode());
        announcement.setExpireAt(request.getExpireAt());
        
        announcement = announcementRepository.save(announcement);
        
        log.info("更新公告成功: announcementId={}", id);
        
        return convertToVO(announcement);
    }
    
    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "公告不存在"));
        
        announcementRepository.delete(announcement);
        
        log.info("删除公告成功: announcementId={}", id);
    }
    
    @Override
    @Transactional
    public AnnouncementVO publishAnnouncement(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "公告不存在"));
        
        if (announcement.getIsPublished()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "公告已发布");
        }
        
        announcement.setIsPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());
        
        announcement = announcementRepository.save(announcement);
        
        log.info("发布公告成功: announcementId={}", id);
        
        return convertToVO(announcement);
    }
    
    @Override
    @Transactional
    public AnnouncementVO unpublishAnnouncement(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "公告不存在"));
        
        announcement.setIsPublished(false);
        
        announcement = announcementRepository.save(announcement);
        
        log.info("取消发布公告成功: announcementId={}", id);
        
        return convertToVO(announcement);
    }
    
    @Override
    public PageResult<AnnouncementVO> getAnnouncementList(int page, int size, String type, Boolean isPublished) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created_at");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        Page<AnnouncementEntity> pageResult = announcementRepository.findByConditions(type, isPublished, pageable);
        
        List<AnnouncementVO> items = pageResult.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.<AnnouncementVO>builder()
                .items(items)
                .total(pageResult.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    public List<AnnouncementVO> getPublishedAnnouncements() {
        List<AnnouncementEntity> announcements = announcementRepository.findPublishedAndNotExpired(LocalDateTime.now());
        
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AnnouncementVO getAnnouncementById(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "公告不存在"));
        
        return convertToVO(announcement);
    }
    
    private AnnouncementVO convertToVO(AnnouncementEntity announcement) {
        UserEntity creator = userRepository.findById(announcement.getCreatedBy()).orElse(null);
        
        return AnnouncementVO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .announcementType(announcement.getAnnouncementType())
                .isPublished(announcement.getIsPublished())
                .publishedAt(announcement.getPublishedAt())
                .expireAt(announcement.getExpireAt())
                .createdBy(announcement.getCreatedBy())
                .createdByName(creator != null ? creator.getUsername() : null)
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}
