package com.example.finflow.web;

import com.example.finflow.dto.CreateTransactionRequest;
import com.example.finflow.model.Transaction;
import com.example.finflow.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<Transaction> createTransaction(@RequestBody @Valid CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }
}
