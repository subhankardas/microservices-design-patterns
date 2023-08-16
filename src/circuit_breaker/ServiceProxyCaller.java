package circuit_breaker;

import java.util.concurrent.TimeUnit;

class ExternalService {
    public void performOperation() throws Exception {
        throw new Exception("Service Exception");
    }
}

public class ServiceProxyCaller {

    public static void main(String[] args) throws InterruptedException {

        CircuitBreaker breaker = new CircuitBreaker(3, 5000);
        ExternalService service = new ExternalService();

        for (int i = 0; i < 10; i++) {
            if (breaker.allowRequest()) {
                try {
                    service.performOperation(); // After 3 failures, circuit gets opened and rest 7 requests are allowed
                    System.out.println("Success");
                } catch (Exception e) {
                    System.out.println("Failed");
                    breaker.recordFailure();
                }
            } else {
                System.out.println("Circuit Breaker is OPEN. Request not allowed.");
            }
        }

        // After 6 seconds, reset the circuit breaker
        TimeUnit.SECONDS.sleep(6);
        breaker.reset();

        // Sending more test requests
        for (int i = 0; i < 5; i++) {
            if (breaker.allowRequest()) {
                try {
                    service.performOperation(); // Allows first 3 requests, after 3 failures, circuit gets opened again
                                                // and blocks next 2 requests
                    System.out.println("Success");
                } catch (Exception e) {
                    System.out.println("Failed");
                    breaker.recordFailure();
                }
            } else {
                System.out.println("Circuit Breaker is OPEN. Request not allowed.");
            }

            TimeUnit.SECONDS.sleep(1);
        }
    }

}
