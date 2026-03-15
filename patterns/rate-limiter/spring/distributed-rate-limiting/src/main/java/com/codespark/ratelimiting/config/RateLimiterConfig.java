package com.codespark.ratelimiting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RateLimiterConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    RedisClient redisClient() {
        // 1. Creates a Redis client using Lettuce with the provided connection
        // details.This is used by Bucket4j to store rate-limiting data in Redis.
        return RedisClient.create(RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withPassword(redisPassword.toCharArray())
                .withSsl(false)
                .build());
    }

    @Bean
    ProxyManager<String> lettuceBasedProxyManager(RedisClient redisClient) {
        // 2. Creates a ProxyManager that Bucket4j uses to interact with Redis. It
        // establishes a connection to Redis using the provided RedisClient and
        // configures it to use String keys and byte array values for storing bucket
        // data.
        StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        return LettuceBasedProxyManager.builderFor(redisConnection)
                .build();
    }
}
