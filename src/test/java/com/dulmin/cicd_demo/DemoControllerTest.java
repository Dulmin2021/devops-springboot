package com.dulmin.cicd_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DemoController.
 *
 * Uses MockMvc to fire HTTP requests against the full Spring context
 * without starting a real server — fast and reliable in CI.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Verify GET /hello returns HTTP 200 and the expected greeting.
     */
    @Test
    void helloEndpoint_shouldReturnGreeting() throws Exception {
        mockMvc.perform(get("/hello"))
               .andExpect(status().isOk())
               .andExpect(content().string("Hello from Spring Boot CI/CD Demo!"));
    }

    /**
     * Verify GET /health returns HTTP 200 and the expected status message.
     */
    @Test
    void healthEndpoint_shouldReturnRunningStatus() throws Exception {
        mockMvc.perform(get("/health"))
               .andExpect(status().isOk())
               .andExpect(content().string("Application is running"));
    }
}
