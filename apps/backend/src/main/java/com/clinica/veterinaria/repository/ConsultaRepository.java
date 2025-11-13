package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Consulta}.
 * 
 * <p>Proporciona métodos de acceso a datos para consultas médicas (historia clínica),
 * incluyendo búsquedas por paciente, profesional, rango de fechas y consultas con
 * prescripciones cargadas. Incluye métodos para estadísticas y reportes.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Búsquedas por paciente ordenadas por fecha (más recientes primero)</li>
 *   <li>Filtros por profesional y rango de fechas</li>
 *   <li>Consultas con prescripciones cargadas (JOIN FETCH)</li>
 *   <li>Métodos de conteo para estadísticas</li>
 *   <li>Soporte de paginación para historiales largos</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Consulta
 * @see ConsultaService
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /**
     * Busca consultas por paciente ordenadas por fecha descendente
     * @param pacienteId ID del paciente
     * @return Lista de consultas del paciente (más recientes primero)
     */
    @Query("SELECT c FROM Consulta c WHERE c.paciente.id = :pacienteId ORDER BY c.fecha DESC")
    List<Consulta> findByPacienteIdOrderByFechaDesc(@Param("pacienteId") Long pacienteId);

    /**
     * Busca consultas por profesional
     * @param profesionalId ID del profesional
     * @return Lista de consultas realizadas por el profesional
     */
    List<Consulta> findByProfesionalId(Long profesionalId);

    /**
     * Busca consultas entre fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de consultas en ese rango
     */
    List<Consulta> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca consultas del día
     * @param fechaInicio Inicio del día
     * @param fechaFin Fin del día
     * @return Lista de consultas del día
     */
    @Query("SELECT c FROM Consulta c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fecha DESC")
    List<Consulta> findConsultasDelDia(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca últimas N consultas de un paciente
     * @param pacienteId ID del paciente
     * @param pageable Configuración de paginación (límite)
     * @return Página con las últimas consultas
     */
    @Query("SELECT c FROM Consulta c WHERE c.paciente.id = :pacienteId ORDER BY c.fecha DESC")
    Page<Consulta> findUltimasConsultasPaciente(
        @Param("pacienteId") Long pacienteId,
        Pageable pageable
    );

    /**
     * Cuenta consultas por paciente
     * @param pacienteId ID del paciente
     * @return Cantidad de consultas del paciente
     */
    long countByPacienteId(Long pacienteId);

    /**
     * Cuenta consultas por profesional entre fechas
     * @param profesionalId ID del profesional
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Cantidad de consultas
     */
    @Query("SELECT COUNT(c) FROM Consulta c WHERE c.profesional.id = :profesionalId " +
           "AND c.fecha BETWEEN :fechaInicio AND :fechaFin")
    long countConsultasPorProfesionalEnRango(
        @Param("profesionalId") Long profesionalId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca consultas con prescripciones cargadas
     * @param pacienteId ID del paciente
     * @return Lista de consultas con prescripciones
     */
    @Query("SELECT DISTINCT c FROM Consulta c LEFT JOIN FETCH c.prescripciones " +
           "WHERE c.paciente.id = :pacienteId ORDER BY c.fecha DESC")
    List<Consulta> findConsultasConPrescripciones(@Param("pacienteId") Long pacienteId);

    /**
     * Estadísticas: cuenta consultas por día en un rango
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de conteos por día
     */
    @Query("SELECT FUNCTION('DATE', c.fecha) as dia, COUNT(c) as total " +
           "FROM Consulta c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY FUNCTION('DATE', c.fecha) ORDER BY dia")
    List<Object[]> countConsultasPorDia(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    /**
     * Busca consultas por paciente con paginación.
     * 
     * <p>Ideal para ver el historial médico completo de una mascota,
     * ordenado por fecha descendente (más recientes primero).</p>
     * 
     * @param pacienteId ID del paciente
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de consultas del paciente
     */
    Page<Consulta> findByPacienteId(Long pacienteId, Pageable pageable);
    
    /**
     * Busca consultas por profesional con paginación.
     * 
     * <p>Útil para ver todas las consultas realizadas por un veterinario
     * específico con soporte de paginación.</p>
     * 
     * @param profesionalId ID del profesional
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de consultas del profesional
     */
    Page<Consulta> findByProfesionalId(Long profesionalId, Pageable pageable);
    
    /**
     * Busca consultas por rango de fechas con paginación.
     * 
     * <p>Permite filtrar consultas en un período específico,
     * útil para reportes y estadísticas.</p>
     * 
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de consultas en ese rango
     */
    Page<Consulta> findByFechaBetween(
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin, 
        Pageable pageable);
    
    /**
     * Busca consultas por paciente y rango de fechas con paginación.
     * 
     * <p>Combina filtro por paciente y fechas para búsquedas más específicas,
     * por ejemplo: historial del último año de una mascota.</p>
     * 
     * @param pacienteId ID del paciente
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de consultas que cumplen los criterios
     */
    Page<Consulta> findByPacienteIdAndFechaBetween(
        Long pacienteId, 
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin, 
        Pageable pageable);
    
    /**
     * Busca consultas por profesional y rango de fechas con paginación.
     * 
     * <p>Permite ver las consultas de un veterinario en un período específico,
     * útil para evaluaciones de desempeño o estadísticas.</p>
     * 
     * @param profesionalId ID del profesional
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de consultas que cumplen los criterios
     */
    Page<Consulta> findByProfesionalIdAndFechaBetween(
        Long profesionalId, 
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin, 
        Pageable pageable);
}

