package com.example.despesas_projeto.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class DynamoDBTableInitializer {

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @PostConstruct
    public void createTables() {
        try {
            log.info("Verificando se a tabela Transactions existe...");

            ListTablesResult tables = amazonDynamoDB.listTables();
            if (tables.getTableNames().contains("Transactions")) {
                log.info("Tabela Transactions já existe");

                return;
            }

            log.info("Criando tabela Transactions...");

            List<AttributeDefinition> attributeDefinitions = Arrays.asList(
                    new AttributeDefinition("UserId", ScalarAttributeType.S),
                    new AttributeDefinition("TransactionDate", ScalarAttributeType.S)
            );

            List<KeySchemaElement> keySchema = Arrays.asList(
                    new KeySchemaElement("UserId", KeyType.HASH),
                    new KeySchemaElement("TransactionDate", KeyType.RANGE)
            );

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName("Transactions")
                    .withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

            amazonDynamoDB.createTable(request);
            log.info("Aguardando a tabela ficar ativa...");
            waitForTableToBeCreated("Transactions");
            log.info("Tabela Transactions criada com sucesso!");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar a tabela", e);
        }
    }

    private void waitForTableToBeCreated(String tableName) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(5000);
                DescribeTableResult result = amazonDynamoDB.describeTable(tableName);
                String status = result.getTable().getTableStatus();
                log.debug("Status atual da tabela: {}", status);

                if (result.getTable().getTableStatus().equals("ACTIVE")) {
                    return;
                }
            } catch (Exception e) {
                log.warn("Erro ao verificar status da tabela: ", e);

            }
        }
        throw new RuntimeException("Timeout esperando a tabela ficar ativa");
    }
}
