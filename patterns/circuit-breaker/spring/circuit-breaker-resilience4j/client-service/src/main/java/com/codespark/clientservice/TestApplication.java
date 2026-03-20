package com.codespark.clientservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class TestApplication {

  @Autowired
  private HelloService service;

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @GetMapping("/test")
  public Mono<String> getTest(@RequestParam(defaultValue = "false") boolean fail) {
    return service.getHelloWithCB(fail); // Dummy API to simulate failure and trip the circuit breaker
  }

}
