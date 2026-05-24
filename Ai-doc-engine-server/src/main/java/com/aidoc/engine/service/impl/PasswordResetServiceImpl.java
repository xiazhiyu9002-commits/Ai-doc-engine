package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.entity.PasswordResetTokenEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.repository.PasswordResetTokenRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.EmailService;
import com.aidoc.engine.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final long TOKEN_EXPIRATION_MINUTES = 10;
    
    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        log.info("请求密码重置: email={}", email);
        
        try {
            var userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                log.warn("密码重置请求的邮箱不存在，静默处理: email={}", email);
                return;
            }
            
            UserEntity user = userOptional.get();
            
            if (!"active".equals(user.getStatus())) {
                log.warn("密码重置请求的账号已被禁用，静默处理: email={}", email);
                return;
            }
            
            String token = UUID.randomUUID().toString();
            LocalDateTime expireAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);
            
            PasswordResetTokenEntity tokenEntity = PasswordResetTokenEntity.builder()
                    .userId(user.getId())
                    .email(email)
                    .token(token)
                    .expireAt(expireAt)
                    .used(false)
                    .build();
            
            tokenRepository.save(tokenEntity);
            
            try {
                emailService.sendPasswordResetEmail(email, token);
                log.info("密码重置请求处理成功: email={}, token={}", email, token);
            } catch (BusinessException e) {
                log.error("密码重置邮件发送失败，但令牌已保存: email={}, token={}, error={}", 
                        email, token, e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("密码重置邮件发送时发生未预期异常: email={}, token={}", email, token, e);
                throw new BusinessException(ErrorCode.EMAIL_SEND_ERROR, "邮件发送失败，请稍后重试");
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码重置请求处理失败: email={}", email, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "系统错误，请稍后重试");
        }
    }
    
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("重置密码: token={}", token);
        
        try {
            PasswordResetTokenEntity tokenEntity = tokenRepository.findByTokenAndUsed(token, false)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "令牌无效或已使用"));
            
            if (tokenEntity.getExpireAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "令牌已过期");
            }
            
            UserEntity user = userRepository.findById(tokenEntity.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
            
            if (!"active".equals(user.getStatus())) {
                throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用");
            }
            
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            tokenEntity.setUsed(true);
            tokenRepository.save(tokenEntity);
            
            log.info("密码重置成功: userId={}", user.getId());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码重置失败: token={}", token, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "密码重置失败，请稍后重试");
        }
    }
    
    @Override
    public boolean validateResetToken(String token) {
        try {
            return tokenRepository.findByTokenAndUsed(token, false)
                    .map(tokenEntity -> tokenEntity.getExpireAt().isAfter(LocalDateTime.now()))
                    .orElse(false);
        } catch (Exception e) {
            log.error("验证重置令牌失败: token={}", token, e);
            return false;
        }
    }
}
