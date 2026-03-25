package com.codespark.retrybackoff.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.codespark.retrybackoff.mock.MockExternalClient;

@Service
public class ExternalAPIService {

    private static final Logger log = LoggerFactory.getLogger(ExternalAPIService.class);

    @Autowired
    private MockExternalClient client;

    @Retryable(includes = {
            RuntimeException.class }, maxRetries = 4, delayString = "500ms", multiplier = 2, maxDelayString = "5s", jitter = 2)
    @ConcurrencyLimit(10)
    public String failingAPICallWithRetry() {
        log.info("Attempting API call...");
        return client.failingAPICall();
    }

}
