package com.example.finflow.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @NoArgsConstructor @AllArgsConstructor
public class CreateTransactionRequest {
    @NotNull Long accountId;
    @NotNull Integer operationTypeId;
    @NotNull BigDecimal amount;
}
