package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de proveedores.
 * 
 * <p>Proporciona métodos de acceso a datos para proveedores,
 * incluyendo búsquedas por nombre, email y filtros por estado activo.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Busca un proveedor por su email.
     * 
     * @param email Email del proveedor
     * @return Proveedor encontrado o Optional vacío
     */
    Optional<Proveedor> findByEmail(String email);

    /**
     * Busca un proveedor por su RUC/NIT.
     * 
     * @param ruc RUC/NIT del proveedor
     * @return Proveedor encontrado o Optional vacío
     */
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Busca todos los proveedores activos, ordenados por nombre.
     * 
     * @return Lista de proveedores activos
     */
    List<Proveedor> findByActivoTrueOrderByNombreAsc();

    /**
     * Busca proveedores cuyo nombre contenga el texto proporcionado (case-insensitive).
     * 
     * @param nombre Texto a buscar en el nombre
     * @param activo Si es true, solo proveedores activos
     * @return Lista de proveedores que coinciden
     */
    List<Proveedor> findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc(String nombre, Boolean activo);

    /**
     * Verifica si existe un proveedor con el email proporcionado.
     * 
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un proveedor con el RUC/NIT proporcionado.
     * 
     * @param ruc RUC/NIT a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByRuc(String ruc);

    /**
     * Cuenta el número de movimientos de entrada asociados a un proveedor.
     * 
     * @param proveedorId ID del proveedor
     * @return Número de movimientos de entrada
     */
    @Query("SELECT COUNT(m) FROM MovimientoInventario m " +
           "WHERE m.proveedor.id = :proveedorId " +
           "AND m.tipo = 'ENTRADA'")
    long countMovimientosEntradaByProveedorId(@Param("proveedorId") Long proveedorId);
}

