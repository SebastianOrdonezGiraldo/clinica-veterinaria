package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.CategoriaProducto;
import com.clinica.veterinaria.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestión de productos del inventario.
 * 
 * <p>Proporciona métodos de acceso a datos para productos,
 * incluyendo búsquedas por código, nombre, categoría y filtros de stock.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su código único.
     * 
     * @param codigo Código del producto
     * @return Producto encontrado o Optional vacío
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Verifica si existe un producto con el código proporcionado.
     * 
     * @param codigo Código a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca todos los productos activos, ordenados por nombre.
     * 
     * @return Lista de productos activos
     */
    List<Producto> findByActivoTrueOrderByNombreAsc();

    /**
     * Busca productos por categoría, ordenados por nombre.
     * 
     * @param categoria Categoría del producto
     * @param activo Si es true, solo productos activos
     * @return Lista de productos de la categoría
     */
    List<Producto> findByCategoriaAndActivoOrderByNombreAsc(CategoriaProducto categoria, Boolean activo);

    /**
     * Busca productos cuyo nombre contenga el texto proporcionado (case-insensitive).
     * 
     * @param nombre Texto a buscar en el nombre
     * @param activo Si es true, solo productos activos
     * @return Lista de productos que coinciden
     */
    List<Producto> findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc(String nombre, Boolean activo);

    /**
     * Busca productos cuyo código contenga el texto proporcionado (case-insensitive).
     * 
     * @param codigo Texto a buscar en el código
     * @param activo Si es true, solo productos activos
     * @return Lista de productos que coinciden
     */
    List<Producto> findByCodigoContainingIgnoreCaseAndActivoOrderByNombreAsc(String codigo, Boolean activo);

    /**
     * Busca productos con stock bajo (stock actual menor o igual al stock mínimo).
     * 
     * @return Lista de productos con stock bajo
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND p.stockMinimo IS NOT NULL " +
           "AND p.stockActual <= p.stockMinimo " +
           "ORDER BY p.stockActual ASC")
    List<Producto> findProductosConStockBajo();

    /**
     * Busca productos con stock por debajo del mínimo especificado.
     * 
     * @param stockMinimo Umbral de stock mínimo
     * @return Lista de productos con stock bajo
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND p.stockActual <= :stockMinimo " +
           "ORDER BY p.stockActual ASC")
    List<Producto> findProductosConStockMenorA(@Param("stockMinimo") BigDecimal stockMinimo);

    /**
     * Busca productos con stock por encima del máximo especificado.
     * 
     * @param stockMaximo Umbral de stock máximo
     * @return Lista de productos con sobrestock
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND p.stockMaximo IS NOT NULL " +
           "AND p.stockActual > p.stockMaximo " +
           "ORDER BY p.stockActual DESC")
    List<Producto> findProductosConSobrestock();

    /**
     * Calcula el valor total del inventario (suma de costo * stockActual).
     * 
     * @return Valor total del inventario
     */
    @Query("SELECT COALESCE(SUM(p.costo * p.stockActual), 0) FROM Producto p WHERE p.activo = true")
    BigDecimal calcularValorTotalInventario();

    /**
     * Cuenta el número total de productos activos.
     * 
     * @return Número de productos activos
     */
    long countByActivoTrue();

    /**
     * Cuenta el número de productos con stock bajo.
     * 
     * @return Número de productos con stock bajo
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true " +
           "AND p.stockMinimo IS NOT NULL " +
           "AND p.stockActual <= p.stockMinimo")
    long countProductosConStockBajo();
}

