package com.example.despesas_projeto.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.example.despesas_projeto.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoleConverter implements DynamoDBTypeConverter<String, Role> {

    @Override
    public String convert(Role role) {
        log.debug("Convertendo Role para String: {}", role);
        if (role == null) {
            log.debug("Role é null, retornando USER");
            return Role.USER.name();
        }
        log.debug("Retornando role: {}", role.name());
        return role.name();
    }

    @Override
    public Role unconvert(String s) {
        log.debug("Convertendo String para Role: {}", s);
        if (s == null) {
            log.debug("String é null, retornando USER");
            return Role.USER;
        }
        try {
            Role role = Role.valueOf(s);
            log.debug("Role convertida com sucesso: {}", role);
            return role;
        } catch (IllegalArgumentException e) {
            log.error("Erro ao converter string para Role: {}", s, e);
            return Role.USER;
        }
    }
}


