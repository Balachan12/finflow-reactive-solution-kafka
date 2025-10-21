package com.example.finflow.web;

import com.example.finflow.FinFlowReactiveApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(classes = FinFlowReactiveApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerErrorTest {

    @Autowired
    private WebTestClient webTestClient;

    private long existingAccountId;

    @BeforeEach
    void setup() {
        String docNumber = "DOC_TEST_" + System.currentTimeMillis();
        String requestJson = String.format("{\"documentNumber\":\"%s\"}", docNumber);

        AtomicLong idHolder = new AtomicLong(-1L);

        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accountId").value(v -> idHolder.set(((Number) v).longValue()));

        existingAccountId = idHolder.get();
        if (existingAccountId <= 0) {
            throw new IllegalStateException("Failed to create test account");
        }
    }

    @Test
    void createTransaction_unknownAccount_returns404() {
        String requestJson = """
                {
                  "accountId": 99999999,
                  "operationTypeId": 1,
                  "amount": -100
                }
                """;

        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createTransaction_invalidOperationType_returns400() {
        String requestJson = String.format("""
                {
                  "accountId": %d,
                  "operationTypeId": 999,
                  "amount": -100
                }
                """, existingAccountId);

        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createTransaction_wrongSignForPayment_returns400() {
        // PAYMENT (4) must be positive; sending negative should yield 400
        String requestJson = String.format("""
                {
                  "accountId": %d,
                  "operationTypeId": 4,
                  "amount": -10
                }
                """, existingAccountId);

        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus().isBadRequest();
    }
}