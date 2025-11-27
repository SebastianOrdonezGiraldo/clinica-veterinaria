package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.service.CitaService;
import com.clinica.veterinaria.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador REST público para recursos accesibles sin autenticación.
 * 
 * <p>Este controlador expone endpoints públicos que pueden ser accedidos sin
 * autenticación, útiles para funcionalidades públicas como agendar citas.</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>GET /api/public/veterinarios:</b> Lista veterinarios activos disponibles</li>
 *   <li><b>GET /api/public/veterinarios/{id}/horas-ocupadas:</b> Obtiene las horas ocupadas de un veterinario en una fecha</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PublicController {

    private final UsuarioService usuarioService;
    private final CitaService citaService;

    /**
     * Obtiene la lista de veterinarios activos disponibles para agendar citas.
     * 
     * <p>Este endpoint es público y no requiere autenticación. Útil para que
     * los clientes puedan ver qué veterinarios están disponibles al agendar
     * una cita.</p>
     * 
     * @return Lista de veterinarios activos.
     */
    @GetMapping("/veterinarios")
    public ResponseEntity<List<UsuarioDTO>> getVeterinarios() {
        log.info("GET /api/public/veterinarios");
        return ResponseEntity.ok(usuarioService.findVeterinariosActivos());
    }

    /**
     * Obtiene las horas ocupadas de un veterinario en una fecha específica.
     * 
     * <p>Este endpoint es público y retorna las citas no canceladas de un veterinario
     * en un día específico, útil para mostrar solo las horas disponibles al cliente.</p>
     * 
     * @param veterinarioId ID del veterinario
     * @param fecha Fecha en formato yyyy-MM-dd
     * @return Lista de citas ocupadas (solo horas, sin información sensible)
     */
    @GetMapping("/veterinarios/{veterinarioId}/horas-ocupadas")
    public ResponseEntity<List<String>> getHorasOcupadas(
            @PathVariable Long veterinarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        log.info("GET /api/public/veterinarios/{}/horas-ocupadas?fecha={}", veterinarioId, fecha);
        
        // Calcular inicio y fin del día
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(LocalTime.MAX);
        
        // Obtener citas del veterinario en ese día
        List<CitaDTO> citas = citaService.findByProfesionalAndFechaRange(
            veterinarioId, inicioDia, finDia);
        
        // Extraer solo las horas (formato HH:mm) de las citas no canceladas
        List<String> horasOcupadas = citas.stream()
            .filter(cita -> {
                Cita.EstadoCita estado = cita.getEstado();
                return estado != null && estado != Cita.EstadoCita.CANCELADA;
            })
            .map(cita -> {
                LocalDateTime fechaCita = cita.getFecha();
                if (fechaCita == null) return null;
                return fechaCita.toLocalTime().toString().substring(0, 5); // HH:mm
            })
            .filter(hora -> hora != null)
            .distinct()
            .sorted()
            .toList();
        
        return ResponseEntity.ok(horasOcupadas);
    }
}

