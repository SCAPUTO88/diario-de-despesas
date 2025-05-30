package com.example.despesas_projeto.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.despesas_projeto.model.User;
import com.example.despesas_projeto.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
    public class UserRepositoryImpl implements UserRepository {

        private final DynamoDBMapper dynamoDBMapper;

        public UserRepositoryImpl(DynamoDBMapper dynamoDBMapper) {
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        public <S extends User> S save(S user) {
            return saveWithLogs(user);
        }

        @Override
        public <S extends User> Iterable<S> saveAll(Iterable<S> users) {
            users.forEach(this::save);
            return users;
        }

        @Override
        public Optional<User> findById(String email) {
            User user = dynamoDBMapper.load(User.class, email);
            return Optional.ofNullable(user);
        }

        @Override
        public boolean existsById(String email) {
            return findById(email).isPresent();
        }

        @Override
        public Iterable<User> findAll() {
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            return dynamoDBMapper.scan(User.class, scanExpression);
        }

        @Override
        public Iterable<User> findAllById(Iterable<String> emails) {
            return null; // Implementar se necessário
        }

        @Override
        public long count() {
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            return dynamoDBMapper.count(User.class, scanExpression);
        }

        @Override
        public void deleteById(String email) {
            findById(email).ifPresent(user -> dynamoDBMapper.delete(user));
        }

        @Override
        public void delete(User user) {
            dynamoDBMapper.delete(user);
        }

        @Override
        public void deleteAllById(Iterable<? extends String> emails) {
            emails.forEach(this::deleteById);
        }

        @Override
        public void deleteAll(Iterable<? extends User> users) {
            users.forEach(this::delete);
        }

        @Override
        public void deleteAll() {
            findAll().forEach(this::delete);
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return findById(email);
        }

        @Override
        public boolean existsByEmail(String email) {
            return existsById(email);
        }

        @Override
        public <S extends User> S saveWithLogs(S user) {
            log.info("Iniciando salvamento do usuário no DynamoDB");
            log.debug("Dados do usuário a serem salvos: {}", user);

            try {
                User existingUser = dynamoDBMapper.load(User.class, user.getEmail());

                if (existingUser != null) {
                    log.info("Usuário já existe, verificando se precisa atualizar");

                    // Verifica se há alterações antes de atualizar
                    if (!existingUser.getPassword().equals(user.getPassword()) ||
                            !existingUser.getRole().equals(user.getRole())) {

                        Map<String, ExpectedAttributeValue> expectedAttributes = new HashMap<>();
                        expectedAttributes.put("email", new ExpectedAttributeValue(
                                new AttributeValue().withS(user.getEmail())
                        ));

                        dynamoDBMapper.save(user);
                        log.info("Usuário atualizado com sucesso");
                    } else {
                        log.info("Nenhuma alteração necessária para o usuário");
                        return (S) existingUser;
                    }
                } else {
                    dynamoDBMapper.save(user);
                    log.info("Novo usuário salvo com sucesso");
                }

                return user;
            } catch (Exception e) {
                log.error("Erro ao salvar usuário no DynamoDB: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao salvar usuário", e);
            }
        }
    }




