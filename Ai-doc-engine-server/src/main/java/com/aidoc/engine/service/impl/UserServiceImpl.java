package com.aidoc.engine.service.impl;

import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void incrementOcrCount(Long userId) {
        if (userId == null) {
            log.warn("用户ID为空，跳过OCR计数增加");
            return;
        }
        try {
            userRepository.incrementOcrCount(userId);
            log.debug("用户OCR计数增加成功: userId={}", userId);
        } catch (Exception e) {
            log.error("增加用户OCR计数失败: userId={}", userId, e);
        }
    }
    
    @Override
    @Transactional
    public void incrementExportCount(Long userId) {
        if (userId == null) {
            log.warn("用户ID为空，跳过导出计数增加");
            return;
        }
        try {
            userRepository.incrementExportCount(userId);
            log.debug("用户导出计数增加成功: userId={}", userId);
        } catch (Exception e) {
            log.error("增加用户导出计数失败: userId={}", userId, e);
        }
    }
}
