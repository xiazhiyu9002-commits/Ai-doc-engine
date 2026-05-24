package com.aidoc.engine.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.ocr")
public class AppOcrProperties {

    private String provider;

    private String apiUrl;

    private String apiKey;

    private String appId;

    private String appKey;

    private Integer timeout;
}
