package com.codespark.ratelimiting.service;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;

@Service
public class RateLimitingService {

    // ProxyManager acts as the bridge between Bucket4j and Redis
    private final ProxyManager<String> proxyManager;

    private static final int REQUESTS_PER_MINUTE = 10;

    public RateLimitingService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String key) {
        Supplier<BucketConfiguration> configSupplier = getConfigSupplierForUser(key);

        // Builds a new bucket in Redis if it doesn't exist, or retrieves the existing.
        return proxyManager.builder().build(key, configSupplier);
    }

    private Supplier<BucketConfiguration> getConfigSupplierForUser(String key) {
        // In a real app, you might look up the user's tier in a DB/cache using the key
        // for fetching their rate limit configuration. For demonstration, everyone gets
        // 10 requests per minute. Greedy refill strategy: all tokens are refilled at
        // once after the specified duration.
        return () -> BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(REQUESTS_PER_MINUTE)
                        .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }
}