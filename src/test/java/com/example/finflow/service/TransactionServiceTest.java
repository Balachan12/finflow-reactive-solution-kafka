package com.example.finflow.service;

import com.example.finflow.FinFlowReactiveApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@SpringBootTest(classes = FinFlowReactiveApplication.class)
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DatabaseClient databaseClient;

    private long accountId;

    @BeforeEach
    void setupDb() {
        databaseClient.sql("DELETE FROM transaction").fetch().rowsUpdated().block();
        databaseClient.sql("DELETE FROM account").fetch().rowsUpdated().block();
        databaseClient.sql("INSERT INTO account (DOCUMENT_NUMBER) VALUES ($1)")
                .bind(0, "ACC_" + System.currentTimeMillis())
                .fetch().rowsUpdated().block();
        accountId = databaseClient.sql("SELECT MAX(account_id) FROM account")
                .map(row -> row.get(0, Number.class).longValue())
                .first()
                .block();
    }

    @Test
    void testCreateTransaction() {
        var req = new com.example.finflow.dto.CreateTransactionRequest();
        req.setAccountId(accountId);
        req.setOperationTypeId(1); // PURCHASE
        req.setAmount(new BigDecimal("-123.45")); // must be negative for PURCHASE

        StepVerifier.create(transactionService.createTransaction(req))
                .expectNextMatches(tx ->
                        tx.getAccountId() == accountId &&
                        tx.getOperationTypeId() == 1 &&
                        new BigDecimal("-123.45").compareTo(tx.getAmount()) == 0
                )
                .verifyComplete();
    }
}