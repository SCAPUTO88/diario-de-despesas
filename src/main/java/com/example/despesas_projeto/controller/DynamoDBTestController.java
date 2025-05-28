package com.example.despesas_projeto.controller;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DynamoDBTestController {

    private final AmazonDynamoDB amazonDynamoDB;

    public DynamoDBTestController(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @GetMapping("/test-dynamodb")
    public ResponseEntity<String> testDynamoDBConnection() {
        try {
            ListTablesResult result = amazonDynamoDB.listTables();
            String tables = result.getTableNames().toString();
            return ResponseEntity.ok("Conexão com DynamoDB bem sucedida! Tabelas encontradas: " + tables);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao conectar ao DynamoDB: " + e.getMessage());
        }
    }
}
