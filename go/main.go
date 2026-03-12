package main

import "github.com/subhankardas/microservices-design-patterns/impl"

func main() {
	// --- Test the rate limiter by making requests ---//
	// go impl.StartRateLimitedServer()
	// impl.TestRateLimiter()

	// --- Test the token bucket rate limiter ---//
	impl.TestTokenBucketRateLimiter()
}
