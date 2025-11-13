package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.NotificacionCreateDTO;
import com.clinica.veterinaria.dto.NotificacionDTO;
import com.clinica.veterinaria.entity.Notificacion;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.NotificacionRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar notificaciones del sistema.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene todas las notificaciones de un usuario
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> findByUsuarioId(Long usuarioId) {
        log.info("Obteniendo notificaciones para usuario ID: {}", usuarioId);
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(NotificacionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las notificaciones no leídas de un usuario
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> findNoLeidasByUsuarioId(Long usuarioId) {
        log.info("Obteniendo notificaciones no leídas para usuario ID: {}", usuarioId);
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(NotificacionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta las notificaciones no leídas de un usuario
     */
    @Transactional(readOnly = true)
    public long countNoLeidasByUsuarioId(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    /**
     * Crea una nueva notificación
     */
    public NotificacionDTO create(NotificacionCreateDTO dto) {
        log.info("Creando notificación para usuario ID: {}", dto.getUsuarioId());
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo())
                .mensaje(dto.getMensaje())
                .tipo(dto.getTipo())
                .leida(false)
                .entidadTipo(dto.getEntidadTipo())
                .entidadId(dto.getEntidadId())
                .build();

        notificacion = notificacionRepository.save(notificacion);
        log.info("Notificación creada exitosamente con ID: {}", notificacion.getId());
        
        return NotificacionDTO.fromEntity(notificacion);
    }

    /**
     * Marca una notificación como leída
     */
    public void marcarComoLeida(Long id, Long usuarioId) {
        log.info("Marcando notificación ID: {} como leída para usuario ID: {}", id, usuarioId);
        int updated = notificacionRepository.marcarComoLeida(id, usuarioId);
        if (updated == 0) {
            throw new RuntimeException("Notificación no encontrada o no pertenece al usuario");
        }
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    public void marcarTodasComoLeidas(Long usuarioId) {
        log.info("Marcando todas las notificaciones como leídas para usuario ID: {}", usuarioId);
        notificacionRepository.marcarTodasComoLeidas(usuarioId);
    }

    /**
     * Elimina una notificación
     */
    public void delete(Long id, Long usuarioId) {
        log.info("Eliminando notificación ID: {} para usuario ID: {}", id, usuarioId);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));
        
        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("La notificación no pertenece al usuario");
        }
        
        notificacionRepository.delete(notificacion);
    }

    /**
     * Crea una notificación de forma simplificada (método helper)
     */
    public void crearNotificacion(Long usuarioId, String titulo, String mensaje, 
                                   Notificacion.Tipo tipo, String entidadTipo, Long entidadId) {
        try {
            NotificacionCreateDTO dto = NotificacionCreateDTO.builder()
                    .usuarioId(usuarioId)
                    .titulo(titulo)
                    .mensaje(mensaje)
                    .tipo(tipo)
                    .entidadTipo(entidadTipo)
                    .entidadId(entidadId)
                    .build();
            create(dto);
        } catch (Exception e) {
            log.error("Error al crear notificación automática: {}", e.getMessage());
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }
}

