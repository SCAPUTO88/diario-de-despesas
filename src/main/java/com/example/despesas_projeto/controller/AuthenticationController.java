package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.dto.AuthenticationRequestDTO;
import com.example.despesas_projeto.dto.AuthenticationResponseDTO;
import com.example.despesas_projeto.dto.RegisterRequestDTO;
import com.example.despesas_projeto.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        log.info("Recebendo requisição de registro para o email: {}", request.getEmail());
        log.debug("Dados completos do registro: {}", request);
        try {
            var response = authenticationService.register(request);
            log.info("Registro concluído com sucesso para o email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro durante o registro: {}", e.getMessage(), e);
            throw e;
        }
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody AuthenticationRequestDTO request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
