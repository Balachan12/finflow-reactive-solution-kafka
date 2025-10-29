// src/main/java/com/example/finflow/web/AccountController.java
package com.example.finflow.web;

import com.example.finflow.dto.CreateAccountRequest;
import com.example.finflow.model.Account;
import com.example.finflow.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        // Map DTO -> entity (credit/available limits will be set in service or defaults)
        Account account = Account.builder()
                .documentNumber(request.getDocumentNumber())
                .build();

        return accountService.createAccount(account);
    }

    @GetMapping("/{id}")
    public Mono<Account> getAccount(@PathVariable("id") Long id) {
        return accountService.getAccountById(id);
    }
}
