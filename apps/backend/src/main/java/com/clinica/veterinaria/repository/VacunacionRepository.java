package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Vacunacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Vacunacion}.
 * 
 * <p>Proporciona métodos de acceso a datos para registros de vacunaciones aplicadas.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see Vacunacion
 */
@Repository
public interface VacunacionRepository extends JpaRepository<Vacunacion, Long> {

    /**
     * Busca vacunaciones por paciente
     */
    List<Vacunacion> findByPacienteId(Long pacienteId);

    /**
     * Busca vacunaciones por vacuna
     */
    List<Vacunacion> findByVacunaId(Long vacunaId);

    /**
     * Busca vacunaciones por profesional
     */
    List<Vacunacion> findByProfesionalId(Long profesionalId);

    /**
     * Busca vacunaciones por paciente con paginación
     */
    Page<Vacunacion> findByPacienteId(Long pacienteId, Pageable pageable);

    /**
     * Busca vacunaciones por paciente y vacuna
     */
    List<Vacunacion> findByPacienteIdAndVacunaId(Long pacienteId, Long vacunaId);

    /**
     * Busca la última vacunación de un paciente para una vacuna específica
     */
    Optional<Vacunacion> findFirstByPacienteIdAndVacunaIdOrderByFechaAplicacionDesc(Long pacienteId, Long vacunaId);

    /**
     * Busca vacunaciones próximas a vencer (próxima dosis dentro de un rango)
     */
    @Query("SELECT v FROM Vacunacion v WHERE " +
           "v.proximaDosis IS NOT NULL AND " +
           "v.proximaDosis BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY v.proximaDosis ASC")
    List<Vacunacion> findProximasAVencer(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Busca vacunaciones vencidas (próxima dosis pasada)
     */
    @Query("SELECT v FROM Vacunacion v WHERE " +
           "v.proximaDosis IS NOT NULL AND " +
           "v.proximaDosis < :fechaActual " +
           "ORDER BY v.proximaDosis ASC")
    List<Vacunacion> findVencidas(@Param("fechaActual") LocalDate fechaActual);

    /**
     * Busca vacunaciones por paciente con paginación ordenadas por fecha descendente
     */
    Page<Vacunacion> findByPacienteIdOrderByFechaAplicacionDesc(Long pacienteId, Pageable pageable);

    /**
     * Cuenta vacunaciones por paciente
     */
    long countByPacienteId(Long pacienteId);

    /**
     * Cuenta vacunaciones por vacuna
     */
    long countByVacunaId(Long vacunaId);
}

