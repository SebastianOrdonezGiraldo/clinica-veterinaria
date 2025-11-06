package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.LoginRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController
 */
@DisplayName("Tests de Integración - AuthController")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso con credenciales correctas")
    void testLogin_Success() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("admin@test.com")
            .password("admin123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.type").value("Bearer"))
            .andExpect(jsonPath("$.usuario.email").value("admin@test.com"))
            .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar credenciales incorrectas")
    void testLogin_InvalidPassword() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("admin@test.com")
            .password("wrongpassword")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 400
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe rechazar usuario inexistente")
    void testLogin_UserNotFound() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("noexiste@test.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 400
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe validar email vacío")
    void testLogin_EmptyEmail() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe validar password vacío")
    void testLogin_EmptyPassword() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("admin@test.com")
            .password("")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Debe validar formato de email")
    void testLogin_InvalidEmailFormat() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("emailinvalido")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso con VET")
    void testLogin_VetRole() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("vet@test.com")
            .password("vet123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.usuario.rol").value("VET"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso con RECEPCION")
    void testLogin_RecepcionRole() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
            .email("recepcion@test.com")
            .password("recep123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.usuario.rol").value("RECEPCION"));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Debe validar token correcto")
    void testValidateToken_Valid() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                .param("token", adminToken))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Debe rechazar token inválido")
    void testValidateToken_Invalid() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                .param("token", "tokeninvalido123"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }
}

