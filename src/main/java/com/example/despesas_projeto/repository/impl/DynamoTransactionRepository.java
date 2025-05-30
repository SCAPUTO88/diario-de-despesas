package com.example.despesas_projeto.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.despesas_projeto.domain.TransactionFilter;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.repository.TransactionRepository;
import com.example.despesas_projeto.utils.DynamoDBPaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository("dynamoTransactionRepository")
public class DynamoTransactionRepository implements TransactionRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public DynamoTransactionRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public Optional<Transaction> findById(String userId, LocalDateTime transactionDate) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionDate(transactionDate);
        return Optional.ofNullable(dynamoDBMapper.load(transaction));
    }

    @EnableScan
    @Override
    public List<Transaction> findByUserId(String userId) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);

        var result = DynamoDBPaginationUtil.queryPage(
                dynamoDBMapper,
                Transaction.class,
                transaction,
                10,
                null
        );

        return result.getItems();
    }

    @EnableScan
    @Override
    public List<Transaction> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":startDate", new AttributeValue().withS(startDate.toString()));
        eav.put(":endDate", new AttributeValue().withS(endDate.toString()));

        DynamoDBQueryExpression<Transaction> queryExpression = new DynamoDBQueryExpression<Transaction>()
                .withHashKeyValues(transaction)
                .withFilterExpression("#transactionDate BETWEEN :startDate AND :endDate")
                .withExpressionAttributeNames(Map.of("#transactionDate", "TransactionDate"))
                .withExpressionAttributeValues(eav)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Transaction.class, queryExpression);
    }

    @EnableScan
    @Override
    public List<Transaction> findByUserIdAndType(String userId, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":type", new AttributeValue().withS(type.name()));

        DynamoDBQueryExpression<Transaction> queryExpression = new DynamoDBQueryExpression<Transaction>()
                .withHashKeyValues(transaction)
                .withFilterExpression("#type = :type")
                .withExpressionAttributeNames(Map.of("#type", "Type"))
                .withExpressionAttributeValues(eav)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Transaction.class, queryExpression);
    }

    @EnableScan
    @Override
    public List<Transaction> findByUserIdAndCategory(String userId, String category) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":category", new AttributeValue().withS(category));

        DynamoDBQueryExpression<Transaction> queryExpression = new DynamoDBQueryExpression<Transaction>()
                .withHashKeyValues(transaction)
                .withFilterExpression("#category = :category")
                .withExpressionAttributeNames(Map.of("#category", "Category"))
                .withExpressionAttributeValues(eav)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Transaction.class, queryExpression);
    }

    @Override
    public boolean existsById(String userId, LocalDateTime transactionDate) {
        return findById(userId, transactionDate).isPresent();
    }

    @Override
    public void deleteById(String userId, LocalDateTime transactionDate) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionDate(transactionDate);
        dynamoDBMapper.delete(transaction);
    }

    @Override
    public <S extends Transaction> S save(S entity) {
        log.info("Salvando transação: userId={}, type={}, value={}",
                entity.getUserId(),
                entity.getType(),
                entity.getValue());

        try {
            if (entity.getTransactionDate() == null) {
                entity.setTransactionDate(LocalDateTime.now());
            }

            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }

            if (existsById(entity.getUserId(), entity.getTransactionDate())) {
                LocalDateTime newDate = entity.getTransactionDate();
                while (existsById(entity.getUserId(), newDate)) {
                    newDate = newDate.plusNanos(1000);
                }
                entity.setTransactionDate(newDate);
                log.debug("Ajustada data da transação para evitar duplicidade: {}", newDate);
            }

            dynamoDBMapper.save(entity);
            log.info("Transação salva com sucesso. ID: {}, Data: {}",
                    entity.getUserId(),
                    entity.getTransactionDate());
            return entity;
        } catch (Exception e) {
            log.error("Erro ao salvar transação", e);
            throw e;
        }
    }

    @EnableScan
    @Override
    public List<Transaction> findByFilters(TransactionFilter filter) {
        log.debug("Buscando transações com filtros: {}", filter);

        if (filter == null || filter.getUserId() == null) {
            throw new IllegalArgumentException("Filtro e userId são obrigatórios");
        }

        Transaction hashKey = new Transaction();
        hashKey.setUserId(filter.getUserId());

        // Construir a expressão de filtro dinamicamente
        StringBuilder filterExpression = new StringBuilder();
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        Map<String, String> expressionNames = new HashMap<>();

        // Filtro por tipo
        if (filter.getType() != null) {
            expressionNames.put("#type", "Type");
            expressionValues.put(":type", new AttributeValue().withS(filter.getType().name()));
            filterExpression.append("#type = :type");
        }

        // Filtro por categoria
        if (filter.getCategory() != null) {
            if (filterExpression.length() > 0) {
                filterExpression.append(" AND ");
            }
            expressionNames.put("#category", "Category");
            expressionValues.put(":category", new AttributeValue().withS(filter.getCategory()));
            filterExpression.append("#category = :category");
        }

        DynamoDBQueryExpression<Transaction> queryExpression = new DynamoDBQueryExpression<Transaction>()
                .withHashKeyValues(hashKey);

        if (filterExpression.length() > 0) {
            queryExpression.withFilterExpression(filterExpression.toString())
                    .withExpressionAttributeNames(expressionNames)
                    .withExpressionAttributeValues(expressionValues);
        }

        queryExpression.withConsistentRead(false);


        try {
            PaginatedQueryList<Transaction> result = dynamoDBMapper.query(Transaction.class, queryExpression);
            return new ArrayList<>(result);
        } catch (Exception e) {
            log.error("Erro ao buscar transações com filtros", e);
            throw new RuntimeException("Erro ao buscar transações", e);
        }

    }

    private void addAndOperator(StringBuilder filterExpression) {
        if (filterExpression.length() > 0) {
            filterExpression.append(" AND ");
        }
    }


    @Override
    public void delete(Transaction entity) {
        dynamoDBMapper.delete(entity);
    }
}