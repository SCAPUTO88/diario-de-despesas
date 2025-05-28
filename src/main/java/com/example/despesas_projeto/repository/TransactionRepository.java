package com.example.despesas_projeto.repository;

import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findById(String userId, LocalDateTime transactionDate);
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByUserIdAndType(String userId, TransactionType type);
    List<Transaction> findByUserIdAndCategory(String userId, String category);
    boolean existsById(String userId, LocalDateTime transactionDate);
    void deleteById(String userId, LocalDateTime transactionDate);
    <S extends Transaction> S save(S entity);
    void delete(Transaction entity);
    List<Transaction> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
}