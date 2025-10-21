package com.example.finflow.web;

import com.example.finflow.FinFlowReactiveApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = FinFlowReactiveApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCreateAccount() {
        String requestJson = "{\"documentNumber\":\"DOC123\"}";

        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isCreated();
    }
}
