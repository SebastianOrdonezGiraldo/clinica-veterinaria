package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.TemplateConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateConsultaRepository extends JpaRepository<TemplateConsulta, Long> {
    List<TemplateConsulta> findByActivoTrueOrderByCategoriaAscNombreAsc();
    List<TemplateConsulta> findByCategoriaAndActivoTrueOrderByNombreAsc(String categoria);
    List<TemplateConsulta> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
}

