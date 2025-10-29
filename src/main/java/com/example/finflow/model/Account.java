package com.example.finflow.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("account")
public class Account {

    @Id
    @Column("account_id")
    private Long accountId;

    @Column("document_number")
    private String documentNumber;

    @Builder.Default
    @Column("credit_limit")
    private BigDecimal creditLimit = BigDecimal.valueOf(1000);

    @Builder.Default
    @Column("available_limit")
    private BigDecimal availableLimit = BigDecimal.valueOf(1000);

    /** Shared rule for all debit operations (1,2,3) */
    public boolean canDebit(BigDecimal amount) {
        return availableLimit.compareTo(amount) >= 0;
    }

    /** 1 – PURCHASE */
    public void applyPurchase(BigDecimal amount) {
        availableLimit = availableLimit.subtract(amount);
    }

    /** 2 – INSTALLMENT PURCHASE (treated same as purchase for now) */
    public void applyInstallmentPurchase(BigDecimal amount) {
        availableLimit = availableLimit.subtract(amount);
    }

    /** 3 – WITHDRAWAL (includes optional 1% fee) */
    public void applyWithdrawal(BigDecimal amount) {
        BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.01));
        BigDecimal total = amount.add(fee);
        availableLimit = availableLimit.subtract(total);
    }

    /** 4 – PAYMENT (cap at credit limit) */
    public void applyPayment(BigDecimal amount) {
        availableLimit = availableLimit.add(amount);
        if (availableLimit.compareTo(creditLimit) > 0) {
            availableLimit = creditLimit;
        }
    }
}
