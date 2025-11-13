package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Propietario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Propietario
 * Proporciona métodos de acceso a datos para propietarios de mascotas
 */
@Repository
public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    /**
     * Busca propietario por documento
     * @param documento Documento del propietario
     * @return Optional con el propietario si existe
     */
    Optional<Propietario> findByDocumento(String documento);

    /**
     * Busca propietario por email
     * @param email Email del propietario
     * @return Optional con el propietario si existe
     */
    Optional<Propietario> findByEmail(String email);

    /**
     * Verifica si existe un propietario con el documento dado
     * @param documento Documento a verificar
     * @return true si existe, false si no
     */
    boolean existsByDocumento(String documento);

    /**
     * Busca propietarios por nombre (ignorando mayúsculas/minúsculas)
     * @param nombre Nombre o parte del nombre
     * @return Lista de propietarios que coinciden
     */
    List<Propietario> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca propietarios activos
     * @param activo Estado de actividad
     * @return Lista de propietarios activos
     */
    List<Propietario> findByActivo(Boolean activo);

    /**
     * Busca propietarios con paginación
     * @param nombre Nombre o parte del nombre
     * @param pageable Configuración de paginación
     * @return Página de propietarios
     */
    Page<Propietario> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Query personalizada: busca propietarios con sus pacientes
     * @param activo Estado de actividad
     * @return Lista de propietarios con pacientes cargados
     */
    @Query("SELECT DISTINCT p FROM Propietario p LEFT JOIN FETCH p.pacientes WHERE p.activo = :activo")
    List<Propietario> findPropietariosConPacientes(@Param("activo") Boolean activo);

    /**
     * Busca propietarios por teléfono
     * @param telefono Teléfono a buscar
     * @return Lista de propietarios con ese teléfono
     */
    List<Propietario> findByTelefonoContaining(String telefono);

    /**
     * Cuenta propietarios activos
     * @return Cantidad de propietarios activos
     */
    @Query("SELECT COUNT(p) FROM Propietario p WHERE p.activo = true")
    long countActivos();
    
    /**
     * Busca propietarios por documento con paginación.
     * 
     * <p>Busca coincidencias parciales en el documento de identidad.
     * Útil para búsquedas rápidas cuando se conoce el documento.</p>
     * 
     * @param documento Documento o parte del documento a buscar
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de propietarios que coinciden con el documento
     */
    Page<Propietario> findByDocumentoContaining(String documento, Pageable pageable);
    
    /**
     * Busca propietarios por teléfono con paginación.
     * 
     * <p>Busca coincidencias parciales en el número de teléfono.
     * Útil para búsquedas cuando el cliente proporciona su teléfono.</p>
     * 
     * @param telefono Teléfono o parte del teléfono a buscar
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de propietarios que coinciden con el teléfono
     */
    Page<Propietario> findByTelefonoContaining(String telefono, Pageable pageable);
    
    /**
     * Busca propietarios por nombre y documento con paginación.
     * 
     * <p>Permite búsqueda combinada por nombre y documento.
     * Útil para búsquedas más precisas cuando se tienen ambos datos.</p>
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @param documento Documento o parte del documento a buscar
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de propietarios que coinciden con ambos criterios
     */
    Page<Propietario> findByNombreContainingIgnoreCaseAndDocumentoContaining(
        String nombre, String documento, Pageable pageable);
    
    /**
     * Busca propietarios activos con paginación.
     * 
     * <p>Filtra propietarios según su estado activo/inactivo.
     * Útil para listar solo clientes activos o para auditorías.</p>
     * 
     * @param activo Estado de actividad a filtrar (true/false)
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de propietarios con el estado especificado
     */
    Page<Propietario> findByActivo(Boolean activo, Pageable pageable);
}

