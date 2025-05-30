
package com.example.despesas_projeto.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.utils.DynamoDBPaginationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamoTransactionRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private DynamoTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DynamoTransactionRepository(dynamoDBMapper);
    }

    @Test
    void findById_ExistingTransaction_ShouldReturnTransaction() {
        // Arrange
        String userId = "user123";
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .transactionDate(now)
                .build();

        when(dynamoDBMapper.load(any(Transaction.class))).thenReturn(transaction);

        // Act
        Optional<Transaction> result = repository.findById(userId, now);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals(now, result.get().getTransactionDate());
    }

    @Test
    void save_NewTransaction_ShouldSaveSuccessfully() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .userId("user123")
                .description("Test Transaction")
                .value(BigDecimal.TEN)
                .type(TransactionType.INCOME)
                .category("Test")
                .build();

        // Act
        Transaction savedTransaction = repository.save(transaction);

        // Assert
        assertNotNull(savedTransaction);
        assertNotNull(savedTransaction.getTransactionDate());
        assertNotNull(savedTransaction.getCreatedAt());
        verify(dynamoDBMapper).save(transaction);
    }

    @Test
    void findByUserIdAndType_ShouldReturnFilteredTransactions() {
        // Arrange
        String userId = "user123";
        List<Transaction> transactions = Arrays.asList(
                Transaction.builder().userId(userId).type(TransactionType.INCOME).build(),
                Transaction.builder().userId(userId).type(TransactionType.INCOME).build()
        );
        PaginatedQueryList<Transaction> paginatedList = mock(PaginatedQueryList.class);
        lenient().when(paginatedList.stream()).thenReturn(transactions.stream());
        lenient().when(paginatedList.iterator()).thenAnswer(invocation -> transactions.iterator());
        when(paginatedList.size()).thenReturn(transactions.size());
        when(dynamoDBMapper.query(eq(Transaction.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedList);

        // Act
        List<Transaction> result = repository.findByUserIdAndType(userId, TransactionType.INCOME);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dynamoDBMapper).query(eq(Transaction.class), any(DynamoDBQueryExpression.class));
    }

    @Test
    void deleteById_ShouldDeleteTransaction() {
        // Arrange
        String userId = "user123";
        LocalDateTime now = LocalDateTime.now();

        // Act
        repository.deleteById(userId, now);

        // Assert
        verify(dynamoDBMapper).delete(any(Transaction.class));
    }
}