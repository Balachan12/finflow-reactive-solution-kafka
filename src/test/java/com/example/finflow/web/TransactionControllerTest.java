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
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private long accountId;

    @BeforeEach
    void createAccount() {
        String doc = "DOC_OK_" + System.currentTimeMillis();
        AtomicLong idHolder = new AtomicLong(-1L);

        webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"documentNumber\":\"" + doc + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accountId").value(v -> idHolder.set(((Number) v).longValue()));

        accountId = idHolder.get();
        if (accountId <= 0) throw new IllegalStateException("Account creation failed");
    }

    @Test
    void testCreateTransaction() {
        // PURCHASE (1) must be negative
        String body = """
                {
                  "accountId": %d,
                  "operationTypeId": 1,
                  "amount": -100.0
                }
                """.formatted(accountId);

        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.transactionId").exists()
                .jsonPath("$.amount").isEqualTo(-100.0);
    }
}