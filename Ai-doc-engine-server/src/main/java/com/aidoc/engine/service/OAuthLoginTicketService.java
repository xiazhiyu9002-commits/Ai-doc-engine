package com.aidoc.engine.service;

import com.aidoc.engine.common.exception.BusinessException;
import com.aidoc.engine.common.exception.ErrorCode;
import com.aidoc.engine.model.vo.auth.AuthResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived one-time ticket used to hand OAuth login results to the SPA.
 */
@Service
public class OAuthLoginTicketService {

    private static final Duration TICKET_TTL = Duration.ofMinutes(2);

    private final Map<String, TicketPayload> tickets = new ConcurrentHashMap<>();

    public String issue(AuthResponse authResponse) {
        cleanupExpiredTickets();

        String ticket = UUID.randomUUID().toString().replace("-", "");
        tickets.put(ticket, new TicketPayload(authResponse, Instant.now().plus(TICKET_TTL)));
        return ticket;
    }

    public AuthResponse consume(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "OAuth 登录票据不能为空");
        }

        TicketPayload payload = tickets.remove(ticket);
        if (payload == null || payload.expiresAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "OAuth 登录票据无效或已过期");
        }

        return payload.authResponse();
    }

    private void cleanupExpiredTickets() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, TicketPayload>> iterator = tickets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TicketPayload> entry = iterator.next();
            if (entry.getValue().expiresAt().isBefore(now)) {
                iterator.remove();
            }
        }
    }

    private record TicketPayload(AuthResponse authResponse, Instant expiresAt) {
    }
}
