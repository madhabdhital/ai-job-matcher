package com.jobmatcher.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection for the auth endpoints, per client IP:
 *  - login: 10 FAILED attempts per 15 minutes, then blocked until the window passes
 *  - register: 10 attempts per hour
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final long MINUTE = 60_000L;

    private final Limiter loginFailures = new Limiter(10, 15 * MINUTE);
    private final Limiter registrations = new Limiter(10, 60 * MINUTE);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !"POST".equals(request.getMethod())
                || !(LOGIN_PATH.equals(path) || REGISTER_PATH.equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        boolean login = LOGIN_PATH.equals(request.getRequestURI());
        Limiter limiter = login ? loginFailures : registrations;

        long waitSeconds = limiter.secondsUntilAllowed(ip);

        if (waitSeconds > 0) {
            long minutes = (waitSeconds + 59) / 60;

            reject(response, waitSeconds, (login
                    ? "Too many failed login attempts."
                    : "Too many sign-up attempts.")
                    + " Please try again in " + minutes + " minute(s).");
            return;
        }

        if (!login) {
            limiter.record(ip); // every sign-up attempt counts
        }

        filterChain.doFilter(request, response);

        if (login) {
            int status = response.getStatus();

            if (status == HttpServletResponse.SC_UNAUTHORIZED) {
                limiter.record(ip); // wrong password: count it
            } else if (status == HttpServletResponse.SC_OK) {
                limiter.reset(ip);  // successful login clears the counter
            }
        }
    }

    private void reject(HttpServletResponse response, long waitSeconds, String message)
            throws IOException {

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}");
    }

    /** Sliding-window counter, kept in memory. */
    private static class Limiter {

        private final int max;
        private final long windowMs;
        private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

        Limiter(int max, long windowMs) {
            this.max = max;
            this.windowMs = windowMs;
        }

        long secondsUntilAllowed(String key) {
            Deque<Long> queue = hits.get(key);

            if (queue == null) {
                return 0;
            }

            synchronized (queue) {
                prune(queue);

                if (queue.size() < max) {
                    return 0;
                }

                long oldest = queue.peekFirst();
                long remainingMs = oldest + windowMs - System.currentTimeMillis();

                return Math.max(1, (remainingMs + 999) / 1000);
            }
        }

        void record(String key) {
            if (hits.size() > 10_000) {
                purgeStale();
            }

            Deque<Long> queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());

            synchronized (queue) {
                prune(queue);
                queue.addLast(System.currentTimeMillis());
            }
        }

        void reset(String key) {
            hits.remove(key);
        }

        private void prune(Deque<Long> queue) {
            long cutoff = System.currentTimeMillis() - windowMs;

            while (!queue.isEmpty() && queue.peekFirst() < cutoff) {
                queue.pollFirst();
            }
        }

        private void purgeStale() {
            hits.entrySet().removeIf(entry -> {
                synchronized (entry.getValue()) {
                    prune(entry.getValue());
                    return entry.getValue().isEmpty();
                }
            });
        }
    }
}