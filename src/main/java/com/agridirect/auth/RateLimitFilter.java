package com.agridirect.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token-bucket rate limiter for auth endpoints.
 * Limits each IP to 10 requests per minute on /api/auth/register, /login, /firebase, /otp/*.
 * Exceeding the limit returns HTTP 429 Too Many Requests.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000L; // 1 minute

    private static final class Bucket {
        AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = Instant.now().toEpochMilli();
    }

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!isRateLimited(path)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> new Bucket());

        long now = Instant.now().toEpochMilli();
        // Reset window if expired
        if (now - bucket.windowStart > WINDOW_MS) {
            synchronized (bucket) {
                if (now - bucket.windowStart > WINDOW_MS) {
                    bucket.count.set(0);
                    bucket.windowStart = now;
                }
            }
        }

        int current = bucket.count.incrementAndGet();
        if (current > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("success", false, "message", "Too many requests. Please wait 1 minute and try again."));
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isRateLimited(String path) {
        return path.startsWith("/api/auth/register")
            || path.startsWith("/api/auth/login")
            || path.startsWith("/api/auth/firebase")
            || path.startsWith("/api/auth/otp");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
