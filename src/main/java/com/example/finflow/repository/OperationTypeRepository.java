package com.example.finflow.repository;

import com.example.finflow.model.OperationType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface OperationTypeRepository extends R2dbcRepository<OperationType, Integer> {
}
