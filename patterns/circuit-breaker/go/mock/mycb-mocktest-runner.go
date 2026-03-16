package mock

import (
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/subhankardas/microservices-design-patterns/impl"
)

func MyCircuitBreakerMockTest() {
	// Testing my custom circuit breaker implementation
	cb := impl.NewMyCircuitBreaker[string](3, 5*time.Second, 2)

	// Simulate making requests to the server
	url := "http://localhost:8080/hello"
	for i := 0; i < 5; i++ {
		response, err := getRequest(cb, url)
		if err != nil {
			println("MyCircuitBreaker Error:", err.Error())
		} else {
			println("MyCircuitBreaker Response:", response)
		}
	}

	// Simulate failing requests to trigger the circuit breaker
	url = "http://localhost:8080/hello?fail=true"
	for i := 0; i < 10; i++ {
		response, err := getRequest(cb, url)
		if err != nil {
			println("MyCircuitBreaker Error:", err.Error())
		} else {
			println("MyCircuitBreaker Response:", response)
		}
	}

	time.Sleep(5 * time.Second) // Wait for the circuit breaker to reset

	// Test again after reset
	url = "http://localhost:8080/hello"
	response, err := getRequest(cb, url)
	if err != nil {
		println("MyCircuitBreaker Error after reset:", err.Error())
	} else {
		println("MyCircuitBreaker Response after reset:", response)
	}
}

var httpClient = &http.Client{
	Timeout: 2 * time.Second,
}

func getRequest(cb *impl.MyCircuitBreaker[string], url string) (string, error) {
	res, err := cb.Execute(func() (string, error) {
		return get(url)
	})
	if err != nil {
		return "", fmt.Errorf("request failed: %w", err)
	}

	return res, err
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

	limitedReader := http.MaxBytesReader(nil, resp.Body, 10*1024*1024) // 10MB max response size
	body, err := io.ReadAll(limitedReader)
	if err != nil {
		return "", fmt.Errorf("failed to read response: %w", err)
	}
	return string(body), nil
}
