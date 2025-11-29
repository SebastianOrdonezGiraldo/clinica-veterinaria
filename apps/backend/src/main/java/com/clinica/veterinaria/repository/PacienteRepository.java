package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Paciente}.
 * 
 * <p>Proporciona métodos de acceso a datos para pacientes (mascotas), incluyendo
 * búsquedas por propietario, nombre, especie y estado. Incluye métodos
 * personalizados con queries JPQL para optimizar consultas con relaciones.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Búsquedas por propietario (todas las mascotas de un cliente)</li>
 *   <li>Búsqueda por nombre (case-insensitive, parcial)</li>
 *   <li>Filtros por especie y estado activo</li>
 *   <li>Consultas con propietario cargado (JOIN FETCH)</li>
 *   <li>Soporte de paginación y filtros múltiples</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Paciente
 * @see PacienteService
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    /**
     * Busca pacientes por propietario
     * @param propietarioId ID del propietario
     * @return Lista de pacientes del propietario
     */
    List<Paciente> findByPropietarioId(Long propietarioId);

    /**
     * Busca pacientes por nombre (ignorando mayúsculas/minúsculas)
     * @param nombre Nombre o parte del nombre
     * @return Lista de pacientes que coinciden
     */
    List<Paciente> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca pacientes por especie
     * @param especie Especie a buscar
     * @return Lista de pacientes de esa especie
     */
    List<Paciente> findByEspecie(String especie);

    /**
     * Busca pacientes por especie y activos
     * @param especie Especie a buscar
     * @param activo Estado de actividad
     * @return Lista de pacientes activos de esa especie
     */
    List<Paciente> findByEspecieAndActivo(String especie, Boolean activo);

    /**
     * Busca pacientes activos
     * @param activo Estado de actividad
     * @return Lista de pacientes activos
     */
    List<Paciente> findByActivo(Boolean activo);

    /**
     * Busca pacientes con paginación
     * @param nombre Nombre o parte del nombre
     * @param pageable Configuración de paginación
     * @return Página de pacientes
     */
    Page<Paciente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    /**
     * Busca pacientes por especie con paginación
     * @param especie Especie a buscar
     * @param pageable Configuración de paginación
     * @return Página de pacientes de esa especie
     */
    Page<Paciente> findByEspecie(String especie, Pageable pageable);
    
    /**
     * Busca pacientes por nombre y especie con paginación
     * @param nombre Nombre o parte del nombre
     * @param especie Especie a buscar
     * @param pageable Configuración de paginación
     * @return Página de pacientes que coinciden con ambos criterios
     */
    Page<Paciente> findByNombreContainingIgnoreCaseAndEspecie(String nombre, String especie, Pageable pageable);

    /**
     * Query personalizada: busca pacientes con propietario cargado
     * @param activo Estado de actividad
     * @return Lista de pacientes con propietario cargado
     */
    @Query("SELECT p FROM Paciente p JOIN FETCH p.propietario WHERE p.activo = :activo")
    List<Paciente> findPacientesConPropietario(@Param("activo") Boolean activo);

    /**
     * Busca pacientes por especie y propietario
     * @param especie Especie
     * @param propietarioId ID del propietario
     * @return Lista de pacientes
     */
    List<Paciente> findByEspecieAndPropietarioId(String especie, Long propietarioId);

    /**
     * Cuenta pacientes por especie
     * @param especie Especie a contar
     * @return Cantidad de pacientes de esa especie
     */
    long countByEspecie(String especie);

    /**
     * Cuenta pacientes activos
     * @return Cantidad de pacientes activos
     */
    @Query("SELECT COUNT(p) FROM Paciente p WHERE p.activo = true")
    long countActivos();

    /**
     * Busca pacientes por múltiples criterios
     * @param especie Especie (puede ser null)
     * @param activo Estado activo
     * @param pageable Configuración de paginación
     * @return Página de pacientes
     */
    @Query("SELECT p FROM Paciente p WHERE " +
           "(:especie IS NULL OR p.especie = :especie) AND " +
           "p.activo = :activo")
    Page<Paciente> findByFilters(
        @Param("especie") String especie,
        @Param("activo") Boolean activo,
        Pageable pageable
    );
}

