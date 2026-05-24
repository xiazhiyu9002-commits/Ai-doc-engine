package com.aidoc.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mermaid 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.mermaid")
public class MermaidProperties {
    
    /**
     * mmdc 命令路径
     * Windows: mmdc.cmd 或完整路径
     * Linux/Mac: mmdc
     */
    private String command = "mmdc.cmd";
    
    /**
     * 渲染超时时间（秒）
     */
    private Integer timeout = 30;
    
    /**
     * 背景色
     */
    private String background = "transparent";
    
    /**
     * 主题
     */
    private String theme = "default";
}
