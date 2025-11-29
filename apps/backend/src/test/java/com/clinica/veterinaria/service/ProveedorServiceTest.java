package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ProveedorDTO;
import com.clinica.veterinaria.entity.Proveedor;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.ProveedorRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProveedorService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de ProveedorService")
@SuppressWarnings({"null"})
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private IAuditLogger auditLogger;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor proveedor;
    private ProveedorDTO proveedorDTO;

    @BeforeEach
    void setUp() {
        proveedor = Proveedor.builder()
            .id(1L)
            .nombre("Proveedor Test S.A.")
            .ruc("12345678901")
            .email("proveedor@test.com")
            .telefono("1234567890")
            .direccion("Calle Test 123")
            .activo(true)
            .build();

        proveedorDTO = ProveedorDTO.builder()
            .id(1L)
            .nombre("Proveedor Test S.A.")
            .ruc("12345678901")
            .email("proveedor@test.com")
            .telefono("1234567890")
            .direccion("Calle Test 123")
            .activo(true)
            .build();
    }

    @Test
    @DisplayName("Debería obtener todos los proveedores activos")
    void testFindAllActivos() {
        // Given
        List<Proveedor> proveedores = Arrays.asList(proveedor);
        when(proveedorRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(proveedores);

        // When
        List<ProveedorDTO> result = proveedorService.findAllActivos();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Proveedor Test S.A.", result.get(0).getNombre());
        verify(proveedorRepository).findByActivoTrueOrderByNombreAsc();
    }

    @Test
    @DisplayName("Debería obtener todos los proveedores")
    void testFindAll() {
        // Given
        List<Proveedor> proveedores = Arrays.asList(proveedor);
        when(proveedorRepository.findAll()).thenReturn(proveedores);

        // When
        List<ProveedorDTO> result = proveedorService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(proveedorRepository).findAll();
    }

    @Test
    @DisplayName("Debería encontrar un proveedor por ID")
    void testFindById_Success() {
        // Given
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        // When
        ProveedorDTO result = proveedorService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Proveedor Test S.A.", result.getNombre());
        verify(proveedorRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no encuentra proveedor por ID")
    void testFindById_NotFound() {
        // Given
        when(proveedorRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> proveedorService.findById(999L));
        verify(proveedorRepository).findById(999L);
    }

    @Test
    @DisplayName("Debería buscar proveedores por nombre")
    void testBuscarPorNombre_Success() {
        // Given
        List<Proveedor> proveedores = Arrays.asList(proveedor);
        when(proveedorRepository.findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc("Test", true))
            .thenReturn(proveedores);

        // When
        List<ProveedorDTO> result = proveedorService.buscarPorNombre("Test", true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(proveedorRepository).findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc("Test", true);
    }

    @Test
    @DisplayName("Debería crear un nuevo proveedor exitosamente")
    void testCreate_Success() {
        // Given
        ProveedorDTO nuevoDTO = ProveedorDTO.builder()
            .nombre("Nuevo Proveedor")
            .email("nuevo@test.com")
            .ruc("98765432109")
            .build();

        when(proveedorRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(proveedorRepository.existsByRuc("98765432109")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor prov = invocation.getArgument(0);
            prov.setId(2L);
            return prov;
        });

        // When
        ProveedorDTO result = proveedorService.create(nuevoDTO);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Nuevo Proveedor", result.getNombre());
        verify(proveedorRepository).existsByEmail("nuevo@test.com");
        verify(proveedorRepository).existsByRuc("98765432109");
        verify(proveedorRepository).save(any(Proveedor.class));
        verify(auditLogger).logCreate(eq("Proveedor"), eq(2L), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción al crear proveedor con email duplicado")
    void testCreate_DuplicateEmail() {
        // Given
        ProveedorDTO nuevoDTO = ProveedorDTO.builder()
            .nombre("Nuevo Proveedor")
            .email("proveedor@test.com")
            .build();

        when(proveedorRepository.existsByEmail("proveedor@test.com")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> proveedorService.create(nuevoDTO));
        verify(proveedorRepository).existsByEmail("proveedor@test.com");
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción al crear proveedor con RUC duplicado")
    void testCreate_DuplicateRuc() {
        // Given
        ProveedorDTO nuevoDTO = ProveedorDTO.builder()
            .nombre("Nuevo Proveedor")
            .ruc("12345678901")
            .build();

        when(proveedorRepository.existsByRuc("12345678901")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> proveedorService.create(nuevoDTO));
        verify(proveedorRepository).existsByRuc("12345678901");
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar un proveedor exitosamente")
    void testUpdate_Success() {
        // Given
        ProveedorDTO updateDTO = ProveedorDTO.builder()
            .nombre("Proveedor Actualizado")
            .email("actualizado@test.com")
            .telefono("9876543210")
            .build();

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByEmail("actualizado@test.com")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        // When
        ProveedorDTO result = proveedorService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(proveedorRepository).findById(1L);
        verify(proveedorRepository).existsByEmail("actualizado@test.com");
        verify(proveedorRepository).save(any(Proveedor.class));
        verify(auditLogger).logUpdate(eq("Proveedor"), eq(1L), anyString(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar proveedor inexistente")
    void testUpdate_NotFound() {
        // Given
        ProveedorDTO updateDTO = ProveedorDTO.builder()
            .nombre("Actualizado")
            .build();

        when(proveedorRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> proveedorService.update(999L, updateDTO));
        verify(proveedorRepository).findById(999L);
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con email duplicado")
    void testUpdate_DuplicateEmail() {
        // Given
        ProveedorDTO updateDTO = ProveedorDTO.builder()
            .email("otro@test.com")
            .build();

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByEmail("otro@test.com")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> proveedorService.update(1L, updateDTO));
        verify(proveedorRepository).findById(1L);
        verify(proveedorRepository).existsByEmail("otro@test.com");
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería desactivar un proveedor exitosamente")
    void testDelete_Success() {
        // Given
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        // When
        proveedorService.delete(1L);

        // Then
        verify(proveedorRepository).findById(1L);
        verify(proveedorRepository).save(any(Proveedor.class));
        verify(auditLogger).logDelete("Proveedor", 1L);
        assertFalse(proveedor.getActivo());
    }

    @Test
    @DisplayName("Debería lanzar excepción al desactivar proveedor inexistente")
    void testDelete_NotFound() {
        // Given
        when(proveedorRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> proveedorService.delete(999L));
        verify(proveedorRepository).findById(999L);
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería permitir crear proveedor sin email ni RUC")
    void testCreate_WithoutEmailAndRuc() {
        // Given
        ProveedorDTO nuevoDTO = ProveedorDTO.builder()
            .nombre("Proveedor Sin Email")
            .build();

        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor prov = invocation.getArgument(0);
            prov.setId(2L);
            return prov;
        });

        // When
        ProveedorDTO result = proveedorService.create(nuevoDTO);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(proveedorRepository).save(any(Proveedor.class));
    }
}

