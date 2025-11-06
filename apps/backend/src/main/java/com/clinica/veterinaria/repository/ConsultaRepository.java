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
 * Repositorio para la entidad Consulta
 * Proporciona métodos de acceso a datos para consultas médicas (historia clínica)
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
}

