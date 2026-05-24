package com.aidoc.engine.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 * 配置静态资源映射，使上传的文件可以通过 URL 访问
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    /**
     * 配置静态资源映射
     * 
     * 支持两种 URL 格式：
     * 1. /uploads/** - 标准格式，如 /uploads/avatar/xxx.jpg
     * 2. /avatar/**, /feedback/**, /document/**, /temp/** - 兼容旧数据格式
     * 
     * 支持多种路径格式：
     * - 绝对路径: /var/www/uploads 或 D:/uploads
     * - 相对路径: ./uploads（相对于应用启动目录）
     * 
     * 自动处理 Windows 和 Linux 路径差异
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileStorageProperties.getUploadDir();
        
        // 将路径转换为绝对路径并规范化
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        String absolutePath = path.toString();
        
        // 构建资源位置 URL
        // Windows 路径需要转换为 file:/// 格式
        // Linux 路径使用 file:/ 格式
        String resourceLocation;
        if (absolutePath.contains(":") && !absolutePath.startsWith("file:")) {
            // Windows 路径，如 D:\\uploads -> file:///D:/uploads/
            resourceLocation = "file:///" + absolutePath.replace("\\", "/") + "/";
        } else if (!absolutePath.startsWith("file:")) {
            // Linux 路径，如 /var/uploads -> file:/var/uploads/
            resourceLocation = "file:" + absolutePath + "/";
        } else {
            // 已经是 file: 格式
            resourceLocation = absolutePath.endsWith("/") ? absolutePath : absolutePath + "/";
        }
        
        // 标准映射: /uploads/** -> uploads/
        String urlPattern = fileStorageProperties.getNormalizedUrlPrefix() + "/**";
        registry.addResourceHandler(urlPattern)
                .addResourceLocations(resourceLocation);
        log.info("静态资源映射配置: {} -> {}", urlPattern, resourceLocation);
        
        // 兼容旧数据格式: /avatar/** -> uploads/avatar/
        // /feedback/** -> uploads/feedback/
        // /document/** -> uploads/document/
        // /temp/** -> uploads/temp/
        String[] subDirs = {"avatar", "feedback", "document", "temp"};
        for (String subDir : subDirs) {
            String subDirLocation = resourceLocation + subDir + "/";
            registry.addResourceHandler("/" + subDir + "/**")
                    .addResourceLocations(subDirLocation);
            log.info("兼容静态资源映射: /{}/** -> {}", subDir, subDirLocation);
        }
        
        log.info("上传目录绝对路径: {}", absolutePath);
    }
}
