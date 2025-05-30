package com.example.despesas_projeto.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DynamoDBHealthIndicator implements HealthIndicator {

    private final AmazonDynamoDB dynamoDB;

    public DynamoDBHealthIndicator(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public Health health() {
        try {
            ListTablesResult tables = dynamoDB.listTables();
            return Health.up()
                    .withDetail("tables", tables.getTableNames())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
