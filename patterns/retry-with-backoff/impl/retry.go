package impl

import (
	"context"
	"math/rand/v2"
	"time"
)

// Config holds the configuration for the retry mechanism.
type Config struct {
	MaxRetries     uint          // Maximum number of retry attempts
	InitialBackoff time.Duration // Starting wait time (e.g., 100ms)
	MaxBackoff     time.Duration // Maximum wait time (e.g., 5s)
	Multiplier     float64       // How much to multiply the backoff by (usually 2.0)
	Jitter         float64       // Randomness factor between 0.0 and 1.0
}

// Retry encapsulates the logic for retrying an operation with exponential backoff.
type Retry[T any] struct {
	config Config
}

// NewRetryWithBackoff creates a new Retry instance with the given configuration.
func NewRetryWithBackoff[T any](cfg Config) *Retry[T] {
	return &Retry[T]{
		config: cfg,
	}
}

// Do executes the given operation, retrying with exponential backoff if it fails.
func (r *Retry[T]) Do(ctx context.Context, operation func(ctx context.Context) (T, error)) (T, error) {
	var result T
	var err error

	currentBackoff := float64(r.config.InitialBackoff)

	for attempt := uint(0); attempt <= r.config.MaxRetries; attempt++ {
		// 1. Attempt the operation
		result, err = operation(ctx)
		if err == nil {
			return result, nil // Success! Return immediately.
		}

		// 2. If it's the last attempt, don't wait, just break and return the error
		if attempt == r.config.MaxRetries {
			break
		}

		// 3. Calculate next wait time with Jitter
		// Example: If currentBackoff is 1s and Jitter is 0.2, wait time will be between 0.8s and 1.2s
		jitterVariation := currentBackoff * r.config.Jitter
		minWait := currentBackoff - jitterVariation
		maxWait := currentBackoff + jitterVariation

		waitTime := minWait + rand.Float64()*(maxWait-minWait)

		// 4. Enforce the absolute maximum backoff limit
		if time.Duration(waitTime) > r.config.MaxBackoff {
			waitTime = float64(r.config.MaxBackoff)
		}

		// 5. Wait before the next attempt, or abort if the context is cancelled
		select {
		case <-time.After(time.Duration(waitTime)):
			// Increase the backoff exponentially for the next loop
			currentBackoff *= r.config.Multiplier
		case <-ctx.Done():
			// The operation was cancelled by the caller
			return result, ctx.Err()
		}
	}

	return result, err // Return the last error encountered
}
