package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.LoginRequestDTO;
import com.clinica.veterinaria.dto.LoginResponseDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IAuditLogger auditLogger;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioAdmin;
    private Usuario usuarioVet;
    private UserDetails userDetails;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        usuarioAdmin = Usuario.builder()
            .id(1L)
            .nombre("Admin Test")
            .email("admin@test.com")
            .password("$2a$10$encodedPassword")
            .rol(Usuario.Rol.ADMIN)
            .activo(true)
            .build();

        usuarioVet = Usuario.builder()
            .id(2L)
            .nombre("Vet Test")
            .email("vet@test.com")
            .password("$2a$10$encodedPassword")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        userDetails = User.builder()
            .username("admin@test.com")
            .password("$2a$10$encodedPassword")
            .authorities("ROLE_ADMIN")
            .build();

        loginRequest = LoginRequestDTO.builder()
            .email("admin@test.com")
            .password("admin123")
            .build();
    }

    @Test
    @DisplayName("Debe realizar login exitoso y retornar token")
    void testLogin_Success() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), loginRequest.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(userDetailsService.loadUserByUsername(loginRequest.getEmail()))
            .thenReturn(userDetails);
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(usuarioAdmin));
        when(jwtUtil.generateToken(any(UserDetails.class), any(Map.class))).thenReturn("mock-jwt-token");
        doNothing().when(auditLogger).logLoginSuccess(anyString(), anyString());

        // Act
        LoginResponseDTO resultado = authService.login(loginRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals("mock-jwt-token", resultado.getToken());
        assertEquals("Bearer", resultado.getType());
        assertNotNull(resultado.getUsuario());
        assertEquals("admin@test.com", resultado.getUsuario().getEmail());
        assertEquals(Usuario.Rol.ADMIN, resultado.getUsuario().getRol());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class), any(Map.class));
        verify(auditLogger, times(1)).logLoginSuccess(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción con credenciales incorrectas")
    void testLogin_BadCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Credenciales inválidas"));
        doNothing().when(auditLogger).logLoginFailure(anyString(), anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(auditLogger, times(1)).logLoginFailure(anyString(), anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void testLogin_UserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), loginRequest.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(userDetailsService.loadUserByUsername(loginRequest.getEmail()))
            .thenReturn(userDetails);
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.empty());
        doNothing().when(auditLogger).logLoginFailure(anyString(), anyString(), anyString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(usuarioRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario está inactivo")
    void testLogin_UsuarioInactivo() {
        // Arrange
        Usuario usuarioInactivo = Usuario.builder()
            .id(1L)
            .nombre("Usuario Inactivo")
            .email("inactivo@test.com")
            .password("$2a$10$encodedPassword")
            .rol(Usuario.Rol.VET)
            .activo(false)
            .build();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            "inactivo@test.com", "password123");

        LoginRequestDTO loginRequestInactivo = LoginRequestDTO.builder()
            .email("inactivo@test.com")
            .password("password123")
            .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(userDetailsService.loadUserByUsername("inactivo@test.com"))
            .thenReturn(userDetails);
        when(usuarioRepository.findByEmail("inactivo@test.com"))
            .thenReturn(Optional.of(usuarioInactivo));
        doNothing().when(auditLogger).logLoginFailure(anyString(), anyString(), anyString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequestInactivo));
        assertTrue(exception.getMessage().contains("inactivo") || exception.getMessage().contains("deshabilitado"));
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Debe validar token correctamente")
    void testValidateToken_Valid() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.extractUsername(token)).thenReturn("admin@test.com");
        when(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        // Act
        boolean resultado = authService.validateToken(token);

        // Assert
        assertTrue(resultado);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(userDetailsService, times(1)).loadUserByUsername("admin@test.com");
        verify(jwtUtil, times(1)).validateToken(token, userDetails);
    }

    @Test
    @DisplayName("Debe rechazar token inválido")
    void testValidateToken_Invalid() {
        // Arrange
        String token = "invalid-token";
        when(jwtUtil.extractUsername(token)).thenReturn("admin@test.com");
        when(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        // Act
        boolean resultado = authService.validateToken(token);

        // Assert
        assertFalse(resultado);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(jwtUtil, times(1)).validateToken(token, userDetails);
    }

    @Test
    @DisplayName("Debe obtener usuario desde token válido")
    void testGetUserFromToken_Success() {
        // Arrange
        String token = "valid-token";
        String email = "admin@test.com";

        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioAdmin));

        // Act
        UsuarioDTO resultado = authService.getUserFromToken(token);

        // Assert
        assertNotNull(resultado);
        assertEquals("admin@test.com", resultado.getEmail());
        assertEquals("Admin Test", resultado.getNombre());
        assertEquals(Usuario.Rol.ADMIN, resultado.getRol());
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe en token")
    void testGetUserFromToken_UserNotFound() {
        // Arrange
        String token = "valid-token";
        String email = "noexiste@test.com";

        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.getUserFromToken(token));
        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Debe realizar login con rol VET correctamente")
    void testLogin_VetRole() {
        // Arrange
        LoginRequestDTO vetLoginRequest = LoginRequestDTO.builder()
            .email("vet@test.com")
            .password("vet123")
            .build();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            vetLoginRequest.getEmail(), vetLoginRequest.getPassword());

        UserDetails vetUserDetails = User.builder()
            .username("vet@test.com")
            .password("$2a$10$encodedPassword")
            .authorities("ROLE_VET")
            .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(userDetailsService.loadUserByUsername(vetLoginRequest.getEmail()))
            .thenReturn(vetUserDetails);
        when(usuarioRepository.findByEmail(vetLoginRequest.getEmail()))
            .thenReturn(Optional.of(usuarioVet));
        when(jwtUtil.generateToken(any(UserDetails.class), any(Map.class))).thenReturn("vet-jwt-token");
        doNothing().when(auditLogger).logLoginSuccess(anyString(), anyString());

        // Act
        LoginResponseDTO resultado = authService.login(vetLoginRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals("vet-jwt-token", resultado.getToken());
        assertEquals(Usuario.Rol.VET, resultado.getUsuario().getRol());
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class), any(Map.class));
    }

    @Test
    @DisplayName("Debe registrar intento de login fallido en auditoría")
    void testLogin_AuditLogFailure() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Credenciales inválidas"));
        doNothing().when(auditLogger).logLoginFailure(anyString(), anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        
        // Verificar que se registró el intento fallido
        verify(auditLogger, times(1)).logLoginFailure(
            eq(loginRequest.getEmail()), 
            anyString(), 
            eq("Credenciales inválidas"));
    }

    @Test
    @DisplayName("Debe registrar login exitoso en auditoría")
    void testLogin_AuditLogSuccess() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), loginRequest.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(userDetailsService.loadUserByUsername(loginRequest.getEmail()))
            .thenReturn(userDetails);
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(usuarioAdmin));
        when(jwtUtil.generateToken(any(UserDetails.class), any(Map.class))).thenReturn("mock-jwt-token");
        doNothing().when(auditLogger).logLoginSuccess(anyString(), anyString());

        // Act
        authService.login(loginRequest);

        // Assert
        verify(auditLogger, times(1)).logLoginSuccess(
            eq(loginRequest.getEmail()),
            anyString());
    }
}

