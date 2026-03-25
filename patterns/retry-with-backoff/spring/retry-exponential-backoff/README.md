# Retry with Exponential Backoff using Spring Framework's Resilience Features

## Overview

This project demonstrates the **Retry with Exponential Backoff** design pattern using a Spring Boot application and the resilience features from the Spring Framework. It simulates calling an external service that intermittently fails and shows how to gracefully handle these failures by retrying the operation with an increasing delay between attempts.

This pattern is crucial for building resilient microservices that can withstand transient failures in downstream services.

## How it Works

The application is configured to retry a failing API call up to 4 times with an exponential backoff policy.

1.  **`ApiController`**: Exposes a REST endpoint at `/test` that triggers the external service call.
2.  **`ExternalAPIService`**: Contains the core business logic. The `failingAPICallWithRetry()` method is annotated with `@Retryable`. This annotation, part of the Spring Resilience module, tells Spring to re-invoke the method if it throws a `RuntimeException`.
3.  **`MockExternalClient`**: Simulates a remote service that fails for the first 4 attempts and succeeds on the 5th attempt.
4.  **`@Retryable` Configuration**:
    - `maxRetries`: The service will attempt to call the API a maximum of 4 times after the initial failure.
    - `delayString`: The initial delay is "500ms".
    - `multiplier`: The delay is multiplied by 2 after each failed attempt.
    - `maxDelayString`: The delay will not exceed "5s".
    - `jitter`: Adds a random amount of jitter to the delay.
5.  **`@ConcurrencyLimit`**: The service also demonstrates the use of `@ConcurrencyLimit` to restrict the number of concurrent calls to the method to 10.

## Technologies Used

- Java 21
- Spring Boot
- Spring Framework Resilience
- Maven

## How to Test the Endpoint

Once the application is running, you can test the retry functionality by accessing the following URL in your browser or using a tool like `curl`:

```bash
curl http://localhost:8080/test
```

**Observe the Console Logs:**

You will see log messages indicating the failed attempts and the increasing delay between them. After the 4th failure, the 5th attempt will succeed, and you will see a success message in the browser.

**Example Log Output:**

```
WARN  c.c.r.mock.MockExternalClient - API call failed on attempt: 1
INFO  c.c.r.api.ExternalAPIService - Attempting API call...
WARN  c.c.r.mock.MockExternalClient - API call failed on attempt: 2
INFO  c.c.r.api.ExternalAPIService - Attempting API call...
WARN  c.c.r.mock.MockExternalClient - API call failed on attempt: 3
INFO  c.c.r.api.ExternalAPIService - Attempting API call...
WARN  c.c.r.mock.MockExternalClient - API call failed on attempt: 4
INFO  c.c.r.api.ExternalAPIService - Attempting API call...
INFO  c.c.r.mock.MockExternalClient - API call successful on attempt: 5
```

If you call the endpoint again, the cycle will repeat.
