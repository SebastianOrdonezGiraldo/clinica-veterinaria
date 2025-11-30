package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.DashboardStatsDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.*;
import com.clinica.veterinaria.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DashboardService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de DashboardService")
class DashboardServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private VacunacionRepository vacunacionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PrescripcionRepository prescripcionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Propietario propietario;
    private Paciente paciente1;
    private Paciente paciente2;
    private Usuario veterinario;
    private Cita cita1;
    private Cita cita2;
    private Consulta consulta1;
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

        veterinario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        cita1 = Cita.builder()
            .id(1L)
            .fecha(fechaHoy.plusHours(2))
            .motivo("Consulta general")
            .estado(Cita.EstadoCita.CONFIRMADA)
            .paciente(paciente1)
            .propietario(propietario)
            .profesional(veterinario)
            .build();

        cita2 = Cita.builder()
            .id(2L)
            .fecha(fechaHoy.plusHours(4))
            .motivo("Vacunación")
            .estado(Cita.EstadoCita.PENDIENTE)
            .paciente(paciente2)
            .propietario(propietario)
            .profesional(veterinario)
            .build();

        consulta1 = Consulta.builder()
            .id(1L)
            .fecha(fechaHoy.minusDays(2))
            .diagnostico("Gastritis")
            .paciente(paciente1)
            .profesional(veterinario)
            .build();
    }

    @Test
    @DisplayName("Debe obtener estadísticas del dashboard")
    void testGetDashboardStats() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(consulta1));
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList(cita1, cita2));

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertEquals(2L, resultado.getCitasHoy());
        assertEquals(2L, resultado.getPacientesActivos());
        assertEquals(2L, resultado.getConsultasPendientes());
        assertEquals(1L, resultado.getTotalPropietarios());
        assertNotNull(resultado.getProximasCitas());
        assertNotNull(resultado.getConsultasPorDia());
        assertNotNull(resultado.getDistribucionEspecies());
        assertNotNull(resultado.getCitasPorEstado());
        assertNotNull(resultado.getTendenciasConsultas());
        assertNotNull(resultado.getActividadReciente());
        
        // Verificar nuevas métricas
        assertNotNull(resultado.getVacunacionesProximas());
        assertNotNull(resultado.getVacunacionesVencidas());
        assertNotNull(resultado.getProductosStockBajo());
        assertNotNull(resultado.getPrescripcionesMes());
        
        // Se llama múltiples veces: en getDashboardStats, getProximasCitas y getActividadReciente
        verify(citaRepository, atLeast(2)).findByFechaBetween(any(), any());
        // Se llama 2 veces: una en getDashboardStats y otra en getDistribucionEspecies
        verify(pacienteRepository, times(2)).findByActivo(true);
        verify(propietarioRepository, times(1)).findByActivo(true);
        verify(vacunacionRepository, times(1)).findProximasAVencer(any(), any());
        verify(vacunacionRepository, times(1)).findVencidas(any());
        verify(productoRepository, times(1)).findProductosConStockBajo();
        verify(prescripcionRepository, times(1)).findByFechaEmisionBetween(any(), any());
    }

    @Test
    @DisplayName("Debe limitar próximas citas a 4")
    void testGetDashboardStats_LimitaProximasCitas() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        // Crear más de 4 citas
        List<Cita> muchasCitas = Arrays.asList(
            cita1, cita2,
            Cita.builder().id(3L).fecha(fechaHoy.plusHours(6)).paciente(paciente1).propietario(propietario).build(),
            Cita.builder().id(4L).fecha(fechaHoy.plusHours(8)).paciente(paciente2).propietario(propietario).build(),
            Cita.builder().id(5L).fecha(fechaHoy.plusHours(10)).paciente(paciente1).propietario(propietario).build()
        );

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(muchasCitas);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(muchasCitas);
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado.getProximasCitas());
        assertTrue(resultado.getProximasCitas().size() <= 4);
    }

    @Test
    @DisplayName("Debe calcular distribución de especies correctamente")
    void testGetDashboardStats_DistribucionEspecies() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList());
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2)); // Perro y Gato
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList());
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList());

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado.getDistribucionEspecies());
        assertEquals(3, resultado.getDistribucionEspecies().size()); // Caninos, Felinos, Otros
        
        // Verificar que hay al menos un canino y un felino
        boolean tieneCanino = resultado.getDistribucionEspecies().stream()
            .anyMatch(d -> d.getNombre().equals("Caninos") && d.getValor() > 0);
        boolean tieneFelino = resultado.getDistribucionEspecies().stream()
            .anyMatch(d -> d.getNombre().equals("Felinos") && d.getValor() > 0);
        
        assertTrue(tieneCanino);
        assertTrue(tieneFelino);
    }

    @Test
    @DisplayName("Debe manejar datos vacíos correctamente")
    void testGetDashboardStats_DatosVacios() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList());
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList());
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList());
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList());
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList());

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertEquals(0L, resultado.getCitasHoy());
        assertEquals(0L, resultado.getPacientesActivos());
        assertEquals(0L, resultado.getConsultasPendientes());
        assertEquals(0L, resultado.getTotalPropietarios());
        assertNotNull(resultado.getProximasCitas());
        assertTrue(resultado.getProximasCitas().isEmpty());
        assertNotNull(resultado.getConsultasPorDia());
        assertNotNull(resultado.getDistribucionEspecies());
    }

    @Test
    @DisplayName("Debe ordenar próximas citas por fecha")
    void testGetDashboardStats_OrdenaProximasCitas() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        Cita citaTarde = Cita.builder()
            .id(3L)
            .fecha(fechaHoy.plusHours(6))
            .paciente(paciente1)
            .propietario(propietario)
            .build();

        Cita citaTemprano = Cita.builder()
            .id(4L)
            .fecha(fechaHoy.plusHours(1))
            .paciente(paciente2)
            .propietario(propietario)
            .build();

        // Citas en orden desordenado
        List<Cita> citasDesordenadas = Arrays.asList(citaTarde, cita1, citaTemprano, cita2);

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(citasDesordenadas);
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(citasDesordenadas);
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList());

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado.getProximasCitas());
        if (resultado.getProximasCitas().size() > 1) {
            // Verificar que están ordenadas por fecha
            for (int i = 0; i < resultado.getProximasCitas().size() - 1; i++) {
                LocalDateTime fecha1 = resultado.getProximasCitas().get(i).getFecha();
                LocalDateTime fecha2 = resultado.getProximasCitas().get(i + 1).getFecha();
                assertTrue(fecha1.isBefore(fecha2) || fecha1.isEqual(fecha2));
            }
        }
    }

    @Test
    @DisplayName("Debe obtener estadísticas con filtros de fecha")
    void testGetDashboardStats_ConFiltrosFecha() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(7);
        LocalDate fechaFin = LocalDate.now();

        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(consulta1));
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList(cita1, cita2));

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats(fechaInicio, fechaFin);

        // Assert
        assertNotNull(resultado);
        // Se llama múltiples veces: en getConsultasPorDia, getTendenciasConsultas y getActividadReciente
        verify(consultaRepository, atLeast(1)).findByFechaBetween(any(), any());
    }

    @Test
    @DisplayName("Debe calcular vacunaciones próximas y vencidas")
    void testGetDashboardStats_Vacunaciones() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        Vacuna vacuna = Vacuna.builder()
            .id(1L)
            .nombre("Antirrábica")
            .especie("Canino")
            .activo(true)
            .build();

        Vacunacion vacunacionProxima = Vacunacion.builder()
            .id(1L)
            .paciente(paciente1)
            .vacuna(vacuna)
            .proximaDosis(hoy.plusDays(15))
            .build();

        Vacunacion vacunacionVencida = Vacunacion.builder()
            .id(2L)
            .paciente(paciente2)
            .vacuna(vacuna)
            .proximaDosis(hoy.minusDays(5))
            .build();

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList());
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList());
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList(vacunacionProxima));
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList(vacunacionVencida));
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList());

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getVacunacionesProximas());
        assertEquals(1L, resultado.getVacunacionesVencidas());
    }

    @Test
    @DisplayName("Debe calcular productos con stock bajo")
    void testGetDashboardStats_ProductosStockBajo() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        CategoriaProducto categoria = CategoriaProducto.builder()
            .id(1L)
            .nombre("Medicamentos")
            .build();

        Producto productoStockBajo = Producto.builder()
            .id(1L)
            .nombre("Medicamento A")
            .stockActual(java.math.BigDecimal.valueOf(5))
            .stockMinimo(java.math.BigDecimal.valueOf(10))
            .activo(true)
            .categoria(categoria)
            .build();

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList());
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList());
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList(productoStockBajo));
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList());

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getProductosStockBajo());
    }

    @Test
    @DisplayName("Debe calcular prescripciones del mes")
    void testGetDashboardStats_PrescripcionesMes() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        Prescripcion prescripcion = Prescripcion.builder()
            .id(1L)
            .fechaEmision(LocalDateTime.now().minusDays(5))
            .consulta(consulta1)
            .build();

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList());
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList());
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(consulta1));
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList(prescripcion));
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList());
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(consulta1));

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getPrescripcionesMes());
    }

    @Test
    @DisplayName("Debe obtener citas por estado")
    void testGetDashboardStats_CitasPorEstado() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        Cita citaConfirmada = Cita.builder()
            .id(3L)
            .fecha(fechaHoy.plusHours(1))
            .estado(Cita.EstadoCita.CONFIRMADA)
            .paciente(paciente1)
            .propietario(propietario)
            .build();

        Cita citaAtendida = Cita.builder()
            .id(4L)
            .fecha(fechaHoy.minusDays(1))
            .estado(Cita.EstadoCita.ATENDIDA)
            .paciente(paciente2)
            .propietario(propietario)
            .build();

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1, paciente2));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList(cita1, cita2));
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList(cita1, cita2, citaConfirmada, citaAtendida));

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getCitasPorEstado());
        assertTrue(resultado.getCitasPorEstado().size() > 0);
        
        // Verificar que hay citas por estado
        boolean tieneConfirmada = resultado.getCitasPorEstado().stream()
            .anyMatch(c -> c.getEstado().equals("CONFIRMADA") && c.getCantidad() > 0);
        assertTrue(tieneConfirmada);
    }

    @Test
    @DisplayName("Debe obtener actividad reciente")
    void testGetDashboardStats_ActividadReciente() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        Consulta consultaReciente = Consulta.builder()
            .id(2L)
            .fecha(LocalDateTime.now().minusDays(1))
            .paciente(paciente1)
            .profesional(veterinario)
            .build();

        Prescripcion prescripcionReciente = Prescripcion.builder()
            .id(1L)
            .fechaEmision(LocalDateTime.now().minusDays(2))
            .consulta(consultaReciente)
            .build();

        when(citaRepository.findByFechaBetween(inicioHoy, finHoy))
            .thenReturn(Arrays.asList(cita1));
        when(pacienteRepository.findByActivo(true))
            .thenReturn(Arrays.asList(paciente1));
        when(citaRepository.findByEstadoIn(any()))
            .thenReturn(Arrays.asList(cita1));
        when(propietarioRepository.findByActivo(true))
            .thenReturn(Arrays.asList(propietario));
        when(consultaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList(consulta1, consultaReciente));
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());
        when(prescripcionRepository.findByFechaEmisionBetween(any(), any()))
            .thenReturn(Arrays.asList(prescripcionReciente));
        when(citaRepository.findAll())
            .thenReturn(Arrays.asList(cita1));

        // Act
        DashboardStatsDTO resultado = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getActividadReciente());
        assertTrue(resultado.getActividadReciente().size() > 0);
        assertTrue(resultado.getActividadReciente().size() <= 5);
    }
}

