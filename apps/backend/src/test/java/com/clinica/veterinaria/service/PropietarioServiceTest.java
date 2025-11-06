package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PropietarioService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de PropietarioService")
class PropietarioServiceTest {

    @Mock
    private PropietarioRepository propietarioRepository;

    @InjectMocks
    private PropietarioService propietarioService;

    private Propietario propietario1;
    private Propietario propietario2;

    @BeforeEach
    void setUp() {
        propietario1 = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .documento("12345678")
            .email("juan@email.com")
            .telefono("555-1234")
            .direccion("Calle 123")
            .activo(true)
            .build();

        propietario2 = Propietario.builder()
            .id(2L)
            .nombre("María García")
            .documento("87654321")
            .email("maria@email.com")
            .telefono("555-5678")
            .direccion("Avenida 456")
            .activo(true)
            .build();
    }

    @Test
    @DisplayName("Debe listar todos los propietarios")
    void testFindAll() {
        // Arrange
        when(propietarioRepository.findAll()).thenReturn(Arrays.asList(propietario1, propietario2));

        // Act
        List<PropietarioDTO> resultado = propietarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
        assertEquals("María García", resultado.get(1).getNombre());
        verify(propietarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener propietario por ID")
    void testFindById() {
        // Arrange
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario1));

        // Act
        PropietarioDTO resultado = propietarioService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        assertEquals("juan@email.com", resultado.getEmail());
        assertEquals("12345678", resultado.getDocumento());
        verify(propietarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando propietario no existe")
    void testFindById_NoExiste() {
        // Arrange
        when(propietarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> propietarioService.findById(999L));
    }

    @Test
    @DisplayName("Debe crear un nuevo propietario")
    void testCreate() {
        // Arrange
        PropietarioDTO propietarioDTO = PropietarioDTO.builder()
            .nombre("Carlos López")
            .documento("11223344")
            .email("carlos@email.com")
            .telefono("555-9999")
            .direccion("Boulevard 789")
            .build();

        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> {
            Propietario prop = invocation.getArgument(0);
            prop.setId(3L);
            return prop;
        });

        // Act
        PropietarioDTO resultado = propietarioService.create(propietarioDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Carlos López", resultado.getNombre());
        assertEquals("carlos@email.com", resultado.getEmail());
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("Debe actualizar un propietario existente")
    void testUpdate() {
        // Arrange
        PropietarioDTO actualizacionDTO = PropietarioDTO.builder()
            .nombre("Juan Pérez Actualizado")
            .documento("12345678")
            .email("juan.nuevo@email.com")
            .telefono("555-0000")
            .direccion("Nueva Calle 999")
            .activo(true)
            .build();

        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario1));
        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PropietarioDTO resultado = propietarioService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Pérez Actualizado", resultado.getNombre());
        assertEquals("juan.nuevo@email.com", resultado.getEmail());
        verify(propietarioRepository, times(1)).findById(1L);
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("Debe eliminar (soft delete) un propietario")
    void testDelete() {
        // Arrange
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario1));

        // Act
        propietarioService.delete(1L);

        // Assert
        assertFalse(propietario1.getActivo());
        verify(propietarioRepository, times(1)).findById(1L);
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("Debe buscar propietarios por nombre")
    void testFindByNombre() {
        // Arrange
        when(propietarioRepository.findByNombreContainingIgnoreCase("Juan"))
            .thenReturn(Arrays.asList(propietario1));

        // Act
        List<PropietarioDTO> resultado = propietarioService.findByNombre("Juan");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
        verify(propietarioRepository, times(1)).findByNombreContainingIgnoreCase("Juan");
    }
}

