package com.example.despesas_projeto.service;

import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.exception.InvalidTransactionException;
import com.example.despesas_projeto.exception.TransactionNotFoundException;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository);
    }

    @Nested
    class CreateTransaction {
        @Test
        void whenValidTransaction_shouldCreateSuccessfully() {
            // Arrange
            Transaction transaction = createValidTransaction();
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

            // Act
            Transaction result = transactionService.createTransaction(transaction);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getCreatedAt());
            verify(transactionRepository).save(transaction);
        }

        @Test
        void whenInvalidTransaction_shouldThrowException() {
            // Arrange
            Transaction invalidTransaction = new Transaction();

            // Act & Assert
            assertThrows(InvalidTransactionException.class,
                    () -> transactionService.createTransaction(invalidTransaction));
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateTransaction {
        @Test
        void whenValidTransaction_shouldUpdateSuccessfully() {
            // Arrange
            Transaction transaction = createValidTransaction();
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(transactionRepository.existsById(any(), any())).thenReturn(false);

            // Act
            Transaction result = transactionService.updateTransaction(transaction);

            // Assert
            assertNotNull(result);
            verify(transactionRepository).save(transaction);
        }

        @Test
        void whenTransactionNotFound_shouldThrowException() {
            // Arrange
            Transaction transaction = createValidTransaction();
            when(transactionRepository.existsById(any(), any())).thenReturn(true);

            // Act & Assert
            assertThrows(TransactionNotFoundException.class,
                    () -> transactionService.updateTransaction(transaction));
        }
    }

    @Nested
    class DeleteTransaction {
        @Test
        void whenTransactionExists_shouldDeleteSuccessfully() {
            // Arrange
            Transaction transaction = createValidTransaction();
            when(transactionRepository.findById(any(), any()))
                    .thenReturn(Optional.of(transaction));

            // Act
            transactionService.delete("userId", LocalDateTime.now());

            // Assert
            verify(transactionRepository).delete(transaction);
        }

        @Test
        void whenTransactionNotFound_shouldThrowException() {
            // Arrange
            when(transactionRepository.findById(any(), any()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TransactionNotFoundException.class,
                    () -> transactionService.delete("userId", LocalDateTime.now()));
        }
    }

    @Nested
    class CalculationsTests {
        @Test
        void calculateTotalByType_shouldReturnCorrectTotal() {
            // Arrange
            String userId = "user123";
            List<Transaction> transactions = Arrays.asList(
                    createTransactionWithValue(TransactionType.INCOME, new BigDecimal("100")),
                    createTransactionWithValue(TransactionType.INCOME, new BigDecimal("200"))
            );
            when(transactionRepository.findByUserIdAndDateRange(any(), any(), any()))
                    .thenReturn(transactions);

            // Act
            BigDecimal result = transactionService.calculateTotalByType(
                    userId, TransactionType.INCOME, 2023, 1);

            // Assert
            assertEquals(new BigDecimal("300"), result);
        }


        @Test
        void getMonthlyBalance_shouldReturnCorrectBalance() {
            // Arrange
            String userId = "user123";
            when(transactionRepository.findByUserIdAndDateRange(any(), any(), any()))
                    .thenReturn(Arrays.asList(
                            createTransactionWithValue(TransactionType.INCOME, new BigDecimal("1000")),
                            createTransactionWithValue(TransactionType.EXPENSE, new BigDecimal("600"))
                    ));

            // Act
            Map<String, BigDecimal> result = transactionService.getMonthlyBalance(
                    userId, 2023, 1);

            // Assert
            assertEquals(new BigDecimal("1000"), result.get("receitas"));
            assertEquals(new BigDecimal("600"), result.get("despesas"));
            assertEquals(new BigDecimal("400"), result.get("saldo"));
        }

    }

    private Transaction createValidTransaction() {
        return Transaction.builder()
                .userId("user123")
                .transactionDate(LocalDateTime.now())
                .description("Test transaction")
                .value(new BigDecimal("100"))
                .type(TransactionType.INCOME)
                .category("Test")
                .build();
    }

    private Transaction createTransactionWithValue(TransactionType type, BigDecimal value) {
        return Transaction.builder()
                .userId("user123")
                .transactionDate(LocalDateTime.now())
                .description("Test transaction")
                .value(value)
                .type(type)
                .category("Test")
                .build();
    }

}
