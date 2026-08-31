package com.simpatico.crm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.simpatico.crm.dto.ErrorResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter implementing IP-based rate limiting on public landing page endpoints.
 */
@Component
public class RateLimitingFilter implements Filter {

    @Value("${app.rate-limiting.enabled}")
    private boolean enabled;

    @Value("${app.rate-limiting.requests-per-minute}")
    private int requestsPerMinute;

    private final Map<String, List<Instant>> requestTimestamps = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (enabled && httpRequest.getRequestURI().startsWith("/api/public/")) {
            String clientIp = getClientIp(httpRequest);
            Instant now = Instant.now();

            List<Instant> timestamps = requestTimestamps.computeIfAbsent(clientIp, k -> Collections.synchronizedList(new ArrayList<>()));

            synchronized (timestamps) {
                // Remove timestamps older than 1 minute
                Instant oneMinuteAgo = now.minusSeconds(60);
                timestamps.removeIf(timestamp -> timestamp.isBefore(oneMinuteAgo));

                if (timestamps.size() >= requestsPerMinute) {
                    httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    ErrorResponse errorResponse = new ErrorResponse(
                            OffsetDateTime.now(),
                            HttpStatus.TOO_MANY_REQUESTS.value(),
                            HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                            "Rate limit exceeded. Maximum " + requestsPerMinute + " requests per minute allowed.",
                            httpRequest.getRequestURI(),
                            null
                    );
                    objectMapper.writeValue(httpResponse.getWriter(), errorResponse);
                    return;
                }

                timestamps.add(now);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Clear all tracked rate limit records. Useful for testing.
     */
    public void clearRateLimits() {
        requestTimestamps.clear();
    }

    /**
     * Resolve the source client IP, supporting downstream proxy X-Forwarded-For headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
