package impl

import (
	"errors"
	"sync"
	"time"
)

// ErrCircuitOpen is returned when the circuit breaker is open and requests are blocked.
var ErrCircuitOpen = errors.New("circuit breaker is open")

type State string

const (
	StateClosed   State = "closed"    // Normal operation, all requests are allowed
	StateOpen     State = "open"      // Circuit is open, requests are blocked
	StateHalfOpen State = "half-open" // Circuit is half-open, limited requests are allowed
)

type MyCircuitBreaker[T any] struct {
	mu sync.Mutex

	failureThreshold    uint          // Number of consecutive failures to trigger the circuit breaker
	timeout             time.Duration // Duration to keep the circuit breaker open before allowing attempts again
	maxHalfOpenRequests uint          // Number of requests allowed in half-open state before fully closing the circuit breaker

	state               State     // Current state of the circuit breaker
	consecutiveFailures uint      // Count of consecutive failures
	halfOpenSuccesses   uint      // Count of successful requests in half-open state
	halfOpenRequests    uint      // Count of requests made in half-open state
	lastFailureTime     time.Time // Timestamp of the last failure
}

// NewMyCircuitBreaker creates a new instance of MyCircuitBreaker with the specified settings.
func NewMyCircuitBreaker[T any](failureThreshold uint, timeout time.Duration, maxHalfOpenRequests uint) *MyCircuitBreaker[T] {
	return &MyCircuitBreaker[T]{
		failureThreshold:    failureThreshold,
		timeout:             timeout,
		maxHalfOpenRequests: maxHalfOpenRequests,
		state:               StateClosed, // Start in closed state
	}
}

// Execute runs the provided request function through the circuit breaker logic.
func (cb *MyCircuitBreaker[T]) Execute(request func() (T, error)) (T, error) {
	if err := cb.beforeRequest(); err != nil {
		var zeroValue T
		return zeroValue, err
	}

	result, err := request()
	cb.afterRequest(err == nil)

	return result, err
}

func (cb *MyCircuitBreaker[T]) beforeRequest() error {
	cb.mu.Lock()
	defer cb.mu.Unlock()

	switch cb.state {
	case StateOpen:
		// 1. Check if the timeout has elapsed to transition to half-open state
		if time.Since(cb.lastFailureTime) >= cb.timeout {
			cb.state = StateHalfOpen
			cb.halfOpenRequests = 0
			cb.halfOpenSuccesses = 0

			return nil
		}

		// 2. If still within the timeout period, block the request
		return ErrCircuitOpen
	case StateHalfOpen:
		// 3. Allow limited requests in half-open state, but block if the limit is exceeded
		if cb.halfOpenRequests >= cb.maxHalfOpenRequests {
			return ErrCircuitOpen
		}

		cb.halfOpenRequests++
		return nil
	case StateClosed:
		// 4. In closed state, allow all requests
		return nil
	}
	return nil
}

func (cb *MyCircuitBreaker[T]) afterRequest(success bool) {
	cb.mu.Lock()
	defer cb.mu.Unlock()

	if success {
		switch cb.state {
		case StateHalfOpen:
			cb.halfOpenSuccesses++

			if cb.halfOpenSuccesses >= cb.maxHalfOpenRequests {
				// 1. Transition back to closed state after successful requests in half-open state
				cb.state = StateClosed
				cb.consecutiveFailures = 0
			}
		case StateClosed:
			// 2. Reset failure count on success in closed state
			cb.consecutiveFailures = 0
		}
	} else {
		cb.lastFailureTime = time.Now()

		switch cb.state {
		case StateHalfOpen:
			// 3. Transition back to open state on failure in half-open state
			cb.state = StateOpen
			cb.consecutiveFailures = 0
		case StateClosed:
			cb.consecutiveFailures++

			// 4. Transition to open if threshold is reached
			if cb.consecutiveFailures >= cb.failureThreshold {
				cb.state = StateOpen
			}
		}
	}
}
