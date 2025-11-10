package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ReporteDTO;
import com.clinica.veterinaria.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para generación de reportes operativos.
 * 
 * <p>Este controlador expone endpoints HTTP para generar reportes y estadísticas
 * de la clínica veterinaria, incluyendo análisis de citas, consultas, pacientes
 * y veterinarios.</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>GET /api/reportes:</b> Genera un reporte completo</li>
 *   <li><b>GET /api/reportes?periodo={periodo}:</b> Genera reporte para un periodo específico</li>
 * </ul>
 * 
 * <p><strong>Periodos disponibles:</strong></p>
 * <ul>
 *   <li><b>hoy:</b> Reporte del día actual</li>
 *   <li><b>semana:</b> Últimos 7 días</li>
 *   <li><b>mes:</b> Último mes</li>
 *   <li><b>año:</b> Último año</li>
 * </ul>
 * 
 * <p><strong>Datos incluidos en el reporte:</b></p>
 * <ul>
 *   <li>Estadísticas generales (total citas, consultas, pacientes, veterinarios)</li>
 *   <li>Distribución de citas por estado</li>
 *   <li>Tendencia de citas por mes (últimos 6 meses)</li>
 *   <li>Distribución de pacientes por especie</li>
 *   <li>Atenciones por veterinario</li>
 *   <li>Top motivos de consulta</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> ADMIN, VET</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see ReporteService
 * @see ReporteDTO
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    /**
     * Genera un reporte completo con todas las estadísticas.
     * 
     * @param periodo Periodo del reporte (opcional, por defecto "mes")
     * @return DTO con todas las estadísticas del reporte
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<ReporteDTO> generarReporte(
            @RequestParam(defaultValue = "mes") String periodo) {
        log.info("GET /api/reportes?periodo={}", periodo);
        ReporteDTO reporte = reporteService.generarReporte(periodo);
        return ResponseEntity.ok(reporte);
    }
}

