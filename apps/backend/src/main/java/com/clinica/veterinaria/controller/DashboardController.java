package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.DashboardStatsDTO;
import com.clinica.veterinaria.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para estadísticas del dashboard.
 * 
 * <p>Este controlador expone endpoints HTTP para obtener estadísticas
 * y datos agregados necesarios para el dashboard principal de la aplicación.</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>GET /api/dashboard/stats:</b> Obtiene todas las estadísticas del dashboard</li>
 * </ul>
 * 
 * <p><strong>Datos incluidos:</strong></p>
 * <ul>
 *   <li>Contadores: citas hoy, pacientes activos, consultas pendientes, propietarios</li>
 *   <li>Próximas citas del día (máximo 4)</li>
 *   <li>Consultas por día de la semana (últimos 7 días)</li>
 *   <li>Distribución de pacientes por especie</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> Todos los usuarios autenticados pueden acceder.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see DashboardService
 * @see DashboardStatsDTO
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtiene todas las estadísticas del dashboard.
     * 
     * @param fechaInicio Fecha de inicio para filtros (opcional, formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin para filtros (opcional, formato: yyyy-MM-dd)
     * @return DTO con todas las estadísticas del dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin
    ) {
        log.info("GET /api/dashboard/stats - fechaInicio: {}, fechaFin: {}", fechaInicio, fechaFin);
        
        java.time.LocalDate inicio = null;
        java.time.LocalDate fin = null;
        
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            try {
                inicio = java.time.LocalDate.parse(fechaInicio);
            } catch (Exception e) {
                log.warn("Fecha de inicio inválida: {}", fechaInicio);
            }
        }
        
        if (fechaFin != null && !fechaFin.isEmpty()) {
            try {
                fin = java.time.LocalDate.parse(fechaFin);
            } catch (Exception e) {
                log.warn("Fecha de fin inválida: {}", fechaFin);
            }
        }
        
        DashboardStatsDTO stats = dashboardService.getDashboardStats(inicio, fin);
        return ResponseEntity.ok(stats);
    }
}

