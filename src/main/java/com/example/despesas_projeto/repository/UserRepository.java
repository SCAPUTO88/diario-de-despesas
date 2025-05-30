package com.example.despesas_projeto.repository;

import com.example.despesas_projeto.model.User;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    <S extends User> S saveWithLogs(S user);
}



