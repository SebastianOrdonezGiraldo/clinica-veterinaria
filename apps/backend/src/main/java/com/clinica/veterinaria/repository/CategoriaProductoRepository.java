package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de categorías de productos.
 * 
 * <p>Proporciona métodos de acceso a datos para categorías de productos,
 * incluyendo búsquedas por nombre y filtros por estado activo.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {

    /**
     * Busca una categoría por su nombre (case-insensitive).
     * 
     * @param nombre Nombre de la categoría a buscar
     * @return Categoría encontrada o Optional vacío
     */
    Optional<CategoriaProducto> findByNombreIgnoreCase(String nombre);

    /**
     * Busca todas las categorías activas, ordenadas por nombre.
     * 
     * @return Lista de categorías activas
     */
    List<CategoriaProducto> findByActivoTrueOrderByNombreAsc();

    /**
     * Busca categorías cuyo nombre contenga el texto proporcionado (case-insensitive).
     * Útil para búsquedas parciales.
     * 
     * @param nombre Texto a buscar en el nombre
     * @return Lista de categorías que coinciden
     */
    List<CategoriaProducto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Verifica si existe una categoría con el nombre proporcionado (case-insensitive).
     * 
     * @param nombre Nombre a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Cuenta el número de productos asociados a una categoría.
     * 
     * @param categoriaId ID de la categoría
     * @return Número de productos asociados
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.activo = true")
    long countProductosActivosByCategoriaId(@Param("categoriaId") Long categoriaId);
}

