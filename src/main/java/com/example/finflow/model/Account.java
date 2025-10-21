package com.example.finflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("account")
public class Account {
    @Id
    private Long accountId;
    private String documentNumber;

    public Account(String documentNumber) {
        this.documentNumber = documentNumber;
    }


}
