package com.example.finflow.web;

import com.example.finflow.FinFlowReactiveApplication;
import com.example.finflow.dto.CreateTransactionRequest;
import com.example.finflow.model.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.stream.Stream;

@SpringBootTest(classes = FinFlowReactiveApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerParameterizedTest {

    @Autowired
    private WebTestClient webTestClient;

    private static Long accountId = 1L;

    private static Stream<TestCase> transactionSequence() {
        return Stream.of(
                new TestCase(1, 1001.00, "NOK"),
                new TestCase(1, 1000.00, "OK"),
                new TestCase(1, 1.00, "NOK"),
                new TestCase(4, 1000.00, "OK"),
                new TestCase(1, 1.00, "OK"),
                new TestCase(1, 1000.00, "NOK"),
                new TestCase(1, 999.00, "OK")
        );
    }

    @ParameterizedTest(name = "[{index}] Type={0} Amount=${1} -> Expected={2}")
    @MethodSource("transactionSequence")
    void testPhase2Flow(TestCase test) {
        CreateTransactionRequest req = new CreateTransactionRequest(
                accountId,
                test.operationTypeId,
                BigDecimal.valueOf(test.amount)
        );

        webTestClient.post()
                .uri("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Transaction.class)
                .value(tx -> {
                    System.out.printf("Transaction: Type=%d Amount=$%.2f%n",
                            test.operationTypeId, test.amount);
                });
    }

    private record TestCase(int operationTypeId, double amount, String expectedStatus) {}
}
