package com.aidoc.engine.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authHost;
    private Integer requestTimeoutMillis = 10000;

    // 自动生成 getter/setter
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public String getAuthHost() { return authHost; }
    public void setAuthHost(String authHost) { this.authHost = authHost; }
    public Integer getRequestTimeoutMillis() { return requestTimeoutMillis; }
    public void setRequestTimeoutMillis(Integer requestTimeoutMillis) { this.requestTimeoutMillis = requestTimeoutMillis; }
}
