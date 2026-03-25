package com.codespark.retrybackoff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods // IMPORTANT: Enables resilience features like Retryable.
public class RetryExponentialBackoffApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetryExponentialBackoffApplication.class, args);
	}

}
