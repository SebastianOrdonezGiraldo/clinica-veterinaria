package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.TemplatePrescripcionDTO;
import com.clinica.veterinaria.dto.TemplatePrescripcionItemDTO;
import com.clinica.veterinaria.entity.TemplatePrescripcion;
import com.clinica.veterinaria.entity.TemplatePrescripcionItem;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.TemplatePrescripcionRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de TemplatePrescripcionService")
class TemplatePrescripcionServiceTest {

    @Mock
    private TemplatePrescripcionRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TemplatePrescripcionService service;

    private Usuario usuario;
    private TemplatePrescripcion template;
    private TemplatePrescripcionItem item;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Test")
            .email("test@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        template = TemplatePrescripcion.builder()
            .id(1L)
            .nombre("Template Antibióticos")
            .descripcion("Template para tratamientos antibióticos")
            .categoria("Antibióticos")
            .indicacionesGenerales("Tomar con alimentos")
            .activo(true)
            .creadoPor(usuario)
            .vecesUsado(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        item = TemplatePrescripcionItem.builder()
            .id(1L)
            .medicamento("Amoxicilina")
            .presentacion("Tabletas 250mg")
            .dosis("10 mg/kg")
            .frecuencia("Cada 8 horas")
            .duracion("7 días")
            .indicaciones("Tomar con alimentos")
            .orden(0)
            .template(template)
            .createdAt(LocalDateTime.now())
            .build();

        template.getItems().add(item);
    }

    @Test
    @DisplayName("Debe obtener todos los templates activos")
    void testFindAll() {
        // Arrange
        when(repository.findByActivoTrueOrderByCategoriaAscNombreAsc())
            .thenReturn(Arrays.asList(template));

        // Act
        List<TemplatePrescripcionDTO> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Template Antibióticos", result.get(0).getNombre());
        assertEquals(1, result.get(0).getItems().size());
        verify(repository, times(1)).findByActivoTrueOrderByCategoriaAscNombreAsc();
    }

    @Test
    @DisplayName("Debe obtener categorías únicas")
    void testGetCategorias() {
        // Arrange
        when(repository.findDistinctCategorias())
            .thenReturn(Arrays.asList("Antibióticos", "Analgésicos"));

        // Act
        List<String> result = service.getCategorias();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Antibióticos"));
        assertTrue(result.contains("Analgésicos"));
        verify(repository, times(1)).findDistinctCategorias();
    }

    @Test
    @DisplayName("Debe obtener templates por categoría")
    void testFindByCategoria() {
        // Arrange
        when(repository.findByCategoriaAndActivoTrueOrderByNombreAsc("Antibióticos"))
            .thenReturn(Arrays.asList(template));

        // Act
        List<TemplatePrescripcionDTO> result = service.findByCategoria("Antibióticos");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Antibióticos", result.get(0).getCategoria());
        verify(repository, times(1)).findByCategoriaAndActivoTrueOrderByNombreAsc("Antibióticos");
    }

    @Test
    @DisplayName("Debe crear un nuevo template con items")
    void testCreate() {
        // Arrange
        TemplatePrescripcionItemDTO itemDTO = TemplatePrescripcionItemDTO.builder()
            .medicamento("Amoxicilina")
            .presentacion("Tabletas 250mg")
            .dosis("10 mg/kg")
            .frecuencia("Cada 8 horas")
            .duracion("7 días")
            .indicaciones("Tomar con alimentos")
            .orden(0)
            .build();

        TemplatePrescripcionDTO dto = TemplatePrescripcionDTO.builder()
            .nombre("Nuevo Template")
            .descripcion("Descripción")
            .categoria("Analgésicos")
            .indicacionesGenerales("Indicaciones generales")
            .items(Arrays.asList(itemDTO))
            .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(TemplatePrescripcion.class))).thenAnswer(invocation -> {
            TemplatePrescripcion saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        TemplatePrescripcionDTO result = service.create(dto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Nuevo Template", result.getNombre());
        assertEquals(1, result.getItems().size());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(TemplatePrescripcion.class));
    }

    @Test
    @DisplayName("Debe actualizar un template y sus items")
    void testUpdate() {
        // Arrange
        TemplatePrescripcionItemDTO itemDTO = TemplatePrescripcionItemDTO.builder()
            .medicamento("Nuevo Medicamento")
            .dosis("5 mg/kg")
            .frecuencia("Cada 12 horas")
            .duracion("5 días")
            .orden(0)
            .build();

        TemplatePrescripcionDTO dto = TemplatePrescripcionDTO.builder()
            .nombre("Template Actualizado")
            .descripcion("Nueva descripción")
            .categoria("Post-operatorio")
            .indicacionesGenerales("Nuevas indicaciones")
            .items(Arrays.asList(itemDTO))
            .activo(true)
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any(TemplatePrescripcion.class))).thenReturn(template);

        // Act
        TemplatePrescripcionDTO result = service.update(1L, dto);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(TemplatePrescripcion.class));
    }

    @Test
    @DisplayName("Debe incrementar el contador de uso")
    void testIncrementarUso() {
        // Arrange
        template.setVecesUsado(3);
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any(TemplatePrescripcion.class))).thenReturn(template);

        // Act
        service.incrementarUso(1L);

        // Assert
        assertEquals(4, template.getVecesUsado());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(template);
    }
}

