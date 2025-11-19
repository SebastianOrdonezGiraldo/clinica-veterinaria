package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ConsultaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de ConsultaService")
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private Propietario propietario;
    private Paciente paciente;
    private Usuario profesional;
    private Consulta consulta1;
    private Consulta consulta2;
    private LocalDateTime fechaConsulta;

    @BeforeEach
    void setUp() {
        fechaConsulta = LocalDateTime.now();

        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .activo(true)
            .build();

        paciente = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .propietario(propietario)
            .activo(true)
            .build();

        profesional = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        consulta1 = Consulta.builder()
            .id(1L)
            .fecha(fechaConsulta)
            .frecuenciaCardiaca(120)
            .frecuenciaRespiratoria(30)
            .temperatura(new BigDecimal("38.5"))
            .pesoKg(new BigDecimal("30.0"))
            .examenFisico("Estado general bueno")
            .diagnostico("Saludable")
            .tratamiento("Ninguno")
            .paciente(paciente)
            .profesional(profesional)
            .build();

        consulta2 = Consulta.builder()
            .id(2L)
            .fecha(fechaConsulta.minusDays(30))
            .frecuenciaCardiaca(110)
            .frecuenciaRespiratoria(28)
            .temperatura(new BigDecimal("38.0"))
            .pesoKg(new BigDecimal("28.5"))
            .examenFisico("Estado general bueno")
            .diagnostico("Control rutinario")
            .tratamiento("Vacunación")
            .paciente(paciente)
            .profesional(profesional)
            .build();
    }

    @Test
    @DisplayName("Debe listar todas las consultas")
    void testFindAll() {
        // Arrange
        when(consultaRepository.findAll()).thenReturn(Arrays.asList(consulta1, consulta2));

        // Act
        List<ConsultaDTO> resultado = consultaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Saludable", resultado.get(0).getDiagnostico());
        verify(consultaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener consulta por ID")
    void testFindById() {
        // Arrange
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta1));

        // Act
        ConsultaDTO resultado = consultaService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Saludable", resultado.getDiagnostico());
        assertEquals(120, resultado.getFrecuenciaCardiaca());
        assertEquals(new BigDecimal("38.5"), resultado.getTemperatura());
        verify(consultaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando consulta no existe")
    void testFindByIdNoExiste() {
        // Arrange
        when(consultaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> consultaService.findById(999L));
        verify(consultaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe crear una nueva consulta")
    void testCreate() {
        // Arrange
        ConsultaDTO consultaDTO = ConsultaDTO.builder()
            .fecha(fechaConsulta)
            .frecuenciaCardiaca(115)
            .frecuenciaRespiratoria(25)
            .temperatura(new BigDecimal("38.2"))
            .pesoKg(new BigDecimal("29.5"))
            .examenFisico("Examen completo realizado")
            .diagnostico("Gastritis leve")
            .tratamiento("Dieta blanda por 3 días")
            .pacienteId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> {
            Consulta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // Act
        ConsultaDTO resultado = consultaService.create(consultaDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Gastritis leve", resultado.getDiagnostico());
        assertEquals(115, resultado.getFrecuenciaCardiaca());
        assertEquals(1L, resultado.getPacienteId());
        assertEquals(1L, resultado.getProfesionalId());
        verify(pacienteRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).findById(1L);
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Debe asignar fecha automática si no se proporciona")
    void testCreateSinFecha() {
        // Arrange
        ConsultaDTO consultaDTO = ConsultaDTO.builder()
            .frecuenciaCardiaca(120)
            .diagnostico("Consulta sin fecha")
            .pacienteId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> {
            Consulta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // Act
        ConsultaDTO resultado = consultaService.create(consultaDTO);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getFecha()); // Debe tener fecha asignada
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear consulta con paciente inexistente")
    void testCreatePacienteNoExiste() {
        // Arrange
        ConsultaDTO consultaDTO = ConsultaDTO.builder()
            .diagnostico("Consulta")
            .pacienteId(999L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> consultaService.create(consultaDTO));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear consulta con profesional inexistente")
    void testCreateProfesionalNoExiste() {
        // Arrange
        ConsultaDTO consultaDTO = ConsultaDTO.builder()
            .diagnostico("Consulta")
            .pacienteId(1L)
            .profesionalId(999L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> consultaService.create(consultaDTO));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Debe actualizar una consulta existente")
    void testUpdate() {
        // Arrange
        ConsultaDTO actualizacionDTO = ConsultaDTO.builder()
            .fecha(fechaConsulta)
            .frecuenciaCardiaca(125)
            .frecuenciaRespiratoria(32)
            .temperatura(new BigDecimal("39.0"))
            .pesoKg(new BigDecimal("31.0"))
            .examenFisico("Examen actualizado")
            .diagnostico("Diagnóstico actualizado")
            .tratamiento("Tratamiento actualizado")
            .pacienteId(1L)
            .profesionalId(1L)
            .build();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta1));
        // El profesional no cambia (mismo ID), no se necesita el stubbing
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConsultaDTO resultado = consultaService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Diagnóstico actualizado", resultado.getDiagnostico());
        assertEquals(125, resultado.getFrecuenciaCardiaca());
        assertEquals(new BigDecimal("39.0"), resultado.getTemperatura());
        verify(consultaRepository, times(1)).findById(1L);
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Debe actualizar profesional si cambia")
    void testUpdateCambiarProfesional() {
        // Arrange
        Usuario nuevoProfesional = Usuario.builder()
            .id(2L)
            .nombre("Dr. Jones")
            .email("dr.jones@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        ConsultaDTO actualizacionDTO = ConsultaDTO.builder()
            .fecha(fechaConsulta)
            .diagnostico("Diagnóstico")
            .pacienteId(1L)
            .profesionalId(2L) // Cambio de profesional
            .build();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta1));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(nuevoProfesional));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConsultaDTO resultado = consultaService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(2L, resultado.getProfesionalId());
        verify(usuarioRepository, times(1)).findById(2L);
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Debe eliminar una consulta")
    void testDelete() {
        // Arrange
        when(consultaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(consultaRepository).deleteById(1L);

        // Act
        consultaService.delete(1L);

        // Assert
        verify(consultaRepository, times(1)).existsById(1L);
        verify(consultaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar consulta inexistente")
    void testDeleteNoExiste() {
        // Arrange
        when(consultaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> consultaService.delete(999L));
        verify(consultaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe buscar consultas por paciente")
    void testFindByPaciente() {
        // Arrange
        when(consultaRepository.findByPacienteIdOrderByFechaDesc(1L))
            .thenReturn(Arrays.asList(consulta1, consulta2));

        // Act
        List<ConsultaDTO> resultado = consultaService.findByPaciente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(consultaRepository, times(1)).findByPacienteIdOrderByFechaDesc(1L);
    }

    @Test
    @DisplayName("Debe buscar consultas por profesional")
    void testFindByProfesional() {
        // Arrange
        when(consultaRepository.findByProfesionalId(1L))
            .thenReturn(Arrays.asList(consulta1, consulta2));

        // Act
        List<ConsultaDTO> resultado = consultaService.findByProfesional(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(consultaRepository, times(1)).findByProfesionalId(1L);
    }

    @Test
    @DisplayName("Debe buscar consultas por rango de fechas")
    void testFindByFechaRange() {
        // Arrange
        LocalDateTime inicio = fechaConsulta.minusDays(1);
        LocalDateTime fin = fechaConsulta.plusDays(1);
        
        when(consultaRepository.findByFechaBetween(inicio, fin))
            .thenReturn(Arrays.asList(consulta1));

        // Act
        List<ConsultaDTO> resultado = consultaService.findByFechaRange(inicio, fin);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(consultaRepository, times(1)).findByFechaBetween(inicio, fin);
    }

    @Test
    @DisplayName("Debe buscar consultas con filtros y paginación")
    void testSearchWithFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Consulta> paginaConsultas = new PageImpl<>(Arrays.asList(consulta1, consulta2), pageable, 2);
        
        LocalDateTime inicio = fechaConsulta.minusDays(1);
        LocalDateTime fin = fechaConsulta.plusDays(1);

        when(consultaRepository.findByPacienteIdAndFechaBetween(any(), any(), any(), any()))
            .thenReturn(paginaConsultas);

        // Act
        Page<ConsultaDTO> resultado = consultaService.searchWithFilters(
            1L, 1L, inicio, fin, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(consultaRepository, times(1))
            .findByPacienteIdAndFechaBetween(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe buscar consultas sin filtros (todas)")
    void testSearchWithFiltersSinFiltros() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Consulta> paginaConsultas = new PageImpl<>(Arrays.asList(consulta1, consulta2), pageable, 2);
        
        when(consultaRepository.findAll(pageable)).thenReturn(paginaConsultas);

        // Act
        Page<ConsultaDTO> resultado = consultaService.searchWithFilters(
            null, null, null, null, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(consultaRepository, times(1)).findAll(pageable);
    }
}

