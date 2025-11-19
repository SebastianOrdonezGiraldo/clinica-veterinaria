package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ItemPrescripcionDTO;
import com.clinica.veterinaria.dto.PrescripcionDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.ItemPrescripcion;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Prescripcion;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PrescripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PrescripcionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de PrescripcionService")
class PrescripcionServiceTest {

    @Mock
    private PrescripcionRepository prescripcionRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private PrescripcionService prescripcionService;

    private Consulta consulta;
    private Prescripcion prescripcion1;
    private Prescripcion prescripcion2;
    private ItemPrescripcion item1;
    private ItemPrescripcion item2;
    private LocalDateTime fechaEmision;

    @BeforeEach
    void setUp() {
        fechaEmision = LocalDateTime.now();

        Propietario propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .activo(true)
            .build();

        Paciente paciente = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .propietario(propietario)
            .activo(true)
            .build();

        Usuario profesional = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        consulta = Consulta.builder()
            .id(1L)
            .fecha(fechaEmision)
            .diagnostico("Gastritis")
            .paciente(paciente)
            .profesional(profesional)
            .build();

        item1 = ItemPrescripcion.builder()
            .id(1L)
            .medicamento("Omeprazol")
            .presentacion("Cápsulas 20mg")
            .dosis("1 cápsula")
            .frecuencia("Cada 12 horas")
            .duracionDias(7)
            .viaAdministracion(ItemPrescripcion.ViaAdministracion.ORAL)
            .indicaciones("Antes de las comidas")
            .build();

        item2 = ItemPrescripcion.builder()
            .id(2L)
            .medicamento("Metronidazol")
            .presentacion("Tabletas 250mg")
            .dosis("1 tableta")
            .frecuencia("Cada 8 horas")
            .duracionDias(5)
            .viaAdministracion(ItemPrescripcion.ViaAdministracion.ORAL)
            .indicaciones("Con comida")
            .build();

        prescripcion1 = Prescripcion.builder()
            .id(1L)
            .fechaEmision(fechaEmision)
            .indicacionesGenerales("Administrar con comida")
            .consulta(consulta)
            .items(new ArrayList<>(Arrays.asList(item1, item2)))
            .build();
        
        item1.setPrescripcion(prescripcion1);
        item2.setPrescripcion(prescripcion1);

        prescripcion2 = Prescripcion.builder()
            .id(2L)
            .fechaEmision(fechaEmision.minusDays(30))
            .indicacionesGenerales("Seguir tratamiento completo")
            .consulta(consulta)
            .items(List.of())
            .build();
    }

    @Test
    @DisplayName("Debe listar todas las prescripciones")
    void testFindAll() {
        // Arrange
        when(prescripcionRepository.findAll()).thenReturn(Arrays.asList(prescripcion1, prescripcion2));

        // Act
        List<PrescripcionDTO> resultado = prescripcionService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(prescripcionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener prescripción por ID")
    void testFindById() {
        // Arrange
        when(prescripcionRepository.findById(1L)).thenReturn(Optional.of(prescripcion1));

        // Act
        PrescripcionDTO resultado = prescripcionService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Administrar con comida", resultado.getIndicacionesGenerales());
        assertNotNull(resultado.getItems());
        assertEquals(2, resultado.getItems().size());
        verify(prescripcionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando prescripción no existe")
    void testFindByIdNoExiste() {
        // Arrange
        when(prescripcionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> prescripcionService.findById(999L));
        verify(prescripcionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe crear una nueva prescripción con items")
    void testCreate() {
        // Arrange
        ItemPrescripcionDTO itemDTO1 = ItemPrescripcionDTO.builder()
            .medicamento("Omeprazol")
            .presentacion("Cápsulas 20mg")
            .dosis("1 cápsula")
            .frecuencia("Cada 12 horas")
            .duracionDias(7)
            .viaAdministracion(ItemPrescripcion.ViaAdministracion.ORAL)
            .indicaciones("Antes de las comidas")
            .build();

        PrescripcionDTO prescripcionDTO = PrescripcionDTO.builder()
            .fechaEmision(fechaEmision)
            .indicacionesGenerales("Administrar con comida")
            .consultaId(1L)
            .items(Arrays.asList(itemDTO1))
            .build();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(prescripcionRepository.save(any(Prescripcion.class))).thenAnswer(invocation -> {
            Prescripcion p = invocation.getArgument(0);
            p.setId(3L);
            if (p.getItems() != null) {
                p.getItems().forEach(item -> item.setPrescripcion(p));
            }
            return p;
        });

        // Act
        PrescripcionDTO resultado = prescripcionService.create(prescripcionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Administrar con comida", resultado.getIndicacionesGenerales());
        assertNotNull(resultado.getItems());
        assertEquals(1, resultado.getItems().size());
        verify(consultaRepository, times(1)).findById(1L);
        verify(prescripcionRepository, times(2)).save(any(Prescripcion.class)); // Una vez sin items, otra con items
    }

    @Test
    @DisplayName("Debe asignar fecha automática si no se proporciona")
    void testCreateSinFecha() {
        // Arrange
        PrescripcionDTO prescripcionDTO = PrescripcionDTO.builder()
            .indicacionesGenerales("Prescripción sin fecha")
            .consultaId(1L)
            .items(List.of())
            .build();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(prescripcionRepository.save(any(Prescripcion.class))).thenAnswer(invocation -> {
            Prescripcion p = invocation.getArgument(0);
            p.setId(3L);
            return p;
        });

        // Act
        PrescripcionDTO resultado = prescripcionService.create(prescripcionDTO);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getFechaEmision()); // Debe tener fecha asignada
        verify(prescripcionRepository, times(1)).save(any(Prescripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear prescripción con consulta inexistente")
    void testCreateConsultaNoExiste() {
        // Arrange
        PrescripcionDTO prescripcionDTO = PrescripcionDTO.builder()
            .fechaEmision(fechaEmision)
            .consultaId(999L)
            .build();

        when(consultaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> prescripcionService.create(prescripcionDTO));
        verify(prescripcionRepository, never()).save(any(Prescripcion.class));
    }

    @Test
    @DisplayName("Debe actualizar una prescripción existente")
    void testUpdate() {
        // Arrange
        ItemPrescripcionDTO itemDTOActualizado = ItemPrescripcionDTO.builder()
            .medicamento("Nuevo Medicamento")
            .dosis("2 cápsulas")
            .frecuencia("Cada 6 horas")
            .duracionDias(10)
            .build();

        PrescripcionDTO actualizacionDTO = PrescripcionDTO.builder()
            .fechaEmision(fechaEmision.plusDays(1))
            .indicacionesGenerales("Indicaciones actualizadas")
            .consultaId(1L)
            .items(Arrays.asList(itemDTOActualizado))
            .build();

        when(prescripcionRepository.findById(1L)).thenReturn(Optional.of(prescripcion1));
        when(prescripcionRepository.save(any(Prescripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PrescripcionDTO resultado = prescripcionService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Indicaciones actualizadas", resultado.getIndicacionesGenerales());
        assertNotNull(resultado.getItems());
        assertEquals(1, resultado.getItems().size());
        assertEquals("Nuevo Medicamento", resultado.getItems().get(0).getMedicamento());
        verify(prescripcionRepository, times(1)).findById(1L);
        verify(prescripcionRepository, times(1)).save(any(Prescripcion.class));
    }

    @Test
    @DisplayName("Debe eliminar items al actualizar con lista vacía")
    void testUpdateConListaVacia() {
        // Arrange
        PrescripcionDTO actualizacionDTO = PrescripcionDTO.builder()
            .fechaEmision(fechaEmision)
            .indicacionesGenerales("Sin medicamentos")
            .consultaId(1L)
            .items(List.of()) // Lista vacía
            .build();

        when(prescripcionRepository.findById(1L)).thenReturn(Optional.of(prescripcion1));
        when(prescripcionRepository.save(any(Prescripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PrescripcionDTO resultado = prescripcionService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getItems() == null || resultado.getItems().isEmpty());
        verify(prescripcionRepository, times(1)).save(any(Prescripcion.class));
    }

    @Test
    @DisplayName("Debe eliminar una prescripción")
    void testDelete() {
        // Arrange
        when(prescripcionRepository.findById(1L)).thenReturn(Optional.of(prescripcion1));
        doNothing().when(prescripcionRepository).delete(any(Prescripcion.class));

        // Act
        prescripcionService.delete(1L);

        // Assert
        verify(prescripcionRepository, times(1)).findById(1L);
        verify(prescripcionRepository, times(1)).delete(any(Prescripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar prescripción inexistente")
    void testDeleteNoExiste() {
        // Arrange
        when(prescripcionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> prescripcionService.delete(999L));
        verify(prescripcionRepository, never()).delete(any(Prescripcion.class));
    }

    @Test
    @DisplayName("Debe buscar prescripciones por consulta")
    void testFindByConsulta() {
        // Arrange
        when(prescripcionRepository.findByConsultaId(1L))
            .thenReturn(Arrays.asList(prescripcion1, prescripcion2));

        // Act
        List<PrescripcionDTO> resultado = prescripcionService.findByConsulta(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(prescripcionRepository, times(1)).findByConsultaId(1L);
    }

    @Test
    @DisplayName("Debe buscar prescripciones por paciente")
    void testFindByPaciente() {
        // Arrange
        when(prescripcionRepository.findByPacienteId(1L))
            .thenReturn(Arrays.asList(prescripcion1, prescripcion2));

        // Act
        List<PrescripcionDTO> resultado = prescripcionService.findByPaciente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(prescripcionRepository, times(1)).findByPacienteId(1L);
    }

    @Test
    @DisplayName("Debe buscar prescripciones con filtros y paginación")
    void testSearchWithFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prescripcion> paginaPrescripciones = new PageImpl<>(Arrays.asList(prescripcion1, prescripcion2), pageable, 2);
        
        LocalDateTime inicio = fechaEmision.minusDays(1);
        LocalDateTime fin = fechaEmision.plusDays(1);

        when(prescripcionRepository.findByPacienteIdAndFechaEmisionBetween(any(), any(), any(), any()))
            .thenReturn(paginaPrescripciones);

        // Act
        Page<PrescripcionDTO> resultado = prescripcionService.searchWithFilters(
            1L, null, inicio, fin, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(prescripcionRepository, times(1))
            .findByPacienteIdAndFechaEmisionBetween(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe buscar prescripciones por consulta con filtros")
    void testSearchWithFiltersPorConsulta() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prescripcion> paginaPrescripciones = new PageImpl<>(Arrays.asList(prescripcion1), pageable, 1);

        when(prescripcionRepository.findByConsultaId(any(), any()))
            .thenReturn(paginaPrescripciones);

        // Act
        Page<PrescripcionDTO> resultado = prescripcionService.searchWithFilters(
            null, 1L, null, null, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(prescripcionRepository, times(1)).findByConsultaId(any(), any());
    }

    @Test
    @DisplayName("Debe buscar prescripciones sin filtros (todas)")
    void testSearchWithFiltersSinFiltros() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prescripcion> paginaPrescripciones = new PageImpl<>(Arrays.asList(prescripcion1, prescripcion2), pageable, 2);
        
        when(prescripcionRepository.findAll(pageable)).thenReturn(paginaPrescripciones);

        // Act
        Page<PrescripcionDTO> resultado = prescripcionService.searchWithFilters(
            null, null, null, null, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(prescripcionRepository, times(1)).findAll(pageable);
    }
}

