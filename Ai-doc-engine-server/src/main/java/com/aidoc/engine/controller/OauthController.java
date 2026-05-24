package com.aidoc.engine.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aidoc.engine.common.OauthProperties;
import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.dto.auth.SchoolOAuthUserInfo;
import com.aidoc.engine.model.vo.auth.AuthResponse;
import com.aidoc.engine.service.AuthService;
import com.aidoc.engine.service.OAuthLoginTicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class OauthController {

    private static final String STATE_COOKIE = "AI_DOC_OAUTH_STATE";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final OauthProperties oauthProperties;
    private final AuthService authService;
    private final OAuthLoginTicketService oauthLoginTicketService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public OauthController(OauthProperties oauthProperties,
                           AuthService authService,
                           OAuthLoginTicketService oauthLoginTicketService) {
        this.oauthProperties = oauthProperties;
        this.authService = authService;
        this.oauthLoginTicketService = oauthLoginTicketService;
    }

    /**
     * 跳转到学校统一登录页
     */
    @GetMapping("/oauth/login")
    public String toLogin(HttpServletResponse response) {
        log.info("内网统一身份证认证.....................");
        try {
            String state = UUID.randomUUID().toString().replace("-", "");
            ResponseCookie cookie = ResponseCookie.from(STATE_COOKIE, state)
                    .httpOnly(true)
                    .path("/oauth")
                    .maxAge(STATE_TTL)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            String url = UriComponentsBuilder
                    .fromHttpUrl(joinUrl(oauthProperties.getAuthHost(), "/oauth2/authorize"))
                    .queryParam("response_type", "code")
                    .queryParam("client_id", oauthProperties.getClientId())
                    .queryParam("scope", "userinfo")
                    .queryParam("state", state)
                    .queryParam("redirect_uri", oauthProperties.getRedirectUri())
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            return "redirect:" + url;
        } catch (Exception e) {
            log.error("构建学校统一登录地址失败", e);
            return redirectToFrontendError("统一登录暂时不可用，请稍后重试");
        }
    }

    /**
     * 登录回调：code 换取学校用户信息，然后签发本系统的一次性登录票据。
     */
    @GetMapping("/oauth/callback")
    public String callback(@RequestParam(value = "code", required = false) String code,
                           @RequestParam(value = "state", required = false) String state,
                           @CookieValue(value = STATE_COOKIE, required = false) String expectedState,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        clearStateCookie(response);

        if (!StringUtils.hasText(code)) {
            return redirectToFrontendError("授权失败，请重新发起统一登录");
        }

        if (!StringUtils.hasText(state) || !state.equals(expectedState)) {
            log.warn("学校统一登录 state 校验失败: state={}, expected={}", state, expectedState);
            return redirectToFrontendError("登录状态已失效，请重新发起统一登录");
        }

        try {
            SchoolOAuthUserInfo schoolUser = exchangeCodeForSchoolUser(code);
            AuthResponse authResponse = authService.loginBySchoolOAuth(
                    schoolUser,
                    getClientIp(request),
                    request.getHeader("User-Agent")
            );
            String ticket = oauthLoginTicketService.issue(authResponse);
            return redirectToFrontendTicket(ticket);
        } catch (BusinessException e) {
            log.warn("学校统一登录失败: {}", e.getMessage());
            return redirectToFrontendError(e.getMessage());
        } catch (Exception e) {
            log.error("学校统一登录异常", e);
            return redirectToFrontendError("统一登录失败，请稍后重试");
        }
    }

    /**
     * 退出入口保留给旧链接使用。真正的系统退出由前端清理 JWT 完成。
     */
    @GetMapping("/oauth/logout")
    public String logout() {
        return "redirect:" + normalizeFrontendUrl() + "/workbench";
    }

    private SchoolOAuthUserInfo exchangeCodeForSchoolUser(String code) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", code);
        form.put("client_id", oauthProperties.getClientId());
        form.put("client_secret", oauthProperties.getClientSecret());
        form.put("redirect_uri", oauthProperties.getRedirectUri());

        String result = HttpRequest.post(joinUrl(oauthProperties.getAuthHost(), "/oauth2/token"))
                .timeout(resolveRequestTimeout())
                .form(form)
                .execute()
                .body();

        JSONObject json = JSONUtil.parseObj(result);
        Integer resultCode = json.getInt("code");
        if (resultCode != null && resultCode != 200) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, firstNonBlank(json.getStr("msg"), "学校认证失败"));
        }

        JSONObject data = json.getJSONObject("data");
        String userNo = firstNonBlank(
                getString(json, data, "user"),
                getString(json, data, "userNo"),
                getString(json, data, "studentNo"),
                getString(json, data, "employeeNo"),
                getString(json, data, "username"),
                getString(json, data, "account")
        );

        if (!StringUtils.hasText(userNo)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "学校认证未返回账号信息");
        }

        return SchoolOAuthUserInfo.builder()
                .userNo(userNo)
                .userName(firstNonBlank(
                        getString(json, data, "userName"),
                        getString(json, data, "name"),
                        getString(json, data, "realName"),
                        getString(json, data, "displayName"),
                        userNo
                ))
                .email(firstNonBlank(getString(json, data, "email"), getString(json, data, "mail")))
                .build();
    }

    private String getString(JSONObject json, JSONObject data, String key) {
        String value = json.getStr(key);
        if (!StringUtils.hasText(value) && data != null) {
            value = data.getStr(key);
        }
        return value;
    }

    private int resolveRequestTimeout() {
        Integer timeout = oauthProperties.getRequestTimeoutMillis();
        return timeout == null || timeout <= 0 ? 10000 : timeout;
    }

    private void clearStateCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(STATE_COOKIE, "")
                .httpOnly(true)
                .path("/oauth")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String redirectToFrontendTicket(String ticket) {
        return "redirect:" + normalizeFrontendUrl() + "/oauth/callback?ticket=" + ticket;
    }

    private String redirectToFrontendError(String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "redirect:" + normalizeFrontendUrl() + "/oauth/callback?error=" + encodedMessage;
    }

    private String normalizeFrontendUrl() {
        if (!StringUtils.hasText(frontendUrl)) {
            return "http://localhost:3000";
        }

        String value = frontendUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl == null ? "" : baseUrl.trim();
        while (normalizedBase.endsWith("/")) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        return normalizedBase + path;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
