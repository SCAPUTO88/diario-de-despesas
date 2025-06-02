package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.dto.AuthenticationRequestDTO;
import com.example.despesas_projeto.dto.AuthenticationResponseDTO;
import com.example.despesas_projeto.dto.RegisterRequestDTO;
import com.example.despesas_projeto.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequestDTO request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registro realizado com sucesso! Faça login para continuar.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("Erro ao registrar usuário", e);
            result.rejectValue("email", "error.email", "Erro ao registrar usuário: " + e.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AuthenticationRequestDTO request,
                        RedirectAttributes redirectAttributes,
                        HttpServletResponse response) {
        try {
            AuthenticationResponseDTO authResponse = authService.authenticate(request);
            // Adicionar o token como cookie
            Cookie cookie = new Cookie("jwt", authResponse.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(24 * 60 * 60); // 24 horas
            response.addCookie(cookie);

            return "redirect:/";
        } catch (Exception e) {
            log.error("Erro ao fazer login", e);
            redirectAttributes.addFlashAttribute("error", "Email ou senha inválidos");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/")
    public String root() {
        // Se já estiver autenticado, vai para o dashboard
        // Caso contrário, redireciona para o login
        return "redirect:/auth/login";
    }


    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Remover o cookie JWT
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/auth/login";
    }
}

