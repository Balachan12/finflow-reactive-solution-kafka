package com.example.finflow.repository;

import com.example.finflow.model.Transaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends R2dbcRepository<Transaction, Long> {
    Flux<Transaction> findByAccountId(Long accountId);
}
