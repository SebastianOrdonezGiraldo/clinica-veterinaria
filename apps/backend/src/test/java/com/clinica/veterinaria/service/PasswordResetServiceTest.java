package com.clinica.veterinaria.service;

import com.clinica.veterinaria.entity.PasswordResetToken;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.PasswordResetTokenRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PasswordResetService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de PasswordResetService")
@SuppressWarnings({"null"})
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Usuario usuario;
    private Propietario propietario;
    private PasswordResetToken tokenValido;
    private PasswordResetToken tokenExpirado;
    private PasswordResetToken tokenUsado;

    @BeforeEach
    void setUp() {
        // Configurar valores por defecto
        ReflectionTestUtils.setField(passwordResetService, "expirationHours", 24);
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:5173");

        usuario = Usuario.builder()
            .id(1L)
            .nombre("Usuario Test")
            .email("usuario@test.com")
            .password("$2a$10$encodedPassword")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        propietario = Propietario.builder()
            .id(1L)
            .nombre("Propietario Test")
            .email("propietario@test.com")
            .password("$2a$10$encodedPassword")
            .activo(true)
            .build();

        tokenValido = PasswordResetToken.builder()
            .id(1L)
            .token("valid-token-123")
            .email("usuario@test.com")
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().plusHours(24))
            .usado(false)
            .build();

        tokenExpirado = PasswordResetToken.builder()
            .id(2L)
            .token("expired-token-456")
            .email("usuario@test.com")
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().minusHours(1))
            .usado(false)
            .build();

        tokenUsado = PasswordResetToken.builder()
            .id(3L)
            .token("used-token-789")
            .email("usuario@test.com")
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().plusHours(24))
            .usado(true)
            .build();
    }

    @Test
    @DisplayName("Debe solicitar recuperación de contraseña para usuario exitosamente")
    void testSolicitarRecuperacionUsuario_Success() {
        // Arrange
        String email = "usuario@test.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(tokenRepository.findValidTokenByEmailAndUserType(eq(email), eq("USUARIO"), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValido);
        when(emailService.enviarEmailRecuperacionPassword(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);

        // Act
        boolean resultado = passwordResetService.solicitarRecuperacionUsuario(email);

        // Assert
        assertTrue(resultado);
        verify(usuarioRepository, times(1)).findByEmail(email);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).enviarEmailRecuperacionPassword(
            eq(email), eq(usuario.getNombre()), anyString(), eq("USUARIO"));
    }

    @Test
    @DisplayName("Debe solicitar recuperación de contraseña para propietario exitosamente")
    void testSolicitarRecuperacionPropietario_Success() {
        // Arrange
        String email = "propietario@test.com";
        when(propietarioRepository.findByEmail(email)).thenReturn(Optional.of(propietario));
        when(tokenRepository.findValidTokenByEmailAndUserType(eq(email), eq("PROPIETARIO"), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValido);
        when(emailService.enviarEmailRecuperacionPassword(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);

        // Act
        boolean resultado = passwordResetService.solicitarRecuperacionPropietario(email);

        // Assert
        assertTrue(resultado);
        verify(propietarioRepository, times(1)).findByEmail(email);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).enviarEmailRecuperacionPassword(
            eq(email), eq(propietario.getNombre()), anyString(), eq("PROPIETARIO"));
    }

    @Test
    @DisplayName("Debe retornar true incluso si el email no existe (seguridad)")
    void testSolicitarRecuperacion_EmailNoExiste() {
        // Arrange
        String email = "noexiste@test.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean resultado = passwordResetService.solicitarRecuperacionUsuario(email);

        // Assert
        assertTrue(resultado); // Por seguridad, siempre retorna true
        verify(usuarioRepository, times(1)).findByEmail(email);
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).enviarEmailRecuperacionPassword(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe resetear contraseña con token válido para usuario")
    void testResetearPasswordConToken_Usuario_Success() {
        // Arrange
        String token = "valid-token-123";
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newEncodedPassword";

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenValido));
        // Se llama 2 veces: una para resetear y otra para enviar email de confirmación
        when(usuarioRepository.findByEmail(tokenValido.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(tokenRepository).markAsUsed(tokenValido.getId());
        when(emailService.enviarEmailCambioPasswordUsuario(anyString(), anyString(), anyBoolean()))
            .thenReturn(true);

        // Act
        assertDoesNotThrow(() -> passwordResetService.resetearPasswordConToken(token, newPassword));

        // Assert
        verify(tokenRepository, times(1)).findByToken(token);
        // Se llama 2 veces: una para resetear y otra para enviar email de confirmación
        verify(usuarioRepository, atLeast(1)).findByEmail(tokenValido.getEmail());
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(usuarioRepository, times(1)).save(usuario);
        verify(tokenRepository, times(1)).markAsUsed(tokenValido.getId());
        verify(emailService, times(1)).enviarEmailCambioPasswordUsuario(
            eq(usuario.getEmail()), eq(usuario.getNombre()), eq(false));
    }

    @Test
    @DisplayName("Debe resetear contraseña con token válido para propietario")
    void testResetearPasswordConToken_Propietario_Success() {
        // Arrange
        PasswordResetToken tokenPropietario = PasswordResetToken.builder()
            .id(4L)
            .token("propietario-token-123")
            .email("propietario@test.com")
            .userType("PROPIETARIO")
            .expiresAt(LocalDateTime.now().plusHours(24))
            .usado(false)
            .build();

        String token = "propietario-token-123";
        String newPassword = "newPassword123";
        String encodedPassword = "$2a$10$newEncodedPassword";

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenPropietario));
        // Se llama 2 veces: una para resetear y otra para enviar email de confirmación
        when(propietarioRepository.findByEmail(tokenPropietario.getEmail())).thenReturn(Optional.of(propietario));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(propietarioRepository.save(any(Propietario.class))).thenReturn(propietario);
        doNothing().when(tokenRepository).markAsUsed(tokenPropietario.getId());
        when(emailService.enviarEmailCambioPasswordCliente(anyString(), anyString()))
            .thenReturn(true);

        // Act
        assertDoesNotThrow(() -> passwordResetService.resetearPasswordConToken(token, newPassword));

        // Assert
        verify(tokenRepository, times(1)).findByToken(token);
        // Se llama 2 veces: una para resetear y otra para enviar email de confirmación
        verify(propietarioRepository, atLeast(1)).findByEmail(tokenPropietario.getEmail());
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(propietarioRepository, times(1)).save(propietario);
        verify(tokenRepository, times(1)).markAsUsed(tokenPropietario.getId());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token no existe")
    void testResetearPasswordConToken_TokenNoExiste() {
        // Arrange
        String token = "noexiste-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> passwordResetService.resetearPasswordConToken(token, "newPassword123"));
        
        assertTrue(exception.getMessage().contains("no es válido") || exception.getMessage().contains("expirado"));
        verify(tokenRepository, times(1)).findByToken(token);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token está expirado")
    void testResetearPasswordConToken_TokenExpirado() {
        // Arrange
        String token = "expired-token-456";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenExpirado));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> passwordResetService.resetearPasswordConToken(token, "newPassword123"));
        
        assertTrue(exception.getMessage().contains("no es válido") || exception.getMessage().contains("expirado"));
        verify(tokenRepository, times(1)).findByToken(token);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token ya fue usado")
    void testResetearPasswordConToken_TokenUsado() {
        // Arrange
        String token = "used-token-789";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenUsado));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> passwordResetService.resetearPasswordConToken(token, "newPassword123"));
        
        assertTrue(exception.getMessage().contains("no es válido") || exception.getMessage().contains("expirado"));
        verify(tokenRepository, times(1)).findByToken(token);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar token correctamente")
    void testValidarToken_Valid() {
        // Arrange
        String token = "valid-token-123";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenValido));

        // Act
        boolean resultado = passwordResetService.validarToken(token);

        // Assert
        assertTrue(resultado);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe rechazar token inválido")
    void testValidarToken_Invalid() {
        // Arrange
        String token = "invalid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // Act
        boolean resultado = passwordResetService.validarToken(token);

        // Assert
        assertFalse(resultado);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe rechazar token expirado")
    void testValidarToken_Expirado() {
        // Arrange
        String token = "expired-token-456";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenExpirado));

        // Act
        boolean resultado = passwordResetService.validarToken(token);

        // Assert
        assertFalse(resultado);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe obtener información completa del token")
    void testObtenerInfoToken_Success() {
        // Arrange
        String token = "valid-token-123";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenValido));

        // Act
        Map<String, Object> info = passwordResetService.obtenerInfoToken(token);

        // Assert
        assertNotNull(info);
        assertTrue((Boolean) info.get("valid"));
        assertNotNull(info.get("expiresAt"));
        assertTrue(info.get("expiresInHours") instanceof Number);
        verify(tokenRepository, times(1)).findByToken(token);
    }

    @Test
    @DisplayName("Debe invalidar tokens anteriores al generar uno nuevo")
    void testSolicitarRecuperacion_InvalidarTokensAnteriores() {
        // Arrange
        String email = "usuario@test.com";
        PasswordResetToken tokenAnterior = PasswordResetToken.builder()
            .id(5L)
            .token("anterior-token")
            .email(email)
            .userType("USUARIO")
            .expiresAt(LocalDateTime.now().plusHours(12))
            .usado(false)
            .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(tokenRepository.findValidTokenByEmailAndUserType(eq(email), eq("USUARIO"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(tokenAnterior));
        doNothing().when(tokenRepository).markAsUsed(tokenAnterior.getId());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValido);
        when(emailService.enviarEmailRecuperacionPassword(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);

        // Act
        passwordResetService.solicitarRecuperacionUsuario(email);

        // Assert
        verify(tokenRepository, times(1)).markAsUsed(tokenAnterior.getId());
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe al resetear contraseña")
    void testResetearPasswordConToken_UsuarioNoExiste() {
        // Arrange
        String token = "valid-token-123";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenValido));
        when(usuarioRepository.findByEmail(tokenValido.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> passwordResetService.resetearPasswordConToken(token, "newPassword123"));
        
        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(usuarioRepository, times(1)).findByEmail(tokenValido.getEmail());
        verify(usuarioRepository, never()).save(any());
    }
}

