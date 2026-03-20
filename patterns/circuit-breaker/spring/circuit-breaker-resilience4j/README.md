# Production-Grade Circuit Breaker with Resilience4j & Spring Boot

This project demonstrates a production-grade implementation of the Circuit Breaker pattern using **Resilience4j** and **Spring Boot**. It is designed to prevent cascading failures in a microservices architecture by gracefully handling degraded or unresponsive downstream services.

## 🏗️ Architecture & Project Structure

The repository is split into two independent Spring Boot services:

- **`client-service`** (Runs on `localhost:8081`): The primary application. It implements the Resilience4j circuit breaker, handles error propagation, and exposes the Actuator endpoints for monitoring.
- **`mock-service`**: A simulated external/downstream service. It is designed to return successful responses or simulate failures (like 500 Internal Server Errors or timeouts) based on the request parameters.

```text
CIRCUIT-BREAKER-RESILIENCE4J/
├── client-service/      # Main service with Resilience4j config and WebClient
├── mock-service/        # Downstream API simulating external dependencies
└── README.md            # Project documentation
```

## 🚀 How to Run

You will need to run both services simultaneously to test the interactions.

1.  **Start the Mock Service:**
    Navigate to the `mock-service` directory and start the application.

    ```bash
    cd mock-service
    ./mvnw spring-boot:run
    ```

2.  **Start the Client Service:**
    Open a new terminal, navigate to the `client-service` directory, and start the application.
    ```bash
    cd client-service
    ./mvnw spring-boot:run
    ```

## 🧪 Testing the Circuit Breaker

The `client-service` exposes endpoints to trigger requests to the `mock-service` and monitor the state of the circuit breaker via Spring Boot Actuator.

### 1. The Happy Path (Circuit CLOSED)

Test a successful call where the mock service responds normally.

```bash
curl http://localhost:8081/test?fail=false
```

_Expected Result:_ The request succeeds, returning the expected data from the mock service.

### 2. Simulating Service Failures (Tripping the Circuit to OPEN)

Force the mock service to fail. Depending on your configuration (e.g., minimum number of calls and failure rate threshold), repeating this call will trip the circuit breaker.

```bash
curl http://localhost:8081/test?fail=true
```

_Expected Result:_ \* Initially, you will see HTTP errors (e.g., 500/502/503) propagated from the downstream failure.

- Once the failure threshold is met, the circuit transitions to **OPEN**. Subsequent requests (even `fail=false`) will immediately "fast-fail" without actually calling the mock service, preventing further strain on the system.

### 3. Monitoring the State via Actuator

You can inspect the real-time state (CLOSED, OPEN, HALF_OPEN) and metrics (failure rate, buffered calls, etc.) of your circuit breaker at any time.

```bash
curl http://localhost:8081/actuator/circuitbreakers
```

_Tip: Pipe this output into `jq` (`curl ... | jq`) for pretty-printed, highly readable JSON._

## ⚙️ How Recovery Works (HALF-OPEN State)

1.  Trigger the failures until the Actuator endpoint shows the state as `"state": "OPEN"`.
2.  Wait for the configured `waitDurationInOpenState` (e.g., 10 seconds).
3.  The circuit will transition to **HALF_OPEN**.
4.  Send a successful request (`fail=false`). The circuit breaker will allow a limited number of test requests through. If they succeed, it closes the circuit (`"state": "CLOSED"`) and resumes normal operations.

## ⚙️ Configuration (application.yml)

The circuit breaker's sensitivity and recovery timings are fully configurable in the `client-service/src/main/resources/application.yml` file.

Resilience4j uses a "sliding window" to track successful and failed calls. Here is the default production-grade configuration used in this project:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      hello-service-circuit-breaker:
        registerHealthIndicator: true
        # The number of calls to evaluate (e.g., the last 10 calls)
        slidingWindowSize: 10
        # The minimum number of calls required before the failure rate is calculated
        minimumNumberOfCalls: 5
        # The threshold percentage of failures that will trip the circuit to OPEN
        failureRateThreshold: 50
        # How long the circuit stays OPEN before transitioning to HALF_OPEN
        waitDurationInOpenState: 10s
        # Number of test calls allowed when in HALF_OPEN state to check if the downstream is healthy
        permittedNumberOfCallsInHalfOpenState: 3
```

### Key Properties Explained

- **`minimumNumberOfCalls`**: Prevents the circuit from tripping prematurely. If this is set to 5, the circuit breaker will not calculate the failure rate until at least 5 requests have been made.
- **`slidingWindowSize`**: Dictates the sample size. A size of 10 means the circuit breaker looks at the outcome of the last 10 calls to determine the current failure rate.
- **`failureRateThreshold`**: If set to 50 (50%), and 5 out of the last 10 calls fail, the circuit trips (**OPEN**).
- **`waitDurationInOpenState`**: The "cool down" period. It gives the failing downstream service time to recover (10 seconds in this example) before the client service tries sending traffic again.
- **`permittedNumberOfCallsInHalfOpenState`**: When the cool down finishes, the circuit enters **HALF_OPEN**. It will allow this exact number of calls through as a test. If they succeed, the circuit closes; if they fail, it re-opens.
