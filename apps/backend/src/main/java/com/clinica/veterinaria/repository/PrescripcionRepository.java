package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Prescripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Prescripcion
 * Proporciona métodos de acceso a datos para prescripciones médicas
 */
@Repository
public interface PrescripcionRepository extends JpaRepository<Prescripcion, Long> {

    /**
     * Busca prescripciones por consulta
     * @param consultaId ID de la consulta
     * @return Lista de prescripciones de esa consulta
     */
    List<Prescripcion> findByConsultaId(Long consultaId);

    /**
     * Busca prescripciones por paciente (a través de consulta)
     * @param pacienteId ID del paciente
     * @return Lista de prescripciones del paciente
     */
    @Query("SELECT p FROM Prescripcion p JOIN p.consulta c WHERE c.paciente.id = :pacienteId " +
           "ORDER BY p.fechaEmision DESC")
    List<Prescripcion> findByPacienteId(@Param("pacienteId") Long pacienteId);

    /**
     * Busca prescripciones entre fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de prescripciones en ese rango
     */
    List<Prescripcion> findByFechaEmisionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca prescripciones con items cargados
     * @param consultaId ID de la consulta
     * @return Lista de prescripciones con items
     */
    @Query("SELECT DISTINCT p FROM Prescripcion p LEFT JOIN FETCH p.items " +
           "WHERE p.consulta.id = :consultaId")
    List<Prescripcion> findPrescripcionesConItems(@Param("consultaId") Long consultaId);

    /**
     * Busca última prescripción de una consulta
     * @param consultaId ID de la consulta
     * @return Última prescripción emitida
     */
    @Query("SELECT p FROM Prescripcion p WHERE p.consulta.id = :consultaId " +
           "ORDER BY p.fechaEmision DESC LIMIT 1")
    Prescripcion findUltimaPrescripcionDeConsulta(@Param("consultaId") Long consultaId);

    /**
     * Cuenta prescripciones por paciente
     * @param pacienteId ID del paciente
     * @return Cantidad de prescripciones
     */
    @Query("SELECT COUNT(p) FROM Prescripcion p JOIN p.consulta c WHERE c.paciente.id = :pacienteId")
    long countByPacienteId(@Param("pacienteId") Long pacienteId);
    
    // Métodos de paginación
    Page<Prescripcion> findByConsultaId(Long consultaId, Pageable pageable);
    
    @Query("SELECT p FROM Prescripcion p JOIN p.consulta c WHERE c.paciente.id = :pacienteId ORDER BY p.fechaEmision DESC")
    Page<Prescripcion> findByPacienteId(@Param("pacienteId") Long pacienteId, Pageable pageable);
    
    Page<Prescripcion> findByFechaEmisionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    @Query("SELECT p FROM Prescripcion p JOIN p.consulta c WHERE c.paciente.id = :pacienteId " +
           "AND p.fechaEmision BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fechaEmision DESC")
    Page<Prescripcion> findByPacienteIdAndFechaEmisionBetween(
        @Param("pacienteId") Long pacienteId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable);
}

