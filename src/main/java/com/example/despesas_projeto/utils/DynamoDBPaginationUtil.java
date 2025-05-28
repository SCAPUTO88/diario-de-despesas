package com.example.despesas_projeto.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.despesas_projeto.model.Transaction;

import java.util.List;
import java.util.Map;

public class DynamoDBPaginationUtil {

    private DynamoDBPaginationUtil() {
    }

    public static <T> PaginatedResult<T> queryPage(
            DynamoDBMapper dynamoDBMapper,
            Class<T> clazz,
            T hashKeyObject,
            Integer limit,
            Map<String, AttributeValue> lastEvaluatedKey) {

        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                .withHashKeyValues(hashKeyObject)
                .withLimit(limit != null ? limit : 10)
                .withExclusiveStartKey(lastEvaluatedKey)
                .withConsistentRead(false);

        var result = dynamoDBMapper.queryPage(clazz, queryExpression);

        return new PaginatedResult<>(
                result.getResults(),
                result.getLastEvaluatedKey()
        );
    }

    public static class PaginatedResult<T> {
        private final List<T> items;
        private final Map<String, AttributeValue> lastEvaluatedKey;

        public PaginatedResult(List<T> items, Map<String, AttributeValue> lastEvaluatedKey) {
            this.items = items;
            this.lastEvaluatedKey = lastEvaluatedKey;
        }

        public List<T> getItems() {
            return items;
        }

        public Map<String, AttributeValue> getLastEvaluatedKey() {
            return lastEvaluatedKey;
        }

        public boolean hasMoreResults() {
            return lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty();
        }
    }

//    public DynamoDBPaginationUtil.PaginatedResult<Transaction> findByUserIdPaginated(
//            String userId,
//            Integer limit,
//            Map<String, AttributeValue> lastEvaluatedKey) {
//
//        Transaction transaction = new Transaction();
//        transaction.setUserId(userId);
//
//        return DynamoDBPaginationUtil.queryPage(
//                dynamoDBMapper,
//                Transaction.class,
//                transaction,
//                limit,
//                lastEvaluatedKey
//        );
//    }
}