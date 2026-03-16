package main

import (
	"fmt"
	"time"

	"github.com/subhankardas/microservices-design-patterns/impl"
	"github.com/subhankardas/microservices-design-patterns/impl/services"
)

func main() {
	// Start the server in a separate goroutine
	go services.StartServer()

	// Create a circuit breaker instance
	cb := impl.NewCircuitBreaker()

	// Simulate making requests to the server
	// 1. Successful request
	url := "http://localhost:8080/hello"
	response, err := services.GetRequestWithCircuitBreaker(cb, url)
	if err != nil {
		fmt.Printf("Error: %v\n", err)
	} else {
		fmt.Printf("Response: %s\n", response)
	}

	// 2. Simulate failure by adding the "fail" query parameter
	// This should open the circuit breaker after a few failed
	// attempts, preventing further requests until it resets.
	for i := 0; i < 10; i++ {
		url = "http://localhost:8080/hello?fail=true"
		response, err = services.GetRequestWithCircuitBreaker(cb, url)
		if err != nil {
			fmt.Printf("Error %2d: %v\n", i, err)
		} else {
			fmt.Printf("Response %2d: %s\n", i, response)
		}
		time.Sleep(100 * time.Millisecond)
	}

	time.Sleep(10 * time.Second) // Wait for the circuit breaker to reset

	// 3. Attempt another request after the circuit breaker has reset
	// This should succeed since the circuit breaker should have transitioned
	// back to the closed state.
	url = "http://localhost:8080/hello"
	response, err = services.GetRequestWithCircuitBreaker(cb, url)
	if err != nil {
		fmt.Printf("Error after reset: %v\n", err)
	} else {
		fmt.Printf("Response after reset: %s\n", response)
	}
}
