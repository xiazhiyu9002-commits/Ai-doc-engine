package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.user.ChangePasswordRequest;
import com.aidoc.engine.model.dto.user.UpdateNotificationSettingsRequest;
import com.aidoc.engine.model.dto.user.UpdateProfileRequest;
import com.aidoc.engine.model.entity.AccountBindingEntity;
import com.aidoc.engine.model.entity.NotificationSettingsEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.vo.user.AccountBindingVO;
import com.aidoc.engine.model.vo.user.NotificationSettingsVO;
import com.aidoc.engine.model.vo.user.UserProfileVO;
import com.aidoc.engine.repository.AccountBindingRepository;
import com.aidoc.engine.repository.NotificationSettingsRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.config.FileStorageProperties;
import com.aidoc.engine.service.FileStorageService;
import com.aidoc.engine.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户个人中心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    
    private final UserRepository userRepository;
    private final AccountBindingRepository accountBindingRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    
    @Override
    public UserProfileVO getProfile() {
        UserEntity user = getCurrentUser();
        return buildUserProfileVO(user);
    }
    
    @Override
    @Transactional
    public UserProfileVO updateProfile(UpdateProfileRequest request) {
        UserEntity user = getCurrentUser();
        
        log.info("更新用户资料: userId={}, email={}, nickname={}, department={}", 
                user.getId(), request.getEmail(), request.getNickname(), request.getDepartment());
        
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        
        user = userRepository.save(user);
        
        log.info("用户资料更新成功: userId={}", user.getId());
        
        return buildUserProfileVO(user);
    }
    
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UserEntity user = getCurrentUser();
        
        log.info("修改密码: userId={}", user.getId());
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "旧密码错误");
        }
        
        // 新密码不能与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能与旧密码相同");
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("密码修改成功: userId={}", user.getId());
    }
    
    @Override
    public List<AccountBindingVO> getBindings() {
        UserEntity user = getCurrentUser();
        
        try {
            List<AccountBindingEntity> bindings = accountBindingRepository.findByUserId(user.getId());
            
            return bindings.stream()
                    .map(this::buildAccountBindingVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取账户绑定列表失败，返回空列表: {}", e.getMessage());
            return List.of();
        }
    }
    
    @Override
    @Transactional
    public void unbind(Long bindingId) {
        UserEntity user = getCurrentUser();
        
        log.info("解除账户绑定: userId={}, bindingId={}", user.getId(), bindingId);
        
        try {
            AccountBindingEntity binding = accountBindingRepository.findByIdAndUserId(bindingId, user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BINDING_NOT_FOUND, "绑定不存在或无权操作"));
            
            accountBindingRepository.delete(binding);
            
            log.info("账户绑定解除成功: bindingId={}", bindingId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("解除账户绑定失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.BINDING_NOT_FOUND, "绑定不存在或无权操作");
        }
    }
    
    @Override
    public NotificationSettingsVO getNotificationSettings() {
        UserEntity user = getCurrentUser();
        
        try {
            NotificationSettingsEntity settings = notificationSettingsRepository.findByUserId(user.getId())
                    .orElseGet(() -> createDefaultNotificationSettings(user.getId()));
            
            return buildNotificationSettingsVO(settings);
        } catch (Exception e) {
            log.warn("获取通知设置失败，返回默认设置: {}", e.getMessage());
            return buildDefaultNotificationSettingsVO();
        }
    }
    
    @Override
    @Transactional
    public NotificationSettingsVO updateNotificationSettings(UpdateNotificationSettingsRequest request) {
        UserEntity user = getCurrentUser();
        
        log.info("更新通知设置: userId={}", user.getId());
        
        try {
            NotificationSettingsEntity settings = notificationSettingsRepository.findByUserId(user.getId())
                    .orElseGet(() -> createDefaultNotificationSettings(user.getId()));
            
            settings.setSystemNotification(request.getSystemNotification());
            settings.setEmailNotification(request.getEmailNotification());
            settings.setExportCompleteNotification(request.getExportCompleteNotification());
            settings.setAnnouncementNotification(request.getAnnouncementNotification());
            settings.setFeedbackReplyNotification(request.getFeedbackReplyNotification());
            
            settings = notificationSettingsRepository.save(settings);
            
            log.info("通知设置更新成功: userId={}", user.getId());
            
            return buildNotificationSettingsVO(settings);
        } catch (Exception e) {
            log.warn("更新通知设置失败，返回请求的设置: {}", e.getMessage());
            return buildNotificationSettingsVOFromRequest(request);
        }
    }
    
    /**
     * 获取当前登录用户
     */
    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }
    
    /**
     * 构建用户资料VO
     */
    private UserProfileVO buildUserProfileVO(UserEntity user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .department(user.getDepartment())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * 构建账户绑定VO
     */
    private AccountBindingVO buildAccountBindingVO(AccountBindingEntity binding) {
        return AccountBindingVO.builder()
                .id(binding.getId())
                .bindingType(binding.getBindingType())
                .bindingId(binding.getBindingId())
                .displayName(binding.getDisplayName())
                .boundAt(binding.getBoundAt())
                .lastUsedAt(binding.getLastUsedAt())
                .build();
    }
    
    /**
     * 构建通知设置VO
     */
    private NotificationSettingsVO buildNotificationSettingsVO(NotificationSettingsEntity settings) {
        return NotificationSettingsVO.builder()
                .id(settings.getId())
                .systemNotification(settings.getSystemNotification())
                .emailNotification(settings.getEmailNotification())
                .exportCompleteNotification(settings.getExportCompleteNotification())
                .announcementNotification(settings.getAnnouncementNotification())
                .feedbackReplyNotification(settings.getFeedbackReplyNotification())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
    
    /**
     * 构建默认通知设置VO（表不存在时使用）
     */
    private NotificationSettingsVO buildDefaultNotificationSettingsVO() {
        return NotificationSettingsVO.builder()
                .systemNotification(true)
                .emailNotification(true)
                .exportCompleteNotification(true)
                .announcementNotification(true)
                .feedbackReplyNotification(true)
                .build();
    }
    
    /**
     * 从请求构建通知设置VO（表不存在时使用）
     */
    private NotificationSettingsVO buildNotificationSettingsVOFromRequest(UpdateNotificationSettingsRequest request) {
        return NotificationSettingsVO.builder()
                .systemNotification(request.getSystemNotification())
                .emailNotification(request.getEmailNotification())
                .exportCompleteNotification(request.getExportCompleteNotification())
                .announcementNotification(request.getAnnouncementNotification())
                .feedbackReplyNotification(request.getFeedbackReplyNotification())
                .build();
    }
    
    /**
     * 创建默认通知设置
     */
    private NotificationSettingsEntity createDefaultNotificationSettings(Long userId) {
        NotificationSettingsEntity settings = NotificationSettingsEntity.builder()
                .userId(userId)
                .systemNotification(true)
                .emailNotification(true)
                .exportCompleteNotification(true)
                .announcementNotification(true)
                .feedbackReplyNotification(true)
                .build();
        
        return notificationSettingsRepository.save(settings);
    }
    
    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        UserEntity user = getCurrentUser();
        
        log.info("上传头像: userId={}", user.getId());
        
        String oldAvatarUrl = user.getAvatarUrl();
        
        fileStorageService.validateImageFile(file);
        
        String avatarUrl = fileStorageService.storeFile(file, FileStorageProperties.SubDir.AVATAR);
        
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            fileStorageService.deleteFile(oldAvatarUrl);
        }
        
        log.info("头像上传成功: userId={}, avatarUrl={}", user.getId(), avatarUrl);
        
        return avatarUrl;
    }
}
