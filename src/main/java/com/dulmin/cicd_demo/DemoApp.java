package com.dulmin.cicd_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring Boot CI/CD Demo application.
 *
 * This application demonstrates a complete DevOps pipeline using:
 * - Java 21 + Spring Boot
 * - Maven for build management
 * - Docker for containerization
 * - Jenkins for CI/CD automation
 * - Nginx as a reverse proxy
 */
@SpringBootApplication
public class DemoApp {

    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class, args);
    }
}
