package com.example.despesas_projeto.domain;

import com.example.despesas_projeto.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionFilter {
    private String userId;
    private TransactionType type;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String description;
}

