package com.codespark.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MockServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockServiceApplication.class, args);
	}

	@GetMapping("/hello")
	public ResponseEntity<String> hello(@RequestParam(defaultValue = "false") boolean fail) {
		if (fail) { // Simulate service failures
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body("Service Unavailable");
		}
		return ResponseEntity.ok("Hello World");
	}

}
