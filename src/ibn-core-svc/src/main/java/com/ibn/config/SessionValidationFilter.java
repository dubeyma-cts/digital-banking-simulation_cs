package com.ibn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibn.core.entity.Session;
import com.ibn.dao.SessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.session-tracker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SessionValidationFilter extends OncePerRequestFilter {

    private static final long INACTIVITY_MINUTES = 3L;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_REVOKED = "REVOKED";

    private final SessionDao sessionDao;
    private final ObjectMapper objectMapper;

    @Autowired
    public SessionValidationFilter(SessionDao sessionDao, ObjectMapper objectMapper) {
        this.sessionDao = sessionDao;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return true;
        }

        return "/api/auth/login".equals(path)
            || "/api/auth/banker-login".equals(path)
                || "/api/auth/validate-captcha".equals(path)
                || "/api/auth/health".equals(path)
                || "/api/customers/save".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request.getHeader("Authorization"));
        if (token.isEmpty()) {
            writeUnauthorized(response, "Missing session token. Please login again.");
            return;
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(token);
        } catch (Exception ex) {
            writeUnauthorized(response, "Invalid session token. Please login again.");
            return;
        }

        Optional<Session> sessionOptional = sessionDao.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            writeUnauthorized(response, "Session not found. Please login again.");
            return;
        }

        Session session = sessionOptional.get();
        if (!STATUS_ACTIVE.equalsIgnoreCase(session.getStatus())) {
            writeUnauthorized(response, "Session is no longer active. Please login again.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSeen = session.getLastSeenAt() == null ? session.getCreatedAt() : session.getLastSeenAt();
        LocalDateTime inactivityDeadline = lastSeen == null ? now : lastSeen.plusMinutes(INACTIVITY_MINUTES);

        if ((session.getExpiresAt() != null && now.isAfter(session.getExpiresAt())) || now.isAfter(inactivityDeadline)) {
            session.setStatus(STATUS_EXPIRED);
            session.setRevokedReason("Inactivity timeout (3 minutes)");
            session.setExpiresAt(now);
            sessionDao.save(session);
            writeUnauthorized(response, "Session expired due to inactivity. Please login again.");
            return;
        }

        session.setLastSeenAt(now);
        session.setExpiresAt(now.plusMinutes(INACTIVITY_MINUTES));
        sessionDao.save(session);

        request.setAttribute("sessionId", session.getSessionId() == null ? "" : session.getSessionId().toString());
        request.setAttribute("userId", session.getUserId() == null ? "" : session.getUserId().toString());

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return "";
        }
        String value = authorizationHeader.trim();
        if (!value.toLowerCase().startsWith("bearer ")) {
            return "";
        }
        return value.substring(7).trim();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
