package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"time"

	"github.com/subhankardas/microservices-design-patterns/impl"
)

func main() {
	// 1. Setup our retry configuration
	cfg := impl.Config{
		MaxRetries:     3,
		InitialBackoff: 1 * time.Second,
		MaxBackoff:     10 * time.Second,
		Multiplier:     2.0, // Double the wait time each attempt
		Jitter:         0.2, // +/- 20% randomness
	}

	// 2. Create a context (with a timeout just in case)
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	retry := impl.NewRetryWithBackoff[string](cfg)

	// 3. Execute the operation using our retry wrapper
	result, err := retry.Do(ctx, flakyOperation())
	if err != nil {
		log.Fatalf("Operation failed after retries: %v", err)
	}

	fmt.Printf("Result: %s\n", result)
}

// flakyOperation creates a stateful operation function that simulates transient failures.
func flakyOperation() func(context.Context) (string, error) {
	var attempts = 0
	return func(ctx context.Context) (string, error) {
		attempts++
		fmt.Printf("Attempt %d...\n", attempts)

		// Simulate a failure on the first 2 attempts
		if attempts <= 2 {
			return "", errors.New("temporary network error")
		}
		return "Successfully fetched data!", nil
	}
}
