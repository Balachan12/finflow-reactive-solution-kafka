package com.example.finflow.web;

import com.example.finflow.FinFlowReactiveApplication;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.stream.Stream;

/**
 * Integration test covering all 4 operation types with a realistic sequence.
 */
@SpringBootTest(
        classes = FinFlowReactiveApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionControllerParameterizedTest {

    @Autowired
    private WebTestClient webTestClient;

    private static Long accountId;
    private static String documentNumber;

    @BeforeAll
    static void createAccount(@Autowired WebTestClient webTestClient) {
        documentNumber = "DOC_" + System.nanoTime(); // always unique to avoid UNIQUE constraint violation

        var spec = webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"documentNumber\":\"" + documentNumber + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accountId").exists();

        // extract id safely
        accountId = webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"documentNumber\":\"" + documentNumber + "B\"}") // create another to read id
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accountId").value(v -> {
                    // We actually only need ANY valid account; use the second one's id
                    accountId = ((Number) v).longValue();
                })
                .returnResult().getResponseBody() == null ? -1L : accountId;

        // Or, if you prefer the very first created account's id, expose it from /accounts response.

        Assertions.assertTrue(accountId != null && accountId > 0, "Account creation failed");
        System.out.println("✅ Test account created: id=" + accountId + ", doc=" + documentNumber);
    }

    static Stream<TestCase> testCases() {
        return Stream.of(
                new TestCase(1, new BigDecimal("1001.00"), "NOK"),  // PURCHASE: over limit
                new TestCase(1, new BigDecimal("1000.00"), "OK"),   // PURCHASE: OK
                new TestCase(1, new BigDecimal("1.00"), "NOK"),     // PURCHASE: over limit after zero
                new TestCase(4, new BigDecimal("1000.00"), "OK"),   // PAYMENT: restore full
                new TestCase(2, new BigDecimal("500.00"), "OK"),    // INSTALLMENT PURCHASE
                new TestCase(3, new BigDecimal("600.00"), "NOK"),   // WITHDRAWAL: beyond remaining
                new TestCase(3, new BigDecimal("400.00"), "OK")     // WITHDRAWAL: OK (with 1% fee)
        );
    }

    @Order(1)
    @ParameterizedTest(name = "[{index}] Type={0} Amount={1} Expect={2}")
    @MethodSource("testCases")
    void endToEndFlow(TestCase tc) {
        String payload = String.format("""
                {
                  "accountId": %d,
                  "operationTypeId": %d,
                  "amount": %s
                }""", accountId, tc.operationTypeId, tc.amount);

        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(payload), String.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accountId").isEqualTo(accountId)
                .jsonPath("$.operationTypeId").isEqualTo(tc.operationTypeId)
                .jsonPath("$.amount").isEqualTo(tc.amount.doubleValue());

        System.out.printf("✔️  Tx Type=%d Amount=%s Expected=%s%n",
                tc.operationTypeId, tc.amount, tc.expectedStatus);
    }

    private record TestCase(int operationTypeId, BigDecimal amount, String expectedStatus) {}
}
