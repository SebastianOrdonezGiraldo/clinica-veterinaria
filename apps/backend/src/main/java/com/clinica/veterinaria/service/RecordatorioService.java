package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.NotificacionCreateDTO;
import com.clinica.veterinaria.entity.*;
import com.clinica.veterinaria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio para gestionar recordatorios automáticos.
 * 
 * <p>Este servicio ejecuta tareas programadas para enviar recordatorios
 * automáticos sobre citas próximas, vacunaciones vencidas, etc.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecordatorioService {

    private final CitaRepository citaRepository;
    private final VacunacionRepository vacunacionRepository;
    private final ProductoRepository productoRepository;
    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Verifica y envía recordatorios de citas próximas.
     * Se ejecuta cada hora.
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora
    public void enviarRecordatoriosCitas() {
        log.info("Iniciando verificación de recordatorios de citas");
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en24Horas = ahora.plusHours(24);
        LocalDateTime en1Hora = ahora.plusHours(1);
        
        // Citas en las próximas 24 horas (entre 23 y 24 horas desde ahora)
        LocalDateTime inicio24h = ahora.plusHours(23);
        List<Cita> citas24Horas = citaRepository.findByFechaBetween(
            inicio24h,
            en24Horas
        ).stream()
        .filter(cita -> cita.getEstado() == Cita.EstadoCita.CONFIRMADA || 
                       cita.getEstado() == Cita.EstadoCita.PENDIENTE)
        .toList();
        
        // Citas en la próxima hora (entre 55 minutos y 1 hora desde ahora)
        LocalDateTime inicio1h = ahora.plusMinutes(55);
        List<Cita> citas1Hora = citaRepository.findByFechaBetween(
            inicio1h,
            en1Hora
        ).stream()
        .filter(cita -> cita.getEstado() == Cita.EstadoCita.CONFIRMADA || 
                       cita.getEstado() == Cita.EstadoCita.PENDIENTE)
        .toList();
        
        // Enviar recordatorios de 24 horas
        for (Cita cita : citas24Horas) {
            if (cita.getProfesional() != null) {
                enviarRecordatorioCita(cita, cita.getProfesional().getId(), 24);
            }
        }
        
        // Enviar recordatorios de 1 hora
        for (Cita cita : citas1Hora) {
            if (cita.getProfesional() != null) {
                enviarRecordatorioCita(cita, cita.getProfesional().getId(), 1);
            }
        }
        
        log.info("Recordatorios de citas procesados: {} en 24h, {} en 1h", 
            citas24Horas.size(), citas1Hora.size());
    }

    /**
     * Verifica y envía alertas de vacunaciones vencidas y próximas.
     * Se ejecuta diariamente a las 8:00 AM.
     */
    @Scheduled(cron = "0 0 8 * * *") // Diariamente a las 8:00 AM
    public void enviarAlertasVacunaciones() {
        log.info("Iniciando verificación de alertas de vacunaciones");
        
        LocalDate hoy = LocalDate.now();
        
        // Vacunaciones vencidas
        List<Vacunacion> vencidas = vacunacionRepository.findVencidas(hoy);
        for (Vacunacion vacunacion : vencidas) {
            if (vacunacion.getPaciente() != null && 
                vacunacion.getPaciente().getPropietario() != null) {
                // Notificar a todos los veterinarios
                List<Usuario> veterinarios = usuarioRepository.findByRolAndActivo(
                    Usuario.Rol.VET, true
                );
                for (Usuario vet : veterinarios) {
                    enviarAlertaVacunacionVencida(vacunacion, vet.getId());
                }
            }
        }
        
        // Vacunaciones próximas a vencer (próximos 7 días)
        List<Vacunacion> proximas = vacunacionRepository.findProximasAVencer(
            hoy, hoy.plusDays(7)
        );
        for (Vacunacion vacunacion : proximas) {
            if (vacunacion.getPaciente() != null && 
                vacunacion.getPaciente().getPropietario() != null) {
                List<Usuario> veterinarios = usuarioRepository.findByRolAndActivo(
                    Usuario.Rol.VET, true
                );
                for (Usuario vet : veterinarios) {
                    enviarAlertaVacunacionProxima(vacunacion, vet.getId());
                }
            }
        }
        
        log.info("Alertas de vacunaciones procesadas: {} vencidas, {} próximas", 
            vencidas.size(), proximas.size());
    }

    /**
     * Verifica y envía alertas de productos con stock bajo.
     * Se ejecuta diariamente a las 9:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * *") // Diariamente a las 9:00 AM
    public void enviarAlertasStockBajo() {
        log.info("Iniciando verificación de alertas de stock bajo");
        
        List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();
        
        if (!productosStockBajo.isEmpty()) {
            // Notificar a administradores y recepcionistas
            List<Usuario> usuarios = usuarioRepository.findByRolInAndActivo(
                List.of(Usuario.Rol.ADMIN, Usuario.Rol.RECEPCION), true
            );
            
            for (Usuario usuario : usuarios) {
                enviarAlertaStockBajo(productosStockBajo, usuario.getId());
            }
        }
        
        log.info("Alertas de stock bajo procesadas: {} productos", productosStockBajo.size());
    }

    private void enviarRecordatorioCita(Cita cita, Long usuarioId, int horas) {
        try {
            String titulo = horas == 1 
                ? "Recordatorio: Cita en 1 hora" 
                : "Recordatorio: Cita en 24 horas";
            
            String mensaje = String.format(
                "Tienes una cita programada %s:\n" +
                "Paciente: %s\n" +
                "Hora: %s\n" +
                "Motivo: %s",
                horas == 1 ? "en 1 hora" : "mañana",
                cita.getPaciente() != null ? cita.getPaciente().getNombre() : "N/A",
                cita.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                cita.getMotivo()
            );
            
            NotificacionCreateDTO dto = NotificacionCreateDTO.builder()
                .usuarioId(usuarioId)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(Notificacion.Tipo.CITA)
                .entidadTipo("CITA")
                .entidadId(cita.getId())
                .build();
            
            notificacionService.create(dto);
        } catch (Exception e) {
            log.error("Error al enviar recordatorio de cita: {}", e.getMessage(), e);
        }
    }

    private void enviarAlertaVacunacionVencida(Vacunacion vacunacion, Long usuarioId) {
        try {
            String titulo = "Alerta: Vacunación vencida";
            String mensaje = String.format(
                "La vacunación del paciente %s está vencida:\n" +
                "Vacuna: %s\n" +
                "Próxima dosis: %s",
                vacunacion.getPaciente() != null ? vacunacion.getPaciente().getNombre() : "N/A",
                vacunacion.getVacuna() != null ? vacunacion.getVacuna().getNombre() : "N/A",
                vacunacion.getProximaDosis() != null ? vacunacion.getProximaDosis().toString() : "N/A"
            );
            
            NotificacionCreateDTO dto = NotificacionCreateDTO.builder()
                .usuarioId(usuarioId)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(Notificacion.Tipo.SISTEMA)
                .entidadTipo("VACUNACION")
                .entidadId(vacunacion.getId())
                .build();
            
            notificacionService.create(dto);
        } catch (Exception e) {
            log.error("Error al enviar alerta de vacunación vencida: {}", e.getMessage(), e);
        }
    }

    private void enviarAlertaVacunacionProxima(Vacunacion vacunacion, Long usuarioId) {
        try {
            String titulo = "Recordatorio: Vacunación próxima";
            String mensaje = String.format(
                "El paciente %s tiene una vacunación próxima:\n" +
                "Vacuna: %s\n" +
                "Próxima dosis: %s",
                vacunacion.getPaciente() != null ? vacunacion.getPaciente().getNombre() : "N/A",
                vacunacion.getVacuna() != null ? vacunacion.getVacuna().getNombre() : "N/A",
                vacunacion.getProximaDosis() != null ? vacunacion.getProximaDosis().toString() : "N/A"
            );
            
            NotificacionCreateDTO dto = NotificacionCreateDTO.builder()
                .usuarioId(usuarioId)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(Notificacion.Tipo.SISTEMA)
                .entidadTipo("VACUNACION")
                .entidadId(vacunacion.getId())
                .build();
            
            notificacionService.create(dto);
        } catch (Exception e) {
            log.error("Error al enviar alerta de vacunación próxima: {}", e.getMessage(), e);
        }
    }

    private void enviarAlertaStockBajo(List<Producto> productos, Long usuarioId) {
        try {
            String titulo = "Alerta: Productos con stock bajo";
            StringBuilder mensaje = new StringBuilder(
                String.format("Se encontraron %d productos con stock bajo:\n\n", productos.size())
            );
            
            for (int i = 0; i < Math.min(productos.size(), 10); i++) {
                Producto producto = productos.get(i);
                mensaje.append(String.format(
                    "• %s - Stock: %.0f (Mínimo: %.0f)\n",
                    producto.getNombre(),
                    producto.getStockActual().doubleValue(),
                    producto.getStockMinimo() != null ? producto.getStockMinimo().doubleValue() : 0
                ));
            }
            
            if (productos.size() > 10) {
                mensaje.append(String.format("\n... y %d productos más", productos.size() - 10));
            }
            
            NotificacionCreateDTO dto = NotificacionCreateDTO.builder()
                .usuarioId(usuarioId)
                .titulo(titulo)
                .mensaje(mensaje.toString())
                .tipo(Notificacion.Tipo.SISTEMA)
                .entidadTipo("INVENTARIO")
                .build();
            
            notificacionService.create(dto);
        } catch (Exception e) {
            log.error("Error al enviar alerta de stock bajo: {}", e.getMessage(), e);
        }
    }
}

