package com.example.finflow.service;

import com.example.finflow.dto.CreateTransactionRequest;
import com.example.finflow.model.Account;
import com.example.finflow.model.Transaction;
import com.example.finflow.repository.AccountRepository;
import com.example.finflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *  1 - PURCHASE
 *  2 - INSTALLMENT PURCHASE
 *  3 - WITHDRAWAL
 *  4 - PAYMENT
 * Debit types (1, 2, 3): Allowed only if availableLimit >= amount
 * Credit type  (4): Always OK, restores availableLimit
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Mono<Transaction> createTransaction(CreateTransactionRequest request) {
        return accountRepository.findById(request.getAccountId())
                .flatMap(account -> {

                    BigDecimal amount = request.getAmount();
                    Integer operationTypeId = request.getOperationTypeId();

                    // Create transaction
                    Transaction tx = new Transaction(
                            account.getId(),
                            operationTypeId,
                            amount,
                            LocalDateTime.now()
                    );


                    boolean isOk = false;

                    if (isDebitOperation(operationTypeId)) {
                        // Debit operations (Purchase, Installment, Withdrawal)
                        if (account.canPurchase(amount)) {
                            account.applyPurchase(amount);
                            isOk = true;
                        }
                    } else if (operationTypeId == 4) {
                        // Credit operation (Payment)
                        account.applyPayment(amount);
                        isOk = true;
                    }

                    String status = isOk ? "OK" : "NOK";
                    log.info("Processed Transaction -> Type={} Amount=${} Status={} AvailableLimit={}",
                            operationTypeId, amount, status, account.getAvailableLimit());

                    // Persist results
                    if (isOk) {
                        return accountRepository.save(account)
                                .then(transactionRepository.save(tx));
                    } else {
                        // NOK transactions still stored for audit
                        return transactionRepository.save(tx);
                    }
                });
    }

    private boolean isDebitOperation(int operationTypeId) {
        // 1, 2, 3 are debit operations
        return operationTypeId == 1 || operationTypeId == 2 || operationTypeId == 3;
    }
}
