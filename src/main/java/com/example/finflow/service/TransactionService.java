package com.example.finflow.service;

import com.example.finflow.dto.CreateTransactionRequest;
import com.example.finflow.exception.BusinessException;
import com.example.finflow.exception.ResourceNotFoundException;
import com.example.finflow.model.OperationType;
import com.example.finflow.model.Transaction;
import com.example.finflow.repository.AccountRepository;
import com.example.finflow.repository.OperationTypeRepository;
import com.example.finflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;

    public Mono<Transaction> createTransaction(CreateTransactionRequest request) {
        if (request == null || request.getAccountId() == null) {
            return Mono.error(new BusinessException("accountId is required"));
        }
        if (request.getOperationTypeId() == null) {
            return Mono.error(new BusinessException("operationTypeId is required"));
        }
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            return Mono.error(new BusinessException("amount is required"));
        }

        return accountRepository.existsById(request.getAccountId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Account not found: " + request.getAccountId()));
                    }
                    return operationTypeRepository.findById(request.getOperationTypeId())
                            .switchIfEmpty(Mono.error(new BusinessException("Invalid operationTypeId: " + request.getOperationTypeId())))
                            .flatMap(op -> validateAmount(op, amount)
                                    .then(transactionRepository.save(toEntity(request))));
                });
    }

    private Mono<Void> validateAmount(OperationType op, BigDecimal amount) {
        // For operation types 1-3: amount must be negative; for 4 (PAYMENT): positive
        if (op.getOperationTypeId() == 4) {
            if (amount.signum() <= 0) return Mono.error(new BusinessException("amount must be positive for PAYMENT"));
        } else {
            if (amount.signum() >= 0) return Mono.error(new BusinessException("amount must be negative for this operation"));
        }
        return Mono.empty();
    }

    private Transaction toEntity(CreateTransactionRequest req) {
        Transaction t = new Transaction();
        t.setAccountId(req.getAccountId());
        t.setOperationTypeId(req.getOperationTypeId());
        t.setAmount(req.getAmount());
        t.setEventDate(LocalDateTime.now());
        return t;
    }
}
