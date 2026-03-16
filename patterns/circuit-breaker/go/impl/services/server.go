package services

import (
	"fmt"
	"net/http"
)

func StartServer() {
	http.HandleFunc("/hello", func(w http.ResponseWriter, r *http.Request) {
		// Simulate a failure based on the query parameter "fail", in real scenarios
		// this could be based on actual conditions or errors in the service.
		if r.URL.Query().Get("fail") == "true" {
			http.Error(w, "Service Unavailable", http.StatusServiceUnavailable)
			return
		}

		fmt.Fprintf(w, "Hello, World!")
	})

	// Start the HTTP server
	fmt.Println("Server is running on port 8080...")
	if err := http.ListenAndServe(":8080", nil); err != nil {
		fmt.Printf("Server failed to start: %v\n", err)
	}
}
