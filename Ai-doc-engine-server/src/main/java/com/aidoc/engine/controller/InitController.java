package com.aidoc.engine.controller;

import com.aidoc.engine.common.response.ApiResponse;
import com.aidoc.engine.model.entity.UserEntity;
import com.aidoc.engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitController {
    
    private static final String DEFAULT_PASSWORD = "lyj3401456945";
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/admin")
    public ApiResponse<Map<String, Object>> initAdmin() {
        log.info("初始化管理员账号");
        
        Map<String, Object> result = new HashMap<>();
        
        if (userRepository.findByUsername("admin").isPresent()) {
            UserEntity existingAdmin = userRepository.findByUsername("admin").get();
            result.put("message", "管理员账号已存在，请使用 /api/init/reset-admin 重置密码");
            result.put("id", existingAdmin.getId());
            result.put("username", existingAdmin.getUsername());
            result.put("role", existingAdmin.getRole());
            result.put("password", DEFAULT_PASSWORD + " (默认密码，如无法登录请重置)");
            return ApiResponse.success(result);
        }
        
        String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);
        log.info("生成的密码哈希: {}", passwordHash);
        
        UserEntity admin = UserEntity.builder()
                .username("admin")
                .email("admin@example.com")
                .passwordHash(passwordHash)
                .nickname("管理员")
                .role("ADMIN")
                .status("active")
                .build();
        
        admin = userRepository.save(admin);
        
        result.put("message", "管理员账号创建成功");
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("password", DEFAULT_PASSWORD);
        result.put("role", admin.getRole());
        
        log.info("管理员账号创建成功: id={}, username={}", admin.getId(), admin.getUsername());
        
        return ApiResponse.success(result);
    }
    
    @GetMapping("/reset-admin")
    public ApiResponse<Map<String, Object>> resetAdminPassword() {
        log.info("重置管理员密码");
        
        Map<String, Object> result = new HashMap<>();
        
        UserEntity admin = userRepository.findByUsername("admin").orElse(null);
        
        if (admin == null) {
            String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);
            admin = UserEntity.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .passwordHash(passwordHash)
                    .nickname("管理员")
                    .role("ADMIN")
                    .status("active")
                    .build();
            admin = userRepository.save(admin);
            result.put("message", "管理员账号创建成功");
        } else {
            String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);
            admin.setPasswordHash(passwordHash);
            admin.setRole("ADMIN");
            admin.setStatus("active");
            admin = userRepository.save(admin);
            result.put("message", "管理员密码重置成功");
        }
        
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("password", DEFAULT_PASSWORD);
        result.put("role", admin.getRole());
        
        log.info("管理员密码重置成功: id={}, username={}", admin.getId(), admin.getUsername());
        
        return ApiResponse.success(result);
    }
}
