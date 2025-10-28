package com.example.finflow.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("accounts")
public class Account {

    @Id
    private Long id;
    private String documentNumber;

    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.valueOf(1000.00);

    @Builder.Default
    private BigDecimal availableLimit = BigDecimal.valueOf(1000.00);

    public boolean canPurchase(BigDecimal amount) {
        return availableLimit.compareTo(amount) >= 0;
    }

    public void applyPurchase(BigDecimal amount) {
        availableLimit = availableLimit.subtract(amount);
    }

    public void applyPayment(BigDecimal amount) {
        availableLimit = availableLimit.add(amount);
        if (availableLimit.compareTo(creditLimit) > 0) {
            availableLimit = creditLimit;
        }
    }

    // ✅ Compatibility for getAccountId()
    public Long getAccountId() {
        return id;
    }
}
