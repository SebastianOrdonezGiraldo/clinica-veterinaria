package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.ForgotPasswordRequestDTO;
import com.clinica.veterinaria.dto.ResetPasswordWithTokenDTO;
import com.clinica.veterinaria.entity.PasswordResetToken;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.PasswordResetTokenRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PasswordResetController
 */
@DisplayName("Tests de Integración - PasswordResetController")
@SuppressWarnings({"null"}) // Suprimir warnings de null safety en tests
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PasswordResetControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario testUsuario;
    private Propietario testPropietario;
    private PasswordResetToken tokenValido;
    private PasswordResetToken tokenExpirado;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        
        // Limpiar tokens anteriores
        tokenRepository.deleteAll();

        // Eliminar usuarios de prueba si existen (para evitar conflictos)
        usuarioRepository.findByEmail("passwordtest@test.com")
            .ifPresent(usuarioRepository::delete);
        propietarioRepository.findByEmail("propietariotest@test.com")
            .ifPresent(propietarioRepository::delete);

        // Crear usuario de prueba
        testUsuario = Usuario.builder()
            .nombre("Usuario Test Password")
            .email("passwordtest@test.com")
            .password(passwordEncoder.encode("password123"))
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();
        testUsuario = usuarioRepository.save(testUsuario);

        // Crear propietario de prueba
        testPropietario = Propietario.builder()
            .nombre("Propietario Test Password")
            .email("propietariotest@test.com")
            .password(passwordEncoder.encode("password123"))
            .activo(true)
            .build();
        testPropietario = propietarioRepository.save(testPropietario);

        // Crear token válido
        tokenValido = PasswordResetToken.builder()
            .token("valid-test-token-123")
            .email(testUsuario.getEmail())
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().plusHours(24))
            .usado(false)
            .build();
        tokenValido = tokenRepository.save(tokenValido);

        // Crear token expirado
        tokenExpirado = PasswordResetToken.builder()
            .token("expired-test-token-456")
            .email(testUsuario.getEmail())
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().minusHours(1))
            .usado(false)
            .build();
        tokenExpirado = tokenRepository.save(tokenExpirado);
    }

    @Test
    @DisplayName("POST /api/public/password/forgot-usuario - Debe solicitar recuperación exitosamente")
    void testForgotPasswordUsuario_Success() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
            .email(testUsuario.getEmail())
            .build();

        mockMvc.perform(post("/api/public/password/forgot-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/public/password/forgot-cliente - Debe solicitar recuperación exitosamente")
    void testForgotPasswordCliente_Success() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
            .email(testPropietario.getEmail())
            .build();

        mockMvc.perform(post("/api/public/password/forgot-cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/public/password/reset - Debe resetear contraseña con token válido")
    void testResetPassword_ValidToken() throws Exception {
        ResetPasswordWithTokenDTO request = ResetPasswordWithTokenDTO.builder()
            .token(tokenValido.getToken())
            .password("newPassword123")
            .build();

        mockMvc.perform(post("/api/public/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());

        // Forzar flush y refrescar desde la base de datos
        tokenRepository.flush();
        PasswordResetToken tokenActualizado = tokenRepository.findByToken(tokenValido.getToken()).orElse(null);
        assertNotNull(tokenActualizado, "El token debe existir");
        assertTrue(tokenActualizado.getUsado(), "El token debe estar marcado como usado");
    }

    @Test
    @DisplayName("POST /api/public/password/reset - Debe rechazar token expirado")
    void testResetPassword_ExpiredToken() throws Exception {
        ResetPasswordWithTokenDTO request = ResetPasswordWithTokenDTO.builder()
            .token(tokenExpirado.getToken())
            .password("newPassword123")
            .build();

        mockMvc.perform(post("/api/public/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnprocessableEntity()) // BusinessException retorna 422
            .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("expirado")));
    }

    @Test
    @DisplayName("POST /api/public/password/reset - Debe rechazar token inexistente")
    void testResetPassword_InvalidToken() throws Exception {
        ResetPasswordWithTokenDTO request = ResetPasswordWithTokenDTO.builder()
            .token("noexiste-token-123")
            .password("newPassword123")
            .build();

        mockMvc.perform(post("/api/public/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnprocessableEntity()) // BusinessException retorna 422
            .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("POST /api/public/password/reset - Debe rechazar contraseña muy corta")
    void testResetPassword_ShortPassword() throws Exception {
        ResetPasswordWithTokenDTO request = ResetPasswordWithTokenDTO.builder()
            .token(tokenValido.getToken())
            .password("12345") // Menos de 6 caracteres
            .build();

        mockMvc.perform(post("/api/public/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest()); // Validación de Bean Validation retorna 400
    }

    @Test
    @DisplayName("GET /api/public/password/validate-token - Debe validar token válido")
    void testValidateToken_Valid() throws Exception {
        mockMvc.perform(get("/api/public/password/validate-token")
                .param("token", tokenValido.getToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.expiresInHours").exists());
    }

    @Test
    @DisplayName("GET /api/public/password/validate-token - Debe rechazar token expirado")
    void testValidateToken_Expired() throws Exception {
        mockMvc.perform(get("/api/public/password/validate-token")
                .param("token", tokenExpirado.getToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("GET /api/public/password/validate-token - Debe rechazar token inexistente")
    void testValidateToken_Invalid() throws Exception {
        mockMvc.perform(get("/api/public/password/validate-token")
                .param("token", "noexiste-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("POST /api/public/password/reset - No debe permitir usar el mismo token dos veces")
    void testResetPassword_TokenReuse() throws Exception {
        ResetPasswordWithTokenDTO request = ResetPasswordWithTokenDTO.builder()
            .token(tokenValido.getToken())
            .password("newPassword123")
            .build();

        // Primera vez - debe funcionar
        mockMvc.perform(post("/api/public/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk());

        // Forzar flush para asegurar que el token se marca como usado
        tokenRepository.flush();

        // Segunda vez - debe fallar porque el token ya fue usado
        mockMvc.perform(post("/api/public/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnprocessableEntity()) // BusinessException retorna 422
            .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("expirado")));
    }

    @Test
    @DisplayName("POST /api/public/password/forgot-usuario - Debe validar email requerido")
    void testForgotPasswordUsuario_EmptyEmail() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
            .email("")
            .build();

        mockMvc.perform(post("/api/public/password/forgot-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest()); // Validación de Bean Validation retorna 400
    }

    @Test
    @DisplayName("POST /api/public/password/forgot-usuario - Debe validar formato de email")
    void testForgotPasswordUsuario_InvalidEmail() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
            .email("email-invalido")
            .build();

        mockMvc.perform(post("/api/public/password/forgot-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest()); // Validación de Bean Validation retorna 400
    }

    @Test
    @DisplayName("POST /api/public/password/forgot-usuario - Rate limiting básico (múltiples solicitudes)")
    void testForgotPasswordUsuario_RateLimiting() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
            .email(testUsuario.getEmail())
            .build();

        // Hacer múltiples solicitudes rápidas
        int solicitudes = 5;
        int exitosas = 0;
        
        for (int i = 0; i < solicitudes; i++) {
            try {
                mockMvc.perform(post("/api/public/password/forgot-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                    .andExpect(status().isOk());
                exitosas++;
            } catch (Exception e) {
                // Si hay rate limiting, algunas solicitudes pueden fallar
            }
        }

        // Verificar que al menos algunas solicitudes fueron exitosas
        // (Nota: Este test es básico, un rate limiting real bloquearía después de X solicitudes)
        assertTrue(exitosas > 0, "Al menos una solicitud debe ser exitosa");
        
        // Verificar que se crearon tokens (aunque pueden ser limitados)
        long tokensCreados = tokenRepository.count();
        assertTrue(tokensCreados > 0, "Debe haber al menos un token creado");
    }

    @Test
    @DisplayName("POST /api/public/password/forgot-usuario - Debe invalidar tokens anteriores")
    void testForgotPasswordUsuario_InvalidarTokensAnteriores() throws Exception {
        // Crear un token anterior válido
        PasswordResetToken tokenAnterior = PasswordResetToken.builder()
            .token("anterior-token-123")
            .email(testUsuario.getEmail())
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().plusHours(12))
            .usado(false)
            .build();
        tokenAnterior = tokenRepository.save(tokenAnterior);
        
        // Verificar que el token se guardó correctamente
        assertNotNull(tokenAnterior.getId());
        assertFalse(tokenAnterior.getUsado());

        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
            .email(testUsuario.getEmail())
            .build();

        // Solicitar nuevo token
        mockMvc.perform(post("/api/public/password/forgot-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk());

        // Refrescar el token desde la base de datos
        tokenRepository.flush();
        PasswordResetToken tokenAnteriorActualizado = tokenRepository.findByToken("anterior-token-123").orElse(null);
        
        // El token anterior debería estar marcado como usado
        assertNotNull(tokenAnteriorActualizado, "El token anterior debe existir");
        assertTrue(tokenAnteriorActualizado.getUsado(), "El token anterior debe estar marcado como usado");
    }
}

