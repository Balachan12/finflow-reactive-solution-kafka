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

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Operation types:
     * 1 – PURCHASE (debit, allowed if available ≥ amount)
     * 2 – INSTALLMENT PURCHASE (debit, allowed if available ≥ amount)
     * 3 – WITHDRAWAL (debit+fee, allowed if available ≥ amount+fee)
     * 4 – PAYMENT (credit, always allowed, cap at creditLimit)
     */
    public Mono<Transaction> createTransaction(CreateTransactionRequest request) {
        return accountRepository.findById(request.getAccountId())
                .flatMap(account -> {
                    BigDecimal amount = request.getAmount();
                    Integer operationTypeId = request.getOperationTypeId();

                    Transaction tx = new Transaction(
                            account.getAccountId(),
                            operationTypeId,
                            amount,
                            LocalDateTime.now()
                    );

                    boolean isOk = false;

                    switch (operationTypeId) {
                        case 1 -> { // PURCHASE
                            if (account.canDebit(amount)) {
                                account.applyPurchase(amount);
                                isOk = true;
                            }
                        }
                        case 2 -> { // INSTALLMENT PURCHASE
                            if (account.canDebit(amount)) {
                                account.applyInstallmentPurchase(amount);
                                isOk = true;
                            }
                        }
                        case 3 -> { // WITHDRAWAL
                            // check with fee as well
                            BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.01));
                            BigDecimal total = amount.add(fee);
                            if (account.canDebit(total)) {
                                account.applyWithdrawal(amount);
                                isOk = true;
                            }
                        }
                        case 4 -> { // PAYMENT
                            account.applyPayment(amount);
                            isOk = true;
                        }
                        default -> log.warn("Unknown operation type: {}", operationTypeId);
                    }

                    String status = isOk ? "OK" : "NOK";
                    log.info("Transaction -> Type={} Amount={} Status={} NewAvailable={}",
                            operationTypeId, amount, status, account.getAvailableLimit());

                    Mono<Transaction> saveTx = transactionRepository.save(tx);
                    return isOk ? accountRepository.save(account).then(saveTx) : saveTx;
                });
    }
}
