package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ReporteDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.CitaRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ReporteService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de ReporteService")
class ReporteServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReporteService reporteService;

    private Propietario propietario;
    private Paciente paciente1;
    private Paciente paciente2;
    private Usuario veterinario1;
    private Usuario veterinario2;
    private Cita cita1;
    private Cita cita2;
    private LocalDateTime fechaHoy;

    @BeforeEach
    void setUp() {
        fechaHoy = LocalDateTime.now();

        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .activo(true)
            .build();

        paciente1 = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .propietario(propietario)
            .activo(true)
            .build();

        paciente2 = Paciente.builder()
            .id(2L)
            .nombre("Luna")
            .especie("Gato")
            .raza("Siamés")
            .propietario(propietario)
            .activo(true)
            .build();

        veterinario1 = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        veterinario2 = Usuario.builder()
            .id(2L)
            .nombre("Dr. Johnson")
            .email("dr.johnson@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        cita1 = Cita.builder()
            .id(1L)
            .fecha(fechaHoy)
            .motivo("Vacunación anual")
            .estado(Cita.EstadoCita.ATENDIDA)
            .paciente(paciente1)
            .propietario(propietario)
            .profesional(veterinario1)
            .build();

        cita2 = Cita.builder()
            .id(2L)
            .fecha(fechaHoy.minusDays(1))
            .motivo("Consulta general")
            .estado(Cita.EstadoCita.PENDIENTE)
            .paciente(paciente2)
            .propietario(propietario)
            .profesional(veterinario2)
            .build();
    }

    @Test
    @DisplayName("Debe generar reporte para periodo 'hoy'")
    void testGenerarReporte_Hoy() {
        // Arrange
        when(citaRepository.count()).thenReturn(2L);
        when(consultaRepository.count()).thenReturn(5L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1, veterinario2));
        when(citaRepository.countByEstado(any())).thenReturn(1L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("hoy");

        // Assert
        assertNotNull(resultado);
        assertEquals(2L, resultado.getTotalCitas());
        assertEquals(5L, resultado.getTotalConsultas());
        assertEquals(2L, resultado.getTotalPacientes());
        assertEquals(2L, resultado.getTotalVeterinarios());
        assertNotNull(resultado.getCitasPorEstado());
        assertNotNull(resultado.getTendenciaCitas());
        assertNotNull(resultado.getPacientesPorEspecie());
        assertNotNull(resultado.getAtencionesPorVeterinario());
        assertNotNull(resultado.getTopMotivosConsulta());
    }

    @Test
    @DisplayName("Debe generar reporte para periodo 'semana'")
    void testGenerarReporte_Semana() {
        // Arrange
        when(citaRepository.count()).thenReturn(10L);
        when(consultaRepository.count()).thenReturn(15L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1));
        when(citaRepository.countByEstado(any())).thenReturn(2L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("semana");

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getTotalCitas());
        assertNotNull(resultado.getTopMotivosConsulta());
    }

    @Test
    @DisplayName("Debe generar reporte para periodo 'mes'")
    void testGenerarReporte_Mes() {
        // Arrange
        when(citaRepository.count()).thenReturn(50L);
        when(consultaRepository.count()).thenReturn(40L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1, veterinario2));
        when(citaRepository.countByEstado(any())).thenReturn(10L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("mes");

        // Assert
        assertNotNull(resultado);
        assertEquals(50L, resultado.getTotalCitas());
    }

    @Test
    @DisplayName("Debe generar reporte para periodo 'año'")
    void testGenerarReporte_Ano() {
        // Arrange
        when(citaRepository.count()).thenReturn(500L);
        when(consultaRepository.count()).thenReturn(400L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1));
        when(citaRepository.countByEstado(any())).thenReturn(100L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("año");

        // Assert
        assertNotNull(resultado);
        assertEquals(500L, resultado.getTotalCitas());
    }

    @Test
    @DisplayName("Debe usar periodo por defecto si periodo es inválido")
    void testGenerarReporte_PeriodoInvalido() {
        // Arrange
        when(citaRepository.count()).thenReturn(1L);
        when(consultaRepository.count()).thenReturn(1L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1));
        when(citaRepository.countByEstado(any())).thenReturn(0L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("periodo_invalido");

        // Assert
        assertNotNull(resultado);
        // Debe usar periodo por defecto (mes)
        verify(citaRepository, atLeastOnce()).findByFechaBetween(any(), any());
    }

    @Test
    @DisplayName("Debe calcular citas por estado correctamente")
    void testGenerarReporte_CitasPorEstado() {
        // Arrange
        when(citaRepository.count()).thenReturn(10L);
        when(consultaRepository.count()).thenReturn(5L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1));
        when(citaRepository.countByEstado(Cita.EstadoCita.PENDIENTE)).thenReturn(3L);
        when(citaRepository.countByEstado(Cita.EstadoCita.CONFIRMADA)).thenReturn(2L);
        when(citaRepository.countByEstado(Cita.EstadoCita.ATENDIDA)).thenReturn(4L);
        when(citaRepository.countByEstado(Cita.EstadoCita.CANCELADA)).thenReturn(1L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("mes");

        // Assert
        assertNotNull(resultado.getCitasPorEstado());
        assertEquals(4, resultado.getCitasPorEstado().size()); // 4 estados
        
        // Verificar que cada estado tiene su cantidad
        boolean tienePendiente = resultado.getCitasPorEstado().stream()
            .anyMatch(c -> c.getEstado().equals("PENDIENTE") && c.getCantidad() == 3L);
        assertTrue(tienePendiente);
    }

    @Test
    @DisplayName("Debe calcular distribución de pacientes por especie")
    void testGenerarReporte_PacientesPorEspecie() {
        // Arrange
        when(citaRepository.count()).thenReturn(5L);
        when(consultaRepository.count()).thenReturn(3L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2)); // Perro y Gato
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1));
        when(citaRepository.countByEstado(any())).thenReturn(1L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("mes");

        // Assert
        assertNotNull(resultado.getPacientesPorEspecie());
        assertEquals(3, resultado.getPacientesPorEspecie().size()); // Canino, Felino, Otro
        
        // Verificar que hay caninos y felinos
        boolean tieneCanino = resultado.getPacientesPorEspecie().stream()
            .anyMatch(p -> p.getEspecie().equals("Canino") && p.getCantidad() > 0);
        boolean tieneFelino = resultado.getPacientesPorEspecie().stream()
            .anyMatch(p -> p.getEspecie().equals("Felino") && p.getCantidad() > 0);
        
        assertTrue(tieneCanino);
        assertTrue(tieneFelino);
    }

    @Test
    @DisplayName("Debe limitar top motivos de consulta a 10")
    void testGenerarReporte_LimitaTopMotivos() {
        // Arrange
        List<Cita> muchasCitas = Arrays.asList(
            cita1, cita2,
            Cita.builder().id(3L).motivo("Vacunación").fecha(fechaHoy).build(),
            Cita.builder().id(4L).motivo("Consulta general").fecha(fechaHoy).build(),
            Cita.builder().id(5L).motivo("Control").fecha(fechaHoy).build(),
            Cita.builder().id(6L).motivo("Desparasitación").fecha(fechaHoy).build(),
            Cita.builder().id(7L).motivo("Cirugía").fecha(fechaHoy).build(),
            Cita.builder().id(8L).motivo("Emergencia").fecha(fechaHoy).build(),
            Cita.builder().id(9L).motivo("Otro motivo 1").fecha(fechaHoy).build(),
            Cita.builder().id(10L).motivo("Otro motivo 2").fecha(fechaHoy).build(),
            Cita.builder().id(11L).motivo("Otro motivo 3").fecha(fechaHoy).build(),
            Cita.builder().id(12L).motivo("Otro motivo 4").fecha(fechaHoy).build()
        );

        when(citaRepository.count()).thenReturn(12L);
        when(consultaRepository.count()).thenReturn(5L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1));
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario1));
        when(citaRepository.countByEstado(any())).thenReturn(3L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(muchasCitas);
        when(consultaRepository.findByProfesionalId(any()))
            .thenReturn(Arrays.asList());

        // Act
        ReporteDTO resultado = reporteService.generarReporte("mes");

        // Assert
        assertNotNull(resultado.getTopMotivosConsulta());
        assertTrue(resultado.getTopMotivosConsulta().size() <= 10);
    }

    @Test
    @DisplayName("Debe manejar datos vacíos correctamente")
    void testGenerarReporte_DatosVacios() {
        // Arrange
        when(citaRepository.count()).thenReturn(0L);
        when(consultaRepository.count()).thenReturn(0L);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList());
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList());
        when(citaRepository.countByEstado(any())).thenReturn(0L);
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        // No hay veterinarios, así que findByProfesionalId nunca se llama

        // Act
        ReporteDTO resultado = reporteService.generarReporte("mes");

        // Assert
        assertNotNull(resultado);
        assertEquals(0L, resultado.getTotalCitas());
        assertEquals(0L, resultado.getTotalConsultas());
        assertEquals(0L, resultado.getTotalPacientes());
        assertEquals(0L, resultado.getTotalVeterinarios());
        assertNotNull(resultado.getCitasPorEstado());
        assertNotNull(resultado.getTopMotivosConsulta());
        assertTrue(resultado.getTopMotivosConsulta().isEmpty());
    }
}

