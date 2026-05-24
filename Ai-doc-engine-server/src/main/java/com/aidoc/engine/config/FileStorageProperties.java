package com.aidoc.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 文件存储配置类
 * 
 * 集中管理所有文件路径配置，便于维护和修改。
 * 配置项通过 application.yml 中的 app.file 前缀绑定。
 * 
 * 修改路径配置指南：
 * 1. 修改 application.yml 中的 app.file.* 配置项
 * 2. 如需添加新的子目录，在 SubDir 内部类中添加常量
 * 3. 重启应用后生效
 * 
 * 目录结构说明：
 * uploads/
 * ├── avatar/          # 用户头像
 * ├── feedback/        # 用户反馈附件
 * └── [其他子目录]/    # 按需扩展
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {
    
    /**
     * 基础上传目录
     * 默认值: ../uploads (项目根目录的上一级)
     * 生产环境建议使用绝对路径，如: /var/www/uploads
     */
    private String uploadDir = "./uploads";
    
    /**
     * URL访问前缀
     * 用于生成文件的访问URL，默认为 /uploads
     * 需要与 WebConfig 中配置的静态资源路径一致
     */
    private String urlPrefix = "/uploads";
    
    /**
     * 反馈文件目录（相对于 uploadDir）
     */
    private String feedbackDir = "feedback";
    
    /**
     * 文件大小限制（字节）
     * 默认: 10MB
     */
    private Long maxSize = 10485760L;
    
    /**
     * 图片大小限制（字节）
     * 默认: 5MB
     */
    private Long imageMaxSize = 5242880L;
    
    /**
     * 允许的文件扩展名（逗号分隔）
     */
    private String allowedExtensions = "jpg,jpeg,png,gif,webp";
    
    /**
     * 单次上传最大图片数量
     */
    private Integer maxImageCount = 5;
    
    /**
     * 子目录常量定义
     * 
     * 用途：统一管理所有上传文件的子目录名称
     * 好处：避免硬编码，便于统一修改
     * 
     * 使用方式：
     *   String avatarDir = FileStorageProperties.SubDir.AVATAR;
     *   fileStorageService.storeFile(file, avatarDir);
     */
    public static class SubDir {
        /** 用户头像目录 */
        public static final String AVATAR = "avatar";
        /** 用户反馈附件目录 */
        public static final String FEEDBACK = "feedback";
        /** 文档图片目录 */
        public static final String DOCUMENT = "document";
        /** 临时文件目录 */
        public static final String TEMP = "temp";
    }
    
    /**
     * 获取允许的扩展名列表
     */
    public List<String> getAllowedExtensionList() {
        return Arrays.asList(allowedExtensions.toLowerCase().split(","));
    }
    
    /**
     * 获取完整的反馈目录路径
     */
    public String getFullFeedbackPath() {
        return uploadDir + "/" + feedbackDir;
    }
    
    /**
     * 获取完整的URL前缀（确保以/开头）
     */
    public String getNormalizedUrlPrefix() {
        if (urlPrefix == null || urlPrefix.isEmpty()) {
            return "/uploads";
        }
        return urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
    }
}
