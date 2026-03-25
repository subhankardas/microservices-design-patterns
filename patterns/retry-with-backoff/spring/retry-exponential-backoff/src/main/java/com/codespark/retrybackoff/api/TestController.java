package com.codespark.retrybackoff.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private ExternalAPIService externalAPIService;

    @GetMapping("/test")
    public String testFailingAPICall() {
        return externalAPIService.failingAPICallWithRetry();
    }

}
