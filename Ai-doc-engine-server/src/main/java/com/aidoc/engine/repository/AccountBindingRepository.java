package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.AccountBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 账户绑定Repository
 */
@Repository
public interface AccountBindingRepository extends JpaRepository<AccountBindingEntity, Long> {
    
    /**
     * 根据用户ID查询所有绑定
     */
    List<AccountBindingEntity> findByUserId(Long userId);
    
    /**
     * 根据用户ID和绑定ID查询
     */
    Optional<AccountBindingEntity> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 根据绑定类型和绑定ID查询
     */
    Optional<AccountBindingEntity> findByBindingTypeAndBindingId(String bindingType, String bindingId);
    
    /**
     * 检查绑定是否存在
     */
    boolean existsByBindingTypeAndBindingId(String bindingType, String bindingId);
    
    /**
     * 根据用户ID和绑定类型查询
     */
    Optional<AccountBindingEntity> findByUserIdAndBindingType(Long userId, String bindingType);
}
