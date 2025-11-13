package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Notificacion}.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-09
 * @see Notificacion
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Busca todas las notificaciones de un usuario, ordenadas por fecha de creación descendente
     */
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    /**
     * Busca las notificaciones no leídas de un usuario
     */
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Long usuarioId);

    /**
     * Cuenta las notificaciones no leídas de un usuario
     */
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.id = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);

    /**
     * Marca una notificación específica como leída
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.id = :id AND n.usuario.id = :usuarioId")
    int marcarComoLeida(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    /**
     * Elimina notificaciones antiguas (más de X días)
     */
    @Modifying
    @Query("DELETE FROM Notificacion n WHERE n.fechaCreacion < :fechaLimite")
    int eliminarAntiguas(@Param("fechaLimite") java.time.LocalDateTime fechaLimite);
}

