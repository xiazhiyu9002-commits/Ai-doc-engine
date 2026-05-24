package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.NotificationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 通知设置Repository
 */
@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettingsEntity, Long> {
    
    /**
     * 根据用户ID查询通知设置
     */
    Optional<NotificationSettingsEntity> findByUserId(Long userId);
    
    /**
     * 检查用户通知设置是否存在
     */
    boolean existsByUserId(Long userId);
}
