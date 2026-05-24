package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.password.PasswordResetTokenQueryRequest;
import com.aidoc.engine.model.entity.PasswordResetTokenEntity;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.password.PasswordResetStatsVO;
import com.aidoc.engine.model.vo.password.PasswordResetTokenVO;
import com.aidoc.engine.repository.PasswordResetTokenRepository;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.service.PasswordResetTokenService;
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
 * 密码重置令牌服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    
    @Override
    public PageResult<PasswordResetTokenVO> getPasswordResetTokens(PasswordResetTokenQueryRequest request) {
        log.info("查询密码重置令牌: page={}, size={}, userId={}", 
                request.getPage(), request.getSize(), request.getUserId());
        
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        
        Boolean usedParam = request.getUsed();
        
        Page<PasswordResetTokenEntity> tokenPage = tokenRepository.findByConditions(
                request.getUserId(),
                request.getEmail(),
                usedParam,
                request.getStartTime(),
                request.getEndTime(),
                pageable
        );
        
        // 批量获取用户信息
        List<Long> userIds = tokenPage.getContent().stream()
                .map(PasswordResetTokenEntity::getUserId)
                .distinct()
                .toList();
        
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
        
        LocalDateTime now = LocalDateTime.now();
        List<PasswordResetTokenVO> items = tokenPage.getContent().stream()
                .map(entity -> convertToVO(entity, userMap.get(entity.getUserId()), now))
                .toList();
        
        return PageResult.<PasswordResetTokenVO>builder()
                .items(items)
                .total(tokenPage.getTotalElements())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
    
    @Override
    public PasswordResetTokenVO getPasswordResetTokenById(Long id) {
        log.info("获取密码重置令牌详情: id={}", id);
        
        PasswordResetTokenEntity entity = tokenRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "令牌不存在"));
        
        UserEntity user = userRepository.findById(entity.getUserId()).orElse(null);
        
        return convertToVO(entity, user, LocalDateTime.now());
    }
    
    @Override
    public PasswordResetStatsVO getPasswordResetStats() {
        log.info("获取密码重置统计信息");
        
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekAgoStart = todayStart.minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        
        Long totalCount = tokenRepository.count();
        Long usedCount = tokenRepository.countByUsed(true);
        Long unusedCount = tokenRepository.countByUsed(false);
        Long expiredCount = tokenRepository.countExpiredTokens(now);
        
        Double usageRate = totalCount > 0 
                ? Math.round((double) usedCount / totalCount * 10000.0) / 100.0 
                : 0.0;
        
        Long todayCount = tokenRepository.countByCreatedAtAfter(todayStart);
        Long weekCount = tokenRepository.countByCreatedAtAfter(weekAgoStart);
        
        return PasswordResetStatsVO.builder()
                .totalCount(totalCount)
                .usedCount(usedCount)
                .unusedCount(unusedCount)
                .expiredCount(expiredCount)
                .usageRate(usageRate)
                .todayCount(todayCount)
                .weekCount(weekCount)
                .build();
    }
    
    @Override
    public PageResult<PasswordResetTokenVO> getPasswordResetTokensByUserId(Long userId, int page, int size) {
        log.info("获取用户密码重置令牌: userId={}, page={}, size={}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PasswordResetTokenEntity> tokenPage = tokenRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        UserEntity user = userRepository.findById(userId).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        
        List<PasswordResetTokenVO> items = tokenPage.getContent().stream()
                .map(entity -> convertToVO(entity, user, now))
                .toList();
        
        return PageResult.<PasswordResetTokenVO>builder()
                .items(items)
                .total(tokenPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
    
    @Override
    @Transactional
    public void deletePasswordResetToken(Long id) {
        log.info("删除密码重置令牌: id={}", id);
        
        PasswordResetTokenEntity entity = tokenRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "令牌不存在"));
        
        tokenRepository.delete(entity);
        
        log.info("密码重置令牌删除成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void deletePasswordResetTokens(List<Long> ids) {
        log.info("批量删除密码重置令牌: ids={}", ids);
        
        List<PasswordResetTokenEntity> entities = tokenRepository.findAllById(ids);
        tokenRepository.deleteAll(entities);
        
        log.info("批量删除密码重置令牌成功: count={}", entities.size());
    }
    
    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("清理过期令牌");
        
        tokenRepository.deleteByExpireAtBefore(LocalDateTime.now());
        
        log.info("过期令牌清理完成");
    }
    
    @Override
    @Transactional
    public void invalidateToken(Long id) {
        log.info("使令牌失效: id={}", id);
        
        PasswordResetTokenEntity entity = tokenRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "令牌不存在"));
        
        entity.setUsed(true);
        tokenRepository.save(entity);
        
        log.info("令牌已失效: id={}", id);
    }
    
    /**
     * 转换为VO
     */
    private PasswordResetTokenVO convertToVO(PasswordResetTokenEntity entity, UserEntity user, LocalDateTime now) {
        boolean isExpired = entity.getExpireAt().isBefore(now);
        boolean isUsed = Boolean.TRUE.equals(entity.getUsed());
        
        String statusDesc;
        if (isUsed) {
            statusDesc = "已使用";
        } else if (isExpired) {
            statusDesc = "已过期";
        } else {
            statusDesc = "有效";
        }
        
        // 脱敏处理token，只显示前8位和后4位
        String maskedToken = maskToken(entity.getToken());
        
        return PasswordResetTokenVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .email(entity.getEmail())
                .token(maskedToken)
                .expireAt(entity.getExpireAt())
                .used(isUsed)
                .statusDesc(statusDesc)
                .createdAt(entity.getCreatedAt())
                .expired(isExpired)
                .build();
    }
    
    /**
     * 脱敏处理token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "****";
        }
        return token.substring(0, 8) + "****" + token.substring(token.length() - 4);
    }
}
