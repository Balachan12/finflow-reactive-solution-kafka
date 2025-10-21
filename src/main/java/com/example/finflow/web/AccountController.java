package com.example.finflow.web;

import com.example.finflow.dto.AccountResponse;
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
    public Mono<Account> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{accountId}")
    public Mono<AccountResponse> getAccount(@PathVariable("accountId") Long accountId) {
        return accountService.getAccount(accountId)
                .map(a -> new AccountResponse(a.getAccountId(), a.getDocumentNumber()));
    }
}
