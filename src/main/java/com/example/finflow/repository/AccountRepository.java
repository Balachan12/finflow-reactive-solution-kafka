package com.example.finflow.repository;

import com.example.finflow.model.Account;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends R2dbcRepository<Account, Long> {
    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
