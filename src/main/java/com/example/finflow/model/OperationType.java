package com.example.finflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("operation_type")
public class OperationType {
    @Id
    private Integer operationTypeId;
    private String description;


}
