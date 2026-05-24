package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.user.ChangePasswordRequest;
import com.aidoc.engine.model.dto.user.UpdateNotificationSettingsRequest;
import com.aidoc.engine.model.dto.user.UpdateProfileRequest;
import com.aidoc.engine.model.vo.user.AccountBindingVO;
import com.aidoc.engine.model.vo.user.NotificationSettingsVO;
import com.aidoc.engine.model.vo.user.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户个人中心服务接口
 */
public interface UserProfileService {
    
    /**
     * 获取当前用户详细资料
     */
    UserProfileVO getProfile();
    
    /**
     * 更新用户资料
     */
    UserProfileVO updateProfile(UpdateProfileRequest request);
    
    /**
     * 修改密码
     */
    void changePassword(ChangePasswordRequest request);
    
    /**
     * 获取账户绑定列表
     */
    List<AccountBindingVO> getBindings();
    
    /**
     * 解除账户绑定
     */
    void unbind(Long bindingId);
    
    /**
     * 获取通知设置
     */
    NotificationSettingsVO getNotificationSettings();
    
    /**
     * 更新通知设置
     */
    NotificationSettingsVO updateNotificationSettings(UpdateNotificationSettingsRequest request);
    
    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file);
}
