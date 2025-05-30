
package com.example.despesas_projeto.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testBuilder() {
        // Arrange & Act
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .role("ADMIN")
                .build();

        // Assert
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void testGetAuthoritiesWithRole() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .role("ADMIN")
                .build();

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetAuthoritiesWithNullRole() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testUserDetails() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        // Act & Assert
        assertEquals("test@example.com", user.getUsername());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void testNoArgsConstructor() {
        // Act
        User user = new User();

        // Assert
        assertNotNull(user);
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
    }

    @Test
    void testAllArgsConstructor() {
        // Act
        User user = new User("test@example.com", "password123", "ADMIN");

        // Assert
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        User user1 = new User("test@example.com", "password123", "ADMIN");
        User user2 = new User("test@example.com", "password123", "ADMIN");
        User user3 = new User("other@example.com", "password123", "ADMIN");

        // Assert
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }
}