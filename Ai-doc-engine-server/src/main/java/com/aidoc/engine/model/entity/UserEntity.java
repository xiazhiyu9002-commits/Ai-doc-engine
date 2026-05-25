package com.aidoc.engine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    
    @Column(nullable = false, unique = true, length = 128)
    private String email;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(length = 64)
    private String nickname;
    
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    @Column(length = 100)
    private String department;
    
    @Column(length = 20)
    @Builder.Default
    private String role = "USER";
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "active";
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;
    
    @Column(name = "ocr_count", nullable = false)
    @Builder.Default
    private Integer ocrCount = 0;
    
    @Column(name = "export_count", nullable = false)
    @Builder.Default
    private Integer exportCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
