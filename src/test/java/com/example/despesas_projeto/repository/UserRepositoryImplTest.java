
package com.example.despesas_projeto.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.example.despesas_projeto.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl(dynamoDBMapper);
    }

    @Test
    void save_NewUser_ShouldSaveSuccessfully() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .role("USER")
                .build();

        when(dynamoDBMapper.load(User.class, "test@example.com")).thenReturn(null);

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        verify(dynamoDBMapper).save(user);
    }

    @Test
    void save_ExistingUserWithChanges_ShouldUpdate() {
        // Arrange
        User existingUser = User.builder()
                .email("test@example.com")
                .password("oldPassword")
                .role("USER")
                .build();

        User newUser = User.builder()
                .email("test@example.com")
                .password("newPassword")
                .role("ADMIN")
                .build();

        when(dynamoDBMapper.load(User.class, "test@example.com")).thenReturn(existingUser);

        // Act
        User updatedUser = userRepository.save(newUser);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("newPassword", updatedUser.getPassword());
        assertEquals("ADMIN", updatedUser.getRole());
        verify(dynamoDBMapper).save(newUser);
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .role("USER")
                .build();

        when(dynamoDBMapper.load(User.class, "test@example.com")).thenReturn(user);

        // Act
        Optional<User> result = userRepository.findById("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findById_NonExistingUser_ShouldReturnEmpty() {
        // Arrange
        when(dynamoDBMapper.load(User.class, "nonexistent@example.com")).thenReturn(null);

        // Act
        Optional<User> result = userRepository.findById("nonexistent@example.com");

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    void deleteById_ExistingUser_ShouldDelete() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .build();

        when(dynamoDBMapper.load(User.class, "test@example.com")).thenReturn(user);

        // Act
        userRepository.deleteById("test@example.com");

        // Assert
        verify(dynamoDBMapper).delete(user);
    }

    @Test
    void existsByEmail_ExistingUser_ShouldReturnTrue() {
        // Arrange
        when(dynamoDBMapper.load(User.class, "test@example.com"))
                .thenReturn(User.builder().email("test@example.com").build());

        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }
}