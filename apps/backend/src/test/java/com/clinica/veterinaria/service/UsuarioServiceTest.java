package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioAdmin;
    private Usuario usuarioVet;

    @BeforeEach
    void setUp() {
        usuarioAdmin = Usuario.builder()
            .id(1L)
            .nombre("Admin Test")
            .email("admin@test.com")
            .password("encodedPassword")
            .rol(Usuario.Rol.ADMIN)
            .activo(true)
            .build();

        usuarioVet = Usuario.builder()
            .id(2L)
            .nombre("Dr. Test")
            .email("vet@test.com")
            .password("encodedPassword")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();
    }

    @Test
    @DisplayName("Debe listar todos los usuarios")
    void testFindAll() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioAdmin, usuarioVet));

        // Act
        List<UsuarioDTO> resultado = usuarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Admin Test", resultado.get(0).getNombre());
        assertEquals("Dr. Test", resultado.get(1).getNombre());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener un usuario por ID")
    void testFindById() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));

        // Act
        UsuarioDTO resultado = usuarioService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Admin Test", resultado.getNombre());
        assertEquals("admin@test.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void testFindById_NoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioService.findById(999L));
        verify(usuarioRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe crear un nuevo usuario")
    void testCreate() {
        // Arrange
        UsuarioCreateDTO createDTO = UsuarioCreateDTO.builder()
            .nombre("Nuevo Usuario")
            .email("nuevo@test.com")
            .password("password123")
            .rol(Usuario.Rol.VET)
            .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(3L);
            return usuario;
        });

        // Act
        UsuarioDTO resultado = usuarioService.create(createDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nuevo Usuario", resultado.getNombre());
        assertEquals("nuevo@test.com", resultado.getEmail());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe actualizar un usuario existente")
    void testUpdate() {
        // Arrange
        UsuarioCreateDTO updateDTO = UsuarioCreateDTO.builder()
            .nombre("Admin Actualizado")
            .email("admin@test.com")
            .rol(Usuario.Rol.ADMIN)
            .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioDTO resultado = usuarioService.update(1L, updateDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Admin Actualizado", resultado.getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe eliminar un usuario por ID")
    void testDelete() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));

        // Act
        usuarioService.delete(1L);

        // Assert
        assertFalse(usuarioAdmin.getActivo());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(usuarioAdmin);
    }

    @Test
    @DisplayName("Debe buscar usuario por email")
    void testFindByEmail() {
        // Arrange
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuarioAdmin));

        // Act
        UsuarioDTO resultado = usuarioService.findByEmail("admin@test.com");

        // Assert
        assertNotNull(resultado);
        assertEquals("admin@test.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail("admin@test.com");
    }

    @Test
    @DisplayName("Debe validar que el email no exista al crear")
    void testCreate_EmailDuplicado() {
        // Arrange
        UsuarioCreateDTO createDTO = UsuarioCreateDTO.builder()
            .nombre("Duplicado")
            .email("admin@test.com")
            .password("password123")
            .rol(Usuario.Rol.VET)
            .build();

        when(usuarioRepository.existsByEmail("admin@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioService.create(createDTO));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}

