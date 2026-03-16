package services

import (
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/subhankardas/microservices-design-patterns/impl"
)

const (
	defaultHTTPTimeout = 2 * time.Second
	maxResponseSize    = 10 * 1024 * 1024 // 10MB max response size
)

var httpClient = &http.Client{
	Timeout: defaultHTTPTimeout,
}

// GetRequestWithCircuitBreaker makes a request to the given URL using the provided circuit breaker.
func GetRequestWithCircuitBreaker(cb *impl.CircuitBreaker, url string) (string, error) {
	result, err := cb.Execute(func() (interface{}, error) {
		return get(url)
	})
	if err != nil {
		return "", fmt.Errorf("request failed: %w", err)
	}
	return result.(string), nil
}

func get(url string) (string, error) {
	resp, err := httpClient.Get(url)
	if err != nil {
		return "", fmt.Errorf("failed to make request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("received non-OK status: %s", resp.Status)
	}

	// Read the response body with size limit to prevent memory exhaustion
	limitedReader := io.LimitReader(resp.Body, maxResponseSize)
	body, err := io.ReadAll(limitedReader)
	if err != nil {
		return "", fmt.Errorf("failed to read response: %w", err)
	}

	return string(body), nil
}
