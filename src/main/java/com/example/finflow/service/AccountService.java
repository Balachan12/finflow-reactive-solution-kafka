package com.example.finflow.service;

import com.example.finflow.dto.CreateAccountRequest;
import com.example.finflow.exception.BusinessException;
import com.example.finflow.model.Account;
import com.example.finflow.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Mono<Account> createAccount(CreateAccountRequest request) {
        if (request == null || !StringUtils.hasText(request.getDocumentNumber())) {
            return Mono.error(new BusinessException("documentNumber must not be blank"));
        }
        Account account = new Account();
        account.setDocumentNumber(request.getDocumentNumber().trim());
        return accountRepository.save(account);
    }

    public Mono<Account> getAccount(Long id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("account not found")));
    }
}
