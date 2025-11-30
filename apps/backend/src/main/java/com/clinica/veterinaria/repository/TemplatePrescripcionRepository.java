package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.TemplatePrescripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplatePrescripcionRepository extends JpaRepository<TemplatePrescripcion, Long> {
    List<TemplatePrescripcion> findByActivoTrueOrderByCategoriaAscNombreAsc();
    List<TemplatePrescripcion> findByCategoriaAndActivoTrueOrderByNombreAsc(String categoria);
    List<TemplatePrescripcion> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    
    @Query("SELECT DISTINCT t.categoria FROM TemplatePrescripcion t WHERE t.activo = true ORDER BY t.categoria")
    List<String> findDistinctCategorias();
}

