package com.aidoc.engine.service;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 增加用户OCR使用次数
     * @param userId 用户ID
     */
    void incrementOcrCount(Long userId);
    
    /**
     * 增加用户导出次数
     * @param userId 用户ID
     */
    void incrementExportCount(Long userId);
}
