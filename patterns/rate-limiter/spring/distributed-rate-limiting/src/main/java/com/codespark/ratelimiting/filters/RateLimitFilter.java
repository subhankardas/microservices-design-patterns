package com.codespark.ratelimiting.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codespark.ratelimiting.service.RateLimitingService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitingService rateLimitingService;

    public RateLimitFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Identify the client
        String clientId = request.getHeader("X-API-KEY");
        if (clientId == null) {
            clientId = request.getRemoteAddr(); // Fallback to IP address if no API key is provided
        }

        // 2. Resolve bucket
        Bucket bucket = rateLimitingService.resolveBucket(clientId);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            logger.info("Request allowed for client: {}, remaining tokens: {}", clientId, probe.getRemainingTokens());

            // 3. Allow the request to continue down the chain to the Controller
            filterChain.doFilter(request, response);
        } else {
            // 4. Block the request immediately
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            logger.warn("Request blocked for client: {}, retry after: {} seconds", clientId, waitForRefill);

            // Since we are outside Spring MVC, we write the error directly to the response
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Try again later.\"}");
        }
    }
}