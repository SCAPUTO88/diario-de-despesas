package com.example.despesas_projeto.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.utils.BigDecimalConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@DynamoDBTable(tableName = "Transactions")
public class Transaction {

    public Transaction() {

    }

    @DynamoDBHashKey(attributeName = "UserId")
    private String userId;

    @DynamoDBRangeKey(attributeName = "TransactionDate")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime transactionDate;

    @DynamoDBAttribute(attributeName = "Description")
    private String description;

    @DynamoDBAttribute(attributeName = "Value")
    @DynamoDBTypeConverted(converter = BigDecimalConverter.class)
    private BigDecimal value;

    @DynamoDBTypeConverted(converter = TransactionTypeConverter.class)
    @DynamoDBAttribute(attributeName = "Type")
    private TransactionType type;

    @DynamoDBAttribute(attributeName = "Category")
    private String category;

    @DynamoDBAttribute(attributeName = "Tags")
    private List<String> tags;

    @DynamoDBAttribute(attributeName = "CreatedAt")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt;

    static public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public String convert(LocalDateTime object) {
            if (object == null) return null;
            return object.format(FORMATTER);
        }

        @Override
        public LocalDateTime unconvert(String object) {
            if (object == null) return null;
            return LocalDateTime.parse(object, FORMATTER);
        }

    }

    // Conversor para TransactionType
    static public class TransactionTypeConverter implements DynamoDBTypeConverter<String, TransactionType> {
        @Override
        public String convert(TransactionType type) {
            return type != null ? type.name() : null;
        }

        @Override
        public TransactionType unconvert(String stringValue) {
            return stringValue != null ? TransactionType.valueOf(stringValue) : null;
        }
    }

    @DynamoDBAttribute(attributeName = "CreatedAt")
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    public LocalDateTime getCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        return createdAt;
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }
}