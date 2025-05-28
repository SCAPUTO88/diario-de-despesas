package com.example.despesas_projeto.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
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
@Repository
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
        log.info("Salvando transação: userId={}, date={}, type={}, value={}",
                entity.getUserId(),
                entity.getTransactionDate(),
                entity.getType(),
                entity.getValue());

        try {
            dynamoDBMapper.save(entity);
            log.info("Transação salva com sucesso");
            return entity;
        } catch (Exception e) {
            log.error("Erro ao salvar transação", e);
            throw e;
        }
    }

    @Override
    public void delete(Transaction entity) {
        dynamoDBMapper.delete(entity);
    }
}