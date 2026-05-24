package com.aidoc.engine.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 账户绑定VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBindingVO {
    
    private Long id;
    
    /**
     * 绑定类型：SCHOOL_OAUTH-学校统一认证, WECHAT-微信, EMAIL-邮箱等
     */
    private String bindingType;
    
    /**
     * 绑定标识（如学校账号、微信openid等）
     */
    private String bindingId;
    
    /**
     * 绑定显示名称
     */
    private String displayName;
    
    /**
     * 绑定时间
     */
    private LocalDateTime boundAt;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;
}
