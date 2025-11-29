package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.CategoriaProductoDTO;
import com.clinica.veterinaria.entity.CategoriaProducto;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.CategoriaProductoRepository;
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
 * Tests unitarios para CategoriaProductoService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de CategoriaProductoService")
@SuppressWarnings({"null"})
class CategoriaProductoServiceTest {

    @Mock
    private CategoriaProductoRepository categoriaRepository;

    @Mock
    private IAuditLogger auditLogger;

    @InjectMocks
    private CategoriaProductoService categoriaService;

    private CategoriaProducto categoria;
    private CategoriaProductoDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        categoria = CategoriaProducto.builder()
            .id(1L)
            .nombre("Medicamentos")
            .descripcion("Categoría para medicamentos veterinarios")
            .activo(true)
            .build();

        categoriaDTO = CategoriaProductoDTO.builder()
            .id(1L)
            .nombre("Medicamentos")
            .descripcion("Categoría para medicamentos veterinarios")
            .activo(true)
            .build();
    }

    @Test
    @DisplayName("Debería obtener todas las categorías activas")
    void testFindAllActivas() {
        // Given
        List<CategoriaProducto> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(categorias);

        // When
        List<CategoriaProductoDTO> result = categoriaService.findAllActivas();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Medicamentos", result.get(0).getNombre());
        verify(categoriaRepository).findByActivoTrueOrderByNombreAsc();
    }

    @Test
    @DisplayName("Debería obtener todas las categorías")
    void testFindAll() {
        // Given
        List<CategoriaProducto> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        // When
        List<CategoriaProductoDTO> result = categoriaService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoriaRepository).findAll();
    }

    @Test
    @DisplayName("Debería encontrar una categoría por ID")
    void testFindById_Success() {
        // Given
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // When
        CategoriaProductoDTO result = categoriaService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Medicamentos", result.getNombre());
        verify(categoriaRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no encuentra categoría por ID")
    void testFindById_NotFound() {
        // Given
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> categoriaService.findById(999L));
        verify(categoriaRepository).findById(999L);
    }

    @Test
    @DisplayName("Debería encontrar una categoría por nombre")
    void testFindByNombre_Success() {
        // Given
        when(categoriaRepository.findByNombreIgnoreCase("Medicamentos"))
            .thenReturn(Optional.of(categoria));

        // When
        CategoriaProductoDTO result = categoriaService.findByNombre("Medicamentos");

        // Then
        assertNotNull(result);
        assertEquals("Medicamentos", result.getNombre());
        verify(categoriaRepository).findByNombreIgnoreCase("Medicamentos");
    }

    @Test
    @DisplayName("Debería retornar null cuando no encuentra categoría por nombre")
    void testFindByNombre_NotFound() {
        // Given
        when(categoriaRepository.findByNombreIgnoreCase("Inexistente"))
            .thenReturn(Optional.empty());

        // When
        CategoriaProductoDTO result = categoriaService.findByNombre("Inexistente");

        // Then
        assertNull(result);
        verify(categoriaRepository).findByNombreIgnoreCase("Inexistente");
    }

    @Test
    @DisplayName("Debería crear una nueva categoría exitosamente")
    void testCreate_Success() {
        // Given
        CategoriaProductoDTO nuevoDTO = CategoriaProductoDTO.builder()
            .nombre("Insumos")
            .descripcion("Categoría para insumos médicos")
            .build();

        when(categoriaRepository.existsByNombreIgnoreCase("Insumos")).thenReturn(false);
        when(categoriaRepository.save(any(CategoriaProducto.class))).thenAnswer(invocation -> {
            CategoriaProducto cat = invocation.getArgument(0);
            cat.setId(2L);
            return cat;
        });

        // When
        CategoriaProductoDTO result = categoriaService.create(nuevoDTO);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Insumos", result.getNombre());
        verify(categoriaRepository).existsByNombreIgnoreCase("Insumos");
        verify(categoriaRepository).save(any(CategoriaProducto.class));
        verify(auditLogger).logCreate(eq("CategoriaProducto"), eq(2L), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción al crear categoría duplicada")
    void testCreate_Duplicate() {
        // Given
        CategoriaProductoDTO nuevoDTO = CategoriaProductoDTO.builder()
            .nombre("Medicamentos")
            .build();

        when(categoriaRepository.existsByNombreIgnoreCase("Medicamentos")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> categoriaService.create(nuevoDTO));
        verify(categoriaRepository).existsByNombreIgnoreCase("Medicamentos");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar una categoría exitosamente")
    void testUpdate_Success() {
        // Given
        CategoriaProductoDTO updateDTO = CategoriaProductoDTO.builder()
            .nombre("Medicamentos Actualizados")
            .descripcion("Nueva descripción")
            .build();

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Medicamentos Actualizados"))
            .thenReturn(false);
        when(categoriaRepository.save(any(CategoriaProducto.class))).thenReturn(categoria);

        // When
        CategoriaProductoDTO result = categoriaService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).existsByNombreIgnoreCase("Medicamentos Actualizados");
        verify(categoriaRepository).save(any(CategoriaProducto.class));
        verify(auditLogger).logUpdate(eq("CategoriaProducto"), eq(1L), anyString(), anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar categoría inexistente")
    void testUpdate_NotFound() {
        // Given
        CategoriaProductoDTO updateDTO = CategoriaProductoDTO.builder()
            .nombre("Nueva")
            .build();

        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> categoriaService.update(999L, updateDTO));
        verify(categoriaRepository).findById(999L);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar con nombre duplicado")
    void testUpdate_DuplicateName() {
        // Given
        CategoriaProductoDTO updateDTO = CategoriaProductoDTO.builder()
            .nombre("Insumos")
            .build();

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Insumos")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> categoriaService.update(1L, updateDTO));
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).existsByNombreIgnoreCase("Insumos");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería desactivar una categoría exitosamente")
    void testDelete_Success() {
        // Given
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.countProductosActivosByCategoriaId(1L)).thenReturn(0L);
        when(categoriaRepository.save(any(CategoriaProducto.class))).thenReturn(categoria);

        // When
        categoriaService.delete(1L);

        // Then
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).countProductosActivosByCategoriaId(1L);
        verify(categoriaRepository).save(any(CategoriaProducto.class));
        verify(auditLogger).logDelete("CategoriaProducto", 1L);
        assertFalse(categoria.getActivo());
    }

    @Test
    @DisplayName("Debería lanzar excepción al desactivar categoría inexistente")
    void testDelete_NotFound() {
        // Given
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> categoriaService.delete(999L));
        verify(categoriaRepository).findById(999L);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción al desactivar categoría con productos activos")
    void testDelete_WithActiveProducts() {
        // Given
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.countProductosActivosByCategoriaId(1L)).thenReturn(5L);

        // When & Then
        assertThrows(BusinessException.class, () -> categoriaService.delete(1L));
        verify(categoriaRepository).findById(1L);
        verify(categoriaRepository).countProductosActivosByCategoriaId(1L);
        verify(categoriaRepository, never()).save(any());
    }
}

