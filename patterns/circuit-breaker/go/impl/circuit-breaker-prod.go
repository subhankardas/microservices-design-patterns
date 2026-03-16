package impl

import (
	"time"

	"github.com/sony/gobreaker"
)

// Default circuit breaker settings
const (
	DefaultMaxRequests = 3                // Allow 3 requests in half-open state before fully closing
	DefaultInterval    = 5 * time.Second  // Clear counts after 5 seconds
	DefaultTimeout     = 10 * time.Second // Open state duration
)

type CircuitBreaker struct {
	cb *gobreaker.CircuitBreaker
}

// NewCircuitBreaker creates a new circuit breaker with default settings.
func NewCircuitBreaker() *CircuitBreaker {
	settings := gobreaker.Settings{
		Name:        "DefaultCircuitBreaker",
		MaxRequests: DefaultMaxRequests,
		Interval:    DefaultInterval,
		Timeout:     DefaultTimeout,
	}
	return &CircuitBreaker{
		cb: gobreaker.NewCircuitBreaker(settings),
	}
}

// Execute runs a request function through the circuit breaker.
func (c *CircuitBreaker) Execute(request func() (interface{}, error)) (interface{}, error) {
	return c.cb.Execute(request)
}
