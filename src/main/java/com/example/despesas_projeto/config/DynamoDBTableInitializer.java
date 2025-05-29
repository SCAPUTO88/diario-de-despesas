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

    private static final String TRANSACTIONS_TABLE = "transactions";
    private static final String USERS_TABLE = "users";
    private static final long READ_CAPACITY_UNITS = 5L;
    private static final long WRITE_CAPACITY_UNITS = 5L;
    private static final int TABLE_CREATION_TIMEOUT_MINUTES = 5;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    @PostConstruct
    public void createTables() {
        log.info("Iniciando criação/verificação das tabelas do DynamoDB...");
        createTransactionsTable();
        createUsersTable();
        log.info("Processo de criação/verificação das tabelas concluído.");
    }

    private void createTransactionsTable() {
        try {
            log.info("Verificando se a tabela {} existe...", TRANSACTIONS_TABLE);

            if (tableExists(TRANSACTIONS_TABLE)) {
                log.info("Tabela {} já existe", TRANSACTIONS_TABLE);
                return;
            }

            log.info("Criando tabela {}...", TRANSACTIONS_TABLE);

            List<AttributeDefinition> attributeDefinitions = Arrays.asList(
                    new AttributeDefinition("UserId", ScalarAttributeType.S),
                    new AttributeDefinition("TransactionDate", ScalarAttributeType.S)
            );

            List<KeySchemaElement> keySchema = Arrays.asList(
                    new KeySchemaElement("UserId", KeyType.HASH),
                    new KeySchemaElement("TransactionDate", KeyType.RANGE)
            );

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(TRANSACTIONS_TABLE)
                    .withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withProvisionedThroughput(
                            new ProvisionedThroughput(READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS)
                    );

            amazonDynamoDB.createTable(request);
            waitForTableToBeCreated(TRANSACTIONS_TABLE);
            log.info("Tabela {} criada com sucesso!", TRANSACTIONS_TABLE);

        } catch (Exception e) {
            log.error("Erro ao criar a tabela {}: {}", TRANSACTIONS_TABLE, e.getMessage());
            throw new RuntimeException("Erro ao criar a tabela " + TRANSACTIONS_TABLE, e);
        }
    }

    private void createUsersTable() {
        try {
            log.info("Verificando se a tabela {} existe...", USERS_TABLE);

            if (tableExists(USERS_TABLE)) {
                log.info("Tabela {} já existe", USERS_TABLE);
                return;
            }

            log.info("Criando tabela {}...", USERS_TABLE);

            List<AttributeDefinition> attributeDefinitions = List.of(
                    new AttributeDefinition("email", ScalarAttributeType.S)
            );

            List<KeySchemaElement> keySchema = List.of(
                    new KeySchemaElement("email", KeyType.HASH)
            );

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(USERS_TABLE)
                    .withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withProvisionedThroughput(
                            new ProvisionedThroughput(READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS)
                    );

            amazonDynamoDB.createTable(request);
            waitForTableToBeCreated(USERS_TABLE);
            log.info("Tabela {} criada com sucesso!", USERS_TABLE);

        } catch (Exception e) {
            log.error("Erro ao criar a tabela {}: {}", USERS_TABLE, e.getMessage());
            throw new RuntimeException("Erro ao criar a tabela " + USERS_TABLE, e);
        }
    }

    private boolean tableExists(String tableName) {
        try {
            ListTablesResult tables = amazonDynamoDB.listTables();
            return tables.getTableNames().contains(tableName);
        } catch (Exception e) {
            log.error("Erro ao verificar existência da tabela {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Erro ao verificar existência da tabela " + tableName, e);
        }
    }

    private void waitForTableToBeCreated(String tableName) {
        log.info("Aguardando a tabela {} ficar ativa...", tableName);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (TABLE_CREATION_TIMEOUT_MINUTES * 60 * 1000);

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(5000);
                DescribeTableResult result = amazonDynamoDB.describeTable(tableName);
                String status = result.getTable().getTableStatus();
                log.debug("Status atual da tabela {}: {}", tableName, status);

                if ("ACTIVE".equals(result.getTable().getTableStatus())) {
                    return;
                }
            } catch (Exception e) {
                log.warn("Erro ao verificar status da tabela {}: {}", tableName, e.getMessage());
            }
        }
        throw new RuntimeException("Timeout esperando a tabela " + tableName + " ficar ativa");
    }

}
