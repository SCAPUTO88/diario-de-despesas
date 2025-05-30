package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.dto.AuthenticationRequestDTO;
import com.example.despesas_projeto.dto.AuthenticationResponseDTO;
import com.example.despesas_projeto.dto.RegisterRequestDTO;
import com.example.despesas_projeto.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_WithValidData_ShouldReturnOk() throws Exception {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthenticationResponseDTO response = AuthenticationResponseDTO.builder()
                .token("dummy-token")
                .message("Registro realizado com sucesso")
                .build();

        when(authenticationService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-token"))
                .andExpect(jsonPath("$.message").value("Registro realizado com sucesso"));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnOk() throws Exception {
        // Arrange
        AuthenticationRequestDTO request = AuthenticationRequestDTO.builder()
                .email("test@example.com")
                .password("password")
                .build();

        AuthenticationResponseDTO response = AuthenticationResponseDTO.builder()
                .token("dummy-token")
                .message("Autenticação realizada com sucesso")
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-token"))
                .andExpect(jsonPath("$.message").value("Autenticação realizada com sucesso"));
    }
}

