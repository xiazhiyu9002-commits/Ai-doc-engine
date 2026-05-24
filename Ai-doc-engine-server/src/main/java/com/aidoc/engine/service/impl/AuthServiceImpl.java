package com.aidoc.engine.service.impl;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.auth.LoginRequest;
import com.aidoc.engine.model.dto.auth.RegisterRequest;
import com.aidoc.engine.model.dto.auth.SchoolOAuthUserInfo;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.model.vo.auth.AuthResponse;
import com.aidoc.engine.model.vo.auth.UserVO;
import com.aidoc.engine.repository.UserRepository;
import com.aidoc.engine.security.JwtTokenProvider;
import com.aidoc.engine.service.AuthService;
import com.aidoc.engine.service.EmailService;
import com.aidoc.engine.service.LoginLogService;
import com.aidoc.engine.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetService passwordResetService;
    private final LoginLogService loginLogService;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("用户注册: username={}, email={}", request.getUsername(), request.getEmail());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS, "邮箱已存在");
        }
        
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .status("active")
                .build();
        
        user = userRepository.save(user);
        
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            log.warn("发送欢迎邮件失败，但不影响注册: {}", e.getMessage());
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        UserVO userVO = buildUserVO(user);
        
        log.info("用户注册成功: userId={}", user.getId());
        
        return AuthResponse.builder()
                .token(token)
                .user(userVO)
                .build();
    }
    
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String usernameOrEmail = request.getUsername();
        log.info("用户登录: usernameOrEmail={}, ip={}", usernameOrEmail, ipAddress);
        
        if (loginLogService.shouldLockAccount(usernameOrEmail, ipAddress)) {
            loginLogService.logLoginFailure(usernameOrEmail, ipAddress, userAgent, "登录失败次数过多，账号已锁定");
            throw new BusinessException(ErrorCode.USER_DISABLED, "登录失败次数过多，请15分钟后再试");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            UserEntity user = userRepository.findByUsername(usernameOrEmail)
                    .or(() -> userRepository.findByEmail(usernameOrEmail))
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
            
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            userRepository.save(user);
            
            loginLogService.logLoginSuccess(user.getId(), user.getUsername(), ipAddress, userAgent);
            
            String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
            
            UserVO userVO = buildUserVO(user);
            
            log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
            
            return AuthResponse.builder()
                    .token(token)
                    .user(userVO)
                    .build();
                    
        } catch (Exception e) {
            loginLogService.logLoginFailure(usernameOrEmail, ipAddress, userAgent, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse loginBySchoolOAuth(SchoolOAuthUserInfo schoolUser, String ipAddress, String userAgent) {
        if (schoolUser == null || !StringUtils.hasText(schoolUser.getUserNo())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "学校账号信息缺失");
        }

        String schoolUserNo = schoolUser.getUserNo().trim();
        String username = buildSchoolUsername(schoolUserNo);
        log.info("学校统一登录: userNo={}, username={}, ip={}", schoolUserNo, username, ipAddress);

        UserEntity user = userRepository.findByUsername(username)
                .orElseGet(() -> createSchoolOAuthUser(username, schoolUser));

        if (!"active".equals(user.getStatus())) {
            loginLogService.logLoginFailure(username, ipAddress, userAgent, "账号已禁用", "SCHOOL_OAUTH");
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已禁用，请联系管理员");
        }

        String displayName = firstNonBlank(schoolUser.getUserName(), user.getNickname(), schoolUserNo);
        if (StringUtils.hasText(displayName) && !displayName.equals(user.getNickname())) {
            user.setNickname(displayName);
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user = userRepository.save(user);

        loginLogService.logLoginSuccess(user.getId(), user.getUsername(), ipAddress, userAgent, "SCHOOL_OAUTH");

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        log.info("学校统一登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .user(buildUserVO(user))
                .build();
    }
    
    @Override
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        
        return buildUserVO(user);
    }
    
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("用户退出登录");
    }
    
    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("处理忘记密码请求: email={}", email);
        passwordResetService.requestPasswordReset(email);
    }
    
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("处理重置密码请求");
        passwordResetService.resetPassword(token, newPassword);
    }
    
    private UserVO buildUserVO(UserEntity user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .department(user.getDepartment())
                .role(user.getRole() != null ? user.getRole() : "USER")
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserEntity createSchoolOAuthUser(String username, SchoolOAuthUserInfo schoolUser) {
        String email = resolveSchoolUserEmail(username, schoolUser.getEmail());

        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(firstNonBlank(schoolUser.getUserName(), schoolUser.getUserNo()))
                .role("USER")
                .status("active")
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("自动创建学校统一登录用户: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    private String resolveSchoolUserEmail(String username, String schoolEmail) {
        if (StringUtils.hasText(schoolEmail) && !userRepository.existsByEmail(schoolEmail.trim())) {
            return schoolEmail.trim();
        }
        return username + "@school.oauth.local";
    }

    private String buildSchoolUsername(String schoolUserNo) {
        String normalized = schoolUserNo.trim().replaceAll("[^A-Za-z0-9_.-]", "_");
        if (!StringUtils.hasText(normalized)) {
            normalized = "user";
        }

        String prefix = "school_";
        int maxSuffixLength = 64 - prefix.length();
        if (normalized.length() > maxSuffixLength) {
            String hash = Integer.toHexString(schoolUserNo.hashCode());
            int keepLength = Math.max(1, maxSuffixLength - hash.length() - 1);
            normalized = normalized.substring(0, keepLength) + "_" + hash;
        }

        return prefix + normalized;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }
}
