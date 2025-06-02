package com.example.despesas_projeto.service;

import com.example.despesas_projeto.dto.AuthenticationRequestDTO;
import com.example.despesas_projeto.dto.AuthenticationResponseDTO;
import com.example.despesas_projeto.dto.RegisterRequestDTO;
import com.example.despesas_projeto.enums.Role;
import com.example.despesas_projeto.model.User;
import com.example.despesas_projeto.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        log.info("Iniciando processo de registro para usuário: {}", request.getEmail());

        try {
            log.debug("Construindo objeto User com os dados: {}", request);
            var user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.valueOf(request.getRole()).name())
                    .build();

            log.debug("Objeto User construído: {}", user);

            log.info("Tentando salvar usuário no DynamoDB");
            userRepository.saveWithLogs(user);
            log.info("Usuário salvo com sucesso");

            // Não gerar token no registro
            return AuthenticationResponseDTO.builder()
                    .message("Usuário registrado com sucesso")
                    .build();
        } catch (Exception e) {
            log.error("Erro durante o processo de registro: {}", e.getMessage(), e);
            throw e;
        }

    }



    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        log.debug("Tentando autenticar usuário: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.debug("Autenticação bem sucedida para: {}", request.getEmail());

            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            var jwtToken = jwtService.generateToken(user);

            log.debug("Token JWT gerado com sucesso");
            return AuthenticationResponseDTO.builder()
                    .token(jwtToken)
                    .build();
        } catch (AuthenticationException e) {
            log.error("Erro de autenticação: {}", e.getMessage());
            throw e;
        }
    }


}
