package com.example.despesas_projeto.dto;

import com.example.despesas_projeto.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionResponseDTO (String userId,
    LocalDateTime transactionDate,
    String description,
    BigDecimal value,
    TransactionType type,
    String category,
    List<String> tags,
    LocalDateTime createdAt
) {
        public static TransactionResponseDTO fromModel(com.example.despesas_projeto.model.Transaction transaction) {
            return new TransactionResponseDTO(
                    transaction.getUserId(),
                    transaction.getTransactionDate(),
                    transaction.getDescription(),
                    transaction.getValue(),
                    TransactionType.valueOf(String.valueOf(transaction.getType())),
                    transaction.getCategory(),
                    transaction.getTags(),
                    transaction.getCreatedAt()
            );
        }
    }