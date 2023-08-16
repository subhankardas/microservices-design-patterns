package circuit_breaker;

enum CircuitBreakerState {
    OPEN, HALF_OPEN, CLOSED
};

public class CircuitBreaker {

    private int failureThreshold;
    private int timeout;
    private int failures;
    private long lastFailureTime;
    private CircuitBreakerState state;

    public CircuitBreaker(int failureThreshold, int timeout) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
        this.failures = 0;
        this.lastFailureTime = 0;
        this.state = CircuitBreakerState.CLOSED;
    }

    public boolean allowRequest() {
        // CLOSED = service is doing well and can accept requests
        if (state == CircuitBreakerState.CLOSED) {
            return true;
        }
        // OPEN = detected a failure threshold and prevents new requests temporarily
        else if (state == CircuitBreakerState.OPEN) {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastFailureTime;

            // Time since last failure is greater than the timeout, transition to HALF_OPEN
            if (elapsedTime >= timeout) {
                state = CircuitBreakerState.HALF_OPEN;
                return true;
            } else { // Timeout period is not reached, circuit remains OPEN, requests are not allowed
                return false;
            }
        }
        // HALF_OPEN = allow a limited number of requests, allows service to recover
        else if (state == CircuitBreakerState.HALF_OPEN) {
            return true;
        }

        return false;
    }

    public void recordFailure() {
        failures++;
        if (failures >= failureThreshold) {
            state = CircuitBreakerState.OPEN;
            lastFailureTime = System.currentTimeMillis();
            System.out.println("Circuit Breaker OPENED");
        }
    }

    public void reset() {
        failures = 0;
        lastFailureTime = 0;
        state = CircuitBreakerState.CLOSED;
        System.out.println("Circuit Breaker CLOSED");
    }

}
