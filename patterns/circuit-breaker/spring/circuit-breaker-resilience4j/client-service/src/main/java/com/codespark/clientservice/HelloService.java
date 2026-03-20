package com.codespark.clientservice;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import reactor.core.publisher.Mono;

@Service
public class HelloService {

    private static final Logger LOG = LoggerFactory.getLogger(HelloService.class);

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public HelloService(ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = WebClient.builder().baseUrl("http://localhost:8080").build();
        this.circuitBreaker = circuitBreakerFactory.create("hello-service-circuit-breaker");
    }

    public Mono<String> getHelloWithCB(boolean fail) {
        String url = "/hello?fail=" + fail; // Simulate failure based on query param

        return circuitBreaker.run( // Execute http request with circuit breaker
                webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class),
                this::handleFallback // Handle exceptions in fallback method
        );
    }

    private Mono<String> handleFallback(Throwable throwable) {
        // 1. Circuit is OPEN
        if (throwable instanceof CallNotPermittedException) {
            LOG.warn("Circuit Breaker is OPEN. Fast-failing request.");
            return Mono.error(new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily overloaded."));
        }

        // 2. HTTP Errors from the external service
        if (throwable instanceof WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                LOG.error("Bad request to external service: {}", ex.getStatusCode());
                return Mono.error(new ResponseStatusException(
                        ex.getStatusCode(), "Invalid request parameters."));
            }
            if (ex.getStatusCode().is5xxServerError()) {
                LOG.error("External server is failing: {}", ex.getStatusCode());
                return Mono.error(new ResponseStatusException(
                        ex.getStatusCode(), "Upstream service is failing."));
            }
        }

        // 3. Network Timeouts
        if (throwable instanceof TimeoutException) {
            LOG.error("Request to external service timed out.");
            // Propagate as 504 Gateway Timeout
            return Mono.error(new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT, "Upstream service timed out."));
        }

        // 4. Default: Just rethrow the original error
        LOG.error("Unexpected error making request to service", throwable);
        return Mono.error(throwable);
    }

}
