package com.dulmin.cicd_demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller that exposes demo API endpoints.
 *
 * Endpoints:
 *   GET /hello  → Returns a greeting message
 *   GET /health → Returns application health status
 */
@RestController
public class DemoController {

    /**
     * Hello endpoint — used to verify the API is working correctly.
     *
     * @return a simple greeting string
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from Spring Boot CI/CD Demo!");
    }

    /**
     * Health check endpoint — used by monitoring tools, load balancers,
     * and the CI/CD pipeline to confirm the service is up.
     *
     * @return application status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Application is running");
    }
}
