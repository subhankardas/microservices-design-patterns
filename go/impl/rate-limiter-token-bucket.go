package impl

import (
	"fmt"
	"sync"
	"time"
)

type TokenBucket struct {
	rate       uint       // Tokens added per second
	capacity   uint       // Maximum number of tokens in the bucket
	tokens     uint       // Current number of tokens available
	lastRefill time.Time  // Last time the bucket was refilled
	mu         sync.Mutex // Mutex to protect concurrent access to the bucket
}

func NewTokenBucket(rate, capacity uint) *TokenBucket {
	return &TokenBucket{
		rate:       rate,
		capacity:   capacity,
		tokens:     capacity, // Start with a full bucket
		lastRefill: time.Now(),
	}
}

func (tb *TokenBucket) Allow() bool {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	now := time.Now()

	// 1. Calculate the time elapsed since the last refill
	elapsed := now.Sub(tb.lastRefill).Seconds()

	// 2. Calculate how many tokens to add based on the elapsed time and rate
	tokensToAdd := uint(elapsed * float64(tb.rate))

	// 3. Update the token count, but don't exceed the capacity
	tb.tokens = min(tb.tokens+tokensToAdd, tb.capacity)
	tb.lastRefill = now

	// 4. Check if we have enough tokens to allow the request
	if tb.tokens >= 1 {
		tb.tokens--
		return true
	}

	return false
}

func TestTokenBucketRateLimiter() {
	rate := uint(5)
	capacity := uint(10)
	limiter := NewTokenBucket(rate, capacity)

	// Simulate making requests
	for i := 1; i <= 20; i++ {
		if limiter.Allow() {
			fmt.Printf("Request %2d: ✅ Allowed at %v\n", i, time.Now().Format("15:04:05.000"))
		} else {
			fmt.Printf("Request %2d: ❌ Blocked at %v\n", i, time.Now().Format("15:04:05.000"))
		}
		time.Sleep(100 * time.Millisecond) // Simulate time between requests
	}

	time.Sleep(3 * time.Second) // Wait to allow tokens to refill

	// Test again after waiting for tokens to refill
	for i := 21; i <= 30; i++ {
		if limiter.Allow() {
			fmt.Printf("Request %2d: ✅ Allowed at %v\n", i, time.Now().Format("15:04:05.000"))
		} else {
			fmt.Printf("Request %2d: ❌ Blocked at %v\n", i, time.Now().Format("15:04:05.000"))
		}
		time.Sleep(100 * time.Millisecond) // Simulate time between requests
	}
}
