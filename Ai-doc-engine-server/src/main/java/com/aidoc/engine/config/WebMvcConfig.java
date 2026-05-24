package com.aidoc.engine.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置静态资源映射，使上传的文件可以通过 URL 访问
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    /**
     * 配置静态资源映射
     * 将 /uploads/** 映射到实际的文件存储目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileStorageProperties.getUploadDir();
        
        // 确保 Windows 路径格式正确
        String resourceLocation = uploadDir.startsWith("file:") 
                ? uploadDir 
                : "file:" + uploadDir + "/";
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
        
        // 添加日志便于调试
        System.out.println("静态资源映射配置: /uploads/** -> " + resourceLocation);
    }
}
