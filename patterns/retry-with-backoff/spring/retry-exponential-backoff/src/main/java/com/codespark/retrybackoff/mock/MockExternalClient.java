package com.codespark.retrybackoff.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Mock external to simulate intermittent API failures for demonstrating retry mechanisms.
@Component
public class MockExternalClient {

    private static final Logger log = LoggerFactory.getLogger(MockExternalClient.class);

    private int count = 0;

    // Simulates an call that succeeds on every 5th attempt to test retry logic.
    public String failingAPICall() {
        count++;

        // Succeeds on every 5th request, fails otherwise.
        if (count % 5 == 0) {
            log.info("API call successful on attempt: {}", count);
            count = 0; // Reset count after successful call.
            return "Successfully fetched data from external API!";
        }
        log.warn("API call failed on attempt: {}", count);
        throw new RuntimeException("Failing API call!");
    }

}
