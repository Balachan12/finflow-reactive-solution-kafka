package com.example.finflow.service;

import com.example.finflow.model.Account;
import com.example.finflow.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Mono<Account> createAccount(Account request) {
        Account toSave = Account.builder()
                .documentNumber(request.getDocumentNumber())
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.valueOf(1000))
                .availableLimit(request.getAvailableLimit() != null ? request.getAvailableLimit() : BigDecimal.valueOf(1000))
                .build();

        return accountRepository.save(toSave)
                .doOnError(ex -> {
                    ex.printStackTrace();
                });
    }

    public Mono<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
}
