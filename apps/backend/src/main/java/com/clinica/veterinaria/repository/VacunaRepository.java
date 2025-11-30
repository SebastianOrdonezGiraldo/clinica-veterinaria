package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Vacuna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Vacuna}.
 * 
 * <p>Proporciona métodos de acceso a datos para tipos de vacunas disponibles.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see Vacuna
 */
@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    /**
     * Busca vacunas por nombre (ignorando mayúsculas/minúsculas)
     */
    List<Vacuna> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca vacunas por especie
     */
    List<Vacuna> findByEspecie(String especie);

    /**
     * Busca vacunas activas
     */
    List<Vacuna> findByActivo(Boolean activo);

    /**
     * Busca vacunas activas por especie
     */
    List<Vacuna> findByEspecieAndActivo(String especie, Boolean activo);

    /**
     * Busca vacunas con paginación
     */
    Page<Vacuna> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Busca vacunas por especie con paginación
     */
    Page<Vacuna> findByEspecie(String especie, Pageable pageable);

    /**
     * Busca vacunas activas con paginación
     */
    Page<Vacuna> findByActivo(Boolean activo, Pageable pageable);

    /**
     * Busca vacunas por nombre y especie con paginación
     */
    Page<Vacuna> findByNombreContainingIgnoreCaseAndEspecie(String nombre, String especie, Pageable pageable);

    /**
     * Query personalizada: busca vacunas con filtros múltiples
     */
    @Query("SELECT v FROM Vacuna v WHERE " +
           "(:nombre IS NULL OR LOWER(v.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:especie IS NULL OR v.especie = :especie OR v.especie IS NULL) AND " +
           "v.activo = :activo")
    Page<Vacuna> findByFilters(
        @Param("nombre") String nombre,
        @Param("especie") String especie,
        @Param("activo") Boolean activo,
        Pageable pageable
    );
}

