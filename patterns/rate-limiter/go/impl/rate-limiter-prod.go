package impl

import (
	"net/http"

	"golang.org/x/time/rate"
)

// Create a rate limiter:
// - Limit: 2 requests per second (rate.Limit(2))
// - Burst: Maximum of 5 requests at once if tokens are available
var limiter = rate.NewLimiter(2, 5)

// Allow checks if a request can proceed based on the rate limit.
func rateLimitMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if limiter.Allow() {
			next.ServeHTTP(w, r)
		} else {
			http.Error(w, "Rate limit exceeded", http.StatusTooManyRequests)
		}
	})
}

// dummyHandler is a simple HTTP handler that simulates processing a request.
func dummyHandler(w http.ResponseWriter, r *http.Request) {
	w.Write([]byte("Request processed successfully!"))
}

// StartRateLimitedServer starts an HTTP server with the rate-limiting middleware applied.
func StartRateLimitedServer() {
	mux := http.NewServeMux()
	mux.Handle("/", rateLimitMiddleware(http.HandlerFunc(dummyHandler)))

	println("Starting server...")
	http.ListenAndServe(":8080", mux)
}

// TestRateLimiter simulates making multiple requests to the rate-limited server.
func TestRateLimiter() {
	for i := 1; i <= 1000; i++ {
		resp, err := http.Get("http://localhost:8080/")
		if err != nil {
			println("Error making request:", err.Error())
			continue
		}

		if resp.StatusCode == http.StatusOK {
			println("✅ Request", i, "processed successfully.")
		} else {
			println("❌ Request", i, "processed. Status:", resp.Status)
		}
	}
}
