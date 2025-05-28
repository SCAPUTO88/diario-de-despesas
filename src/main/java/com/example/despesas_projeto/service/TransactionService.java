package com.example.despesas_projeto.service;


import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionService {

    Transaction createTransaction(Transaction transaction);
    Optional<Transaction> findById(String userId, LocalDateTime transactionDate);
    List<Transaction> findAllByUserId(String userId);
    Transaction updateTransaction(Transaction transaction);
    void delete(String userId, LocalDateTime transactionDate);

    List<Transaction> findByType(String userId, TransactionType type);
    List<Transaction> findByCategory(String userId, String category);
    List<Transaction> findByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal calculateTotalByType(String userId, TransactionType type, int year, int month);
    Map<String, BigDecimal> getCategorySummary(String userId, int year, int month);
    Map<String, BigDecimal> getMonthlyBalance(String userId, int year, int month);

    void validateTransaction(Transaction transaction);

}