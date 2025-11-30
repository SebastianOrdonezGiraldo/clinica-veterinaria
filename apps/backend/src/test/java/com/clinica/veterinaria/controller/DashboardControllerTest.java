package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.DashboardStatsDTO;
import com.clinica.veterinaria.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DashboardController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de DashboardController")
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private DashboardStatsDTO dashboardStatsDTO;

    @BeforeEach
    void setUp() {
        dashboardStatsDTO = DashboardStatsDTO.builder()
            .citasHoy(5L)
            .pacientesActivos(10L)
            .consultasPendientes(3L)
            .totalPropietarios(8L)
            .vacunacionesProximas(2L)
            .vacunacionesVencidas(1L)
            .productosStockBajo(3L)
            .prescripcionesMes(15L)
            .proximasCitas(Arrays.asList())
            .consultasPorDia(Arrays.asList())
            .distribucionEspecies(Arrays.asList())
            .citasPorEstado(Arrays.asList())
            .tendenciasConsultas(Arrays.asList())
            .actividadReciente(Arrays.asList())
            .build();
    }

    @Test
    @DisplayName("Debe obtener estadísticas sin filtros")
    void testGetStats_SinFiltros() {
        // Arrange
        when(dashboardService.getDashboardStats(any(), any())).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<DashboardStatsDTO> response = dashboardController.getStats(null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getCitasHoy());
        assertEquals(10L, response.getBody().getPacientesActivos());
        verify(dashboardService, times(1)).getDashboardStats(null, null);
    }

    @Test
    @DisplayName("Debe obtener estadísticas con filtros de fecha")
    void testGetStats_ConFiltrosFecha() {
        // Arrange
        String fechaInicio = "2025-01-01";
        String fechaFin = "2025-01-31";
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        when(dashboardService.getDashboardStats(inicio, fin)).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<DashboardStatsDTO> response = dashboardController.getStats(fechaInicio, fechaFin);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(dashboardService, times(1)).getDashboardStats(inicio, fin);
        verify(dashboardService, never()).getDashboardStats();
    }

    @Test
    @DisplayName("Debe manejar fecha inválida correctamente")
    void testGetStats_FechaInvalida() {
        // Arrange
        String fechaInvalida = "fecha-invalida";
        when(dashboardService.getDashboardStats(any(), any())).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<DashboardStatsDTO> response = dashboardController.getStats(fechaInvalida, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Debe usar el método con null cuando la fecha es inválida
        verify(dashboardService, times(1)).getDashboardStats(null, null);
    }

    @Test
    @DisplayName("Debe manejar solo fecha de inicio")
    void testGetStats_SoloFechaInicio() {
        // Arrange
        String fechaInicio = "2025-01-01";
        LocalDate inicio = LocalDate.parse(fechaInicio);
        when(dashboardService.getDashboardStats(inicio, null)).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<DashboardStatsDTO> response = dashboardController.getStats(fechaInicio, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dashboardService, times(1)).getDashboardStats(inicio, null);
    }

    @Test
    @DisplayName("Debe manejar solo fecha de fin")
    void testGetStats_SoloFechaFin() {
        // Arrange
        String fechaFin = "2025-01-31";
        LocalDate fin = LocalDate.parse(fechaFin);
        when(dashboardService.getDashboardStats(null, fin)).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<DashboardStatsDTO> response = dashboardController.getStats(null, fechaFin);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dashboardService, times(1)).getDashboardStats(null, fin);
    }

    @Test
    @DisplayName("Debe retornar todas las métricas")
    void testGetStats_TodasLasMetricas() {
        // Arrange
        when(dashboardService.getDashboardStats(any(), any())).thenReturn(dashboardStatsDTO);

        // Act
        ResponseEntity<DashboardStatsDTO> response = dashboardController.getStats(null, null);

        // Assert
        assertNotNull(response.getBody());
        DashboardStatsDTO stats = response.getBody();
        
        assertNotNull(stats.getCitasHoy());
        assertNotNull(stats.getPacientesActivos());
        assertNotNull(stats.getConsultasPendientes());
        assertNotNull(stats.getTotalPropietarios());
        assertNotNull(stats.getVacunacionesProximas());
        assertNotNull(stats.getVacunacionesVencidas());
        assertNotNull(stats.getProductosStockBajo());
        assertNotNull(stats.getPrescripcionesMes());
        assertNotNull(stats.getProximasCitas());
        assertNotNull(stats.getConsultasPorDia());
        assertNotNull(stats.getDistribucionEspecies());
        assertNotNull(stats.getCitasPorEstado());
        assertNotNull(stats.getTendenciasConsultas());
        assertNotNull(stats.getActividadReciente());
    }
}

