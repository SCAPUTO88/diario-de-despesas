package com.example.despesas_projeto.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.despesas_projeto.model.User;
import com.example.despesas_projeto.repository.UserRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final DynamoDBMapper dynamoDBMapper;

    public UserRepositoryImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public <S extends User> S saveWithLogs(S user) {
        log.info("Iniciando salvamento do usuário no DynamoDB");
        log.debug("Dados do usuário a serem salvos: {}", user);
        try {
            dynamoDBMapper.save(user);
            log.info("Usuário salvo com sucesso");
            return user;
        } catch (Exception e) {
            log.error("Erro ao salvar usuário no DynamoDB: {}", e.getMessage(), e);
            throw e;
        }
    }
}


