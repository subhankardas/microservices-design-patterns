package com.microservices.impl;

public class TokenBucketRateLimiter {

    private final int rate; // Tokens to add per second
    private final int capacity; // Maximum number of tokens in the bucket
    private int tokens; // Current number of tokens in the bucket
    private long lastRefillTimestamp; // Timestamp of the last token refill

    public TokenBucketRateLimiter(int rate, int capacity) {
        this.rate = rate;
        this.capacity = capacity;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();

        // Calculate time elapsed since last refill
        long elapsedTime = now - lastRefillTimestamp;

        // Refill tokens based on elapsed time and rate
        int tokensToAdd = (int) (elapsedTime * rate / 1000);

        // Add tokens to the bucket, ensuring it does not exceed capacity
        if (tokensToAdd > 0) {
            tokens = Math.min(tokens + tokensToAdd, capacity);
            lastRefillTimestamp = now;
        }

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

}

// Test class to demonstrate the functionality of TokenBucketRateLimiter.
class TestTokenBucketRateLimiter {

    public static void main() throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(5, 10);

        for (int i = 1; i <= 20; i++) {
            if (rateLimiter.allowRequest()) {
                System.out.println("Request " + i + " allowed");
            } else {
                System.out.println("Request " + i + " denied");
            }
        }

        Thread.sleep(2000); // Wait to allow tokens to refill

        for (int i = 21; i <= 40; i++) {
            if (rateLimiter.allowRequest()) {
                System.out.println("Request " + i + " allowed");
            } else {
                System.out.println("Request " + i + " denied");
            }
        }
    }

}
