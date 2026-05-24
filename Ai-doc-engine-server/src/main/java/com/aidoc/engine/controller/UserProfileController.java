package com.aidoc.engine.controller;

import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.dto.user.ChangePasswordRequest;
import com.aidoc.engine.model.dto.user.UpdateNotificationSettingsRequest;
import com.aidoc.engine.model.dto.user.UpdateProfileRequest;
import com.aidoc.engine.model.vo.user.AccountBindingVO;
import com.aidoc.engine.model.vo.user.NotificationSettingsVO;
import com.aidoc.engine.model.vo.user.UserProfileVO;
import com.aidoc.engine.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户个人中心控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final UserProfileService userProfileService;
    
    /**
     * 获取当前用户详细资料
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        log.info("获取当前用户资料");
        
        UserProfileVO profile = userProfileService.getProfile();
        
        return ApiResponse.success(profile);
    }
    
    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public ApiResponse<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("更新用户资料: nickname={}, department={}", 
                request.getNickname(), request.getDepartment());
        
        UserProfileVO profile = userProfileService.updateProfile(request);
        
        return ApiResponse.success(profile);
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码请求");
        
        userProfileService.changePassword(request);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 获取账户绑定列表
     */
    @GetMapping("/bindings")
    public ApiResponse<List<AccountBindingVO>> getBindings() {
        log.info("获取账户绑定列表");
        
        List<AccountBindingVO> bindings = userProfileService.getBindings();
        
        return ApiResponse.success(bindings);
    }
    
    /**
     * 解除账户绑定
     */
    @DeleteMapping("/bindings/{id}")
    public ApiResponse<Void> unbind(@PathVariable("id") Long bindingId) {
        log.info("解除账户绑定: bindingId={}", bindingId);
        
        userProfileService.unbind(bindingId);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 获取通知设置
     */
    @GetMapping("/notification-settings")
    public ApiResponse<NotificationSettingsVO> getNotificationSettings() {
        log.info("获取通知设置");
        
        NotificationSettingsVO settings = userProfileService.getNotificationSettings();
        
        return ApiResponse.success(settings);
    }
    
    /**
     * 更新通知设置
     */
    @PutMapping("/notification-settings")
    public ApiResponse<NotificationSettingsVO> updateNotificationSettings(
            @Valid @RequestBody UpdateNotificationSettingsRequest request) {
        log.info("更新通知设置");
        
        NotificationSettingsVO settings = userProfileService.updateNotificationSettings(request);
        
        return ApiResponse.success(settings);
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传头像请求");
        
        String avatarUrl = userProfileService.uploadAvatar(file);
        
        return ApiResponse.success(avatarUrl);
    }
}
