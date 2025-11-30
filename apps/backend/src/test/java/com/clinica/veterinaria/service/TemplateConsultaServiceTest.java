package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.TemplateConsultaDTO;
import com.clinica.veterinaria.entity.TemplateConsulta;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.TemplateConsultaRepository;
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
@DisplayName("Tests Unitarios de TemplateConsultaService")
class TemplateConsultaServiceTest {

    @Mock
    private TemplateConsultaRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TemplateConsultaService service;

    private Usuario usuario;
    private TemplateConsulta template;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Test")
            .email("test@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        template = TemplateConsulta.builder()
            .id(1L)
            .nombre("Template General")
            .descripcion("Template para consultas generales")
            .categoria("General")
            .examenFisico("Examen físico normal")
            .diagnostico("Diagnóstico general")
            .tratamiento("Tratamiento estándar")
            .observaciones("Observaciones generales")
            .activo(true)
            .creadoPor(usuario)
            .vecesUsado(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Debe obtener todos los templates activos")
    void testFindAll() {
        // Arrange
        when(repository.findByActivoTrueOrderByCategoriaAscNombreAsc())
            .thenReturn(Arrays.asList(template));

        // Act
        List<TemplateConsultaDTO> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Template General", result.get(0).getNombre());
        verify(repository, times(1)).findByActivoTrueOrderByCategoriaAscNombreAsc();
    }

    @Test
    @DisplayName("Debe obtener templates por categoría")
    void testFindByCategoria() {
        // Arrange
        when(repository.findByCategoriaAndActivoTrueOrderByNombreAsc("General"))
            .thenReturn(Arrays.asList(template));

        // Act
        List<TemplateConsultaDTO> result = service.findByCategoria("General");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("General", result.get(0).getCategoria());
        verify(repository, times(1)).findByCategoriaAndActivoTrueOrderByNombreAsc("General");
    }

    @Test
    @DisplayName("Debe buscar templates por nombre")
    void testSearch() {
        // Arrange
        when(repository.findByNombreContainingIgnoreCaseAndActivoTrue("Template"))
            .thenReturn(Arrays.asList(template));

        // Act
        List<TemplateConsultaDTO> result = service.search("Template");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByNombreContainingIgnoreCaseAndActivoTrue("Template");
    }

    @Test
    @DisplayName("Debe obtener template por ID")
    void testFindById() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(template));

        // Act
        TemplateConsultaDTO result = service.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Template General", result.getNombre());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando template no existe")
    void testFindById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.findById(999L));
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe crear un nuevo template")
    void testCreate() {
        // Arrange
        TemplateConsultaDTO dto = TemplateConsultaDTO.builder()
            .nombre("Nuevo Template")
            .descripcion("Descripción")
            .categoria("Emergencia")
            .examenFisico("Examen")
            .diagnostico("Diagnóstico")
            .tratamiento("Tratamiento")
            .observaciones("Observaciones")
            .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(TemplateConsulta.class))).thenAnswer(invocation -> {
            TemplateConsulta saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        TemplateConsultaDTO result = service.create(dto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Nuevo Template", result.getNombre());
        assertEquals("Emergencia", result.getCategoria());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(TemplateConsulta.class));
    }

    @Test
    @DisplayName("Debe actualizar un template existente")
    void testUpdate() {
        // Arrange
        TemplateConsultaDTO dto = TemplateConsultaDTO.builder()
            .nombre("Template Actualizado")
            .descripcion("Nueva descripción")
            .categoria("Cirugía")
            .examenFisico("Nuevo examen")
            .diagnostico("Nuevo diagnóstico")
            .tratamiento("Nuevo tratamiento")
            .observaciones("Nuevas observaciones")
            .activo(true)
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any(TemplateConsulta.class))).thenReturn(template);

        // Act
        TemplateConsultaDTO result = service.update(1L, dto);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(TemplateConsulta.class));
    }

    @Test
    @DisplayName("Debe desactivar un template (soft delete)")
    void testDelete() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any(TemplateConsulta.class))).thenReturn(template);

        // Act
        service.delete(1L);

        // Assert
        assertFalse(template.getActivo());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(template);
    }

    @Test
    @DisplayName("Debe incrementar el contador de uso")
    void testIncrementarUso() {
        // Arrange
        template.setVecesUsado(5);
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any(TemplateConsulta.class))).thenReturn(template);

        // Act
        service.incrementarUso(1L);

        // Assert
        assertEquals(6, template.getVecesUsado());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(template);
    }
}

