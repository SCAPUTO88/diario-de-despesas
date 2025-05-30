
package com.example.despesas_projeto.model;

import com.example.despesas_projeto.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void testBuilder() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<String> tags = Arrays.asList("tag1", "tag2");

        // Act
        Transaction transaction = Transaction.builder()
                .userId("user123")
                .transactionDate(now)
                .description("Test Transaction")
                .value(BigDecimal.TEN)
                .type(TransactionType.INCOME)
                .category("Test Category")
                .tags(tags)
                .createdAt(now)
                .build();

        // Assert
        assertEquals("user123", transaction.getUserId());
        assertEquals(now, transaction.getTransactionDate());
        assertEquals("Test Transaction", transaction.getDescription());
        assertEquals(BigDecimal.TEN, transaction.getValue());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals("Test Category", transaction.getCategory());
        assertEquals(tags, transaction.getTags());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void testAddTag() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act
        transaction.addTag("tag1");
        transaction.addTag("tag2");

        // Assert
        assertNotNull(transaction.getTags());
        assertEquals(2, transaction.getTags().size());
        assertTrue(transaction.getTags().contains("tag1"));
        assertTrue(transaction.getTags().contains("tag2"));
    }

    @Test
    void testLocalDateTimeConverter() {
        // Arrange
        Transaction.LocalDateTimeConverter converter = new Transaction.LocalDateTimeConverter();
        LocalDateTime now = LocalDateTime.now();

        // Act
        String converted = converter.convert(now);
        LocalDateTime unconverted = converter.unconvert(converted);

        // Assert
        assertNotNull(converted);
        assertEquals(now, unconverted);
    }

    @Test
    void testLocalDateTimeConverterWithNull() {
        // Arrange
        Transaction.LocalDateTimeConverter converter = new Transaction.LocalDateTimeConverter();

        // Act & Assert
        assertNull(converter.convert(null));
        assertNull(converter.unconvert(null));
    }

    @Test
    void testTransactionTypeConverter() {
        // Arrange
        Transaction.TransactionTypeConverter converter = new Transaction.TransactionTypeConverter();
        TransactionType type = TransactionType.INCOME;

        // Act
        String converted = converter.convert(type);
        TransactionType unconverted = converter.unconvert(converted);

        // Assert
        assertEquals("INCOME", converted);
        assertEquals(type, unconverted);
    }

    @Test
    void testTransactionTypeConverterWithNull() {
        // Arrange
        Transaction.TransactionTypeConverter converter = new Transaction.TransactionTypeConverter();

        // Act & Assert
        assertNull(converter.convert(null));
        assertNull(converter.unconvert(null));
    }

    @Test
    void testGetCreatedAtWithNull() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act
        LocalDateTime createdAt = transaction.getCreatedAt();

        // Assert
        assertNotNull(createdAt);
        assertTrue(createdAt.isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(createdAt.isAfter(LocalDateTime.now().minusSeconds(1)));
    }
}