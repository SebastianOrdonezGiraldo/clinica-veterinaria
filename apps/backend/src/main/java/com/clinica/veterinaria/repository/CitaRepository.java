package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Cita.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Cita}.
 * 
 * <p>Proporciona métodos de acceso a datos para citas médicas, incluyendo búsquedas
 * por paciente, propietario, profesional, estado y rango de fechas. Incluye métodos
 * personalizados con queries JPQL para consultas más complejas.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Búsquedas por relaciones (paciente, propietario, profesional)</li>
 *   <li>Filtros por estado y rango de fechas</li>
 *   <li>Consultas personalizadas para estadísticas y reportes</li>
 *   <li>Soporte de paginación para listados grandes</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Cita
 * @see CitaService
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca citas por paciente
     * @param pacienteId ID del paciente
     * @return Lista de citas del paciente
     */
    List<Cita> findByPacienteId(Long pacienteId);

    /**
     * Busca citas por propietario
     * @param propietarioId ID del propietario
     * @return Lista de citas del propietario
     */
    List<Cita> findByPropietarioId(Long propietarioId);

    /**
     * Busca citas por profesional
     * @param profesionalId ID del profesional
     * @return Lista de citas del profesional
     */
    List<Cita> findByProfesionalId(Long profesionalId);

    /**
     * Busca citas por estado
     * @param estado Estado de la cita
     * @return Lista de citas con ese estado
     */
    List<Cita> findByEstado(EstadoCita estado);

    /**
     * Busca citas entre fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de citas en ese rango
     */
    List<Cita> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca citas por profesional y estado
     * @param profesionalId ID del profesional
     * @param estado Estado de la cita
     * @return Lista de citas
     */
    List<Cita> findByProfesionalIdAndEstado(Long profesionalId, EstadoCita estado);

    /**
     * Busca citas del día por profesional
     * @param profesionalId ID del profesional
     * @param fechaInicio Inicio del día
     * @param fechaFin Fin del día
     * @return Lista de citas del día
     */
    @Query("SELECT c FROM Cita c WHERE c.profesional.id = :profesionalId " +
           "AND c.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY c.fecha")
    List<Cita> findCitasDelDiaPorProfesional(
        @Param("profesionalId") Long profesionalId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca próximas citas de un paciente
     * @param pacienteId ID del paciente
     * @param fechaActual Fecha actual
     * @return Lista de próximas citas
     */
    @Query("SELECT c FROM Cita c WHERE c.paciente.id = :pacienteId " +
           "AND c.fecha >= :fechaActual " +
           "ORDER BY c.fecha")
    List<Cita> findProximasCitasPaciente(
        @Param("pacienteId") Long pacienteId,
        @Param("fechaActual") LocalDateTime fechaActual
    );

    /**
     * Busca citas con paginación
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @param pageable Configuración de paginación
     * @return Página de citas
     */
    Page<Cita> findByFechaBetween(
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Pageable pageable
    );

    /**
     * Cuenta citas por estado
     * @param estado Estado a contar
     * @return Cantidad de citas con ese estado
     */
    long countByEstado(EstadoCita estado);

    /**
     * Cuenta citas pendientes de un profesional
     * @param profesionalId ID del profesional
     * @return Cantidad de citas pendientes
     */
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.profesional.id = :profesionalId " +
           "AND c.estado = 'PENDIENTE'")
    long countCitasPendientesPorProfesional(@Param("profesionalId") Long profesionalId);
}

