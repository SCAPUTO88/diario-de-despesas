package com.example.despesas_projeto.repository;

import com.example.despesas_projeto.model.User;

public interface UserRepositoryCustom {
    <S extends User> S saveWithLogs(S user);
}
