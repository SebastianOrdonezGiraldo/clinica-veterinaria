package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.ItemPrescripcion;
import com.clinica.veterinaria.entity.ItemPrescripcion.ViaAdministracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad ItemPrescripcion
 * Proporciona métodos de acceso a datos para items de prescripciones (medicamentos)
 */
@Repository
public interface ItemPrescripcionRepository extends JpaRepository<ItemPrescripcion, Long> {

    /**
     * Busca items por prescripción
     * @param prescripcionId ID de la prescripción
     * @return Lista de items de esa prescripción
     */
    List<ItemPrescripcion> findByPrescripcionId(Long prescripcionId);

    /**
     * Busca items por medicamento (búsqueda parcial)
     * @param medicamento Nombre del medicamento
     * @return Lista de items con ese medicamento
     */
    List<ItemPrescripcion> findByMedicamentoContainingIgnoreCase(String medicamento);

    /**
     * Busca items por vía de administración
     * @param via Vía de administración
     * @return Lista de items con esa vía
     */
    List<ItemPrescripcion> findByViaAdministracion(ViaAdministracion via);

    /**
     * Busca medicamentos más recetados
     * @param limite Cantidad de resultados
     * @return Lista de medicamentos con su frecuencia
     */
    @Query("SELECT i.medicamento, COUNT(i) as cantidad FROM ItemPrescripcion i " +
           "GROUP BY i.medicamento ORDER BY cantidad DESC LIMIT :limite")
    List<Object[]> findMedicamentosMasRecetados(@Param("limite") int limite);

    /**
     * Busca items de prescripciones de un paciente
     * @param pacienteId ID del paciente
     * @return Lista de items recetados al paciente
     */
    @Query("SELECT i FROM ItemPrescripcion i JOIN i.prescripcion p JOIN p.consulta c " +
           "WHERE c.paciente.id = :pacienteId ORDER BY p.fechaEmision DESC")
    List<ItemPrescripcion> findByPacienteId(@Param("pacienteId") Long pacienteId);

    /**
     * Cuenta items por prescripción
     * @param prescripcionId ID de la prescripción
     * @return Cantidad de items
     */
    long countByPrescripcionId(Long prescripcionId);
}

