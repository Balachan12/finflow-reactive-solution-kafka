package com.example.finflow.service;

import com.example.finflow.dto.CreateAccountRequest;
import com.example.finflow.model.Account;
import com.example.finflow.repository.AccountRepository;
import com.example.finflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        // Delete transactions before accounts to avoid FK violations
        transactionRepository.deleteAll().block();
        accountRepository.deleteAll().block();
    }

    @Test
    @DisplayName("createAccount -> returns Account with id")
    void testCreateAccount() {
        var req = new CreateAccountRequest("ACC_" + System.nanoTime());

        StepVerifier.create(accountService.createAccount(req))
                .expectNextMatches(acc -> acc instanceof Account && acc.getAccountId() != null)
                .verifyComplete();
    }
}