package com.example.despesas_projeto.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "users")
public class User implements UserDetails {

    @DynamoDBHashKey
    @DynamoDBAttribute(attributeName = "email")
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    private String email;

    @DynamoDBAttribute(attributeName = "password")
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    private String password;

    @DynamoDBAttribute(attributeName = "role")
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    private String role;

    @DynamoDBIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")));
    }

    @DynamoDBIgnore
    @Override
    public String getUsername() {
        return email;
    }

    @DynamoDBIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @DynamoDBIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @DynamoDBIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @DynamoDBIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
