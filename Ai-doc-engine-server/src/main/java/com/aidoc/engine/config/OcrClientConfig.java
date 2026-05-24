package com.aidoc.engine.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class OcrClientConfig {

    private final AppOcrProperties appOcrProperties;

    @Bean
    public RestTemplate ocrRestTemplate(RestTemplateBuilder builder) {
        Integer timeoutMs = appOcrProperties.getTimeout();
        Duration timeout = Duration.ofMillis(timeoutMs != null ? timeoutMs : 30000);
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
