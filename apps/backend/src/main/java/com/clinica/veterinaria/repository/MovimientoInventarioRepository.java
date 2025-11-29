package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.MovimientoInventario;
import com.clinica.veterinaria.entity.Producto;
import com.clinica.veterinaria.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la gestión de movimientos de inventario.
 * 
 * <p>Proporciona métodos de acceso a datos para movimientos de inventario,
 * incluyendo búsquedas por producto, tipo, fecha y usuario.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Busca todos los movimientos de un producto, ordenados por fecha descendente.
     * 
     * @param producto Producto del cual buscar movimientos
     * @return Lista de movimientos del producto
     */
    List<MovimientoInventario> findByProductoOrderByFechaDesc(Producto producto);

    /**
     * Busca movimientos por tipo, ordenados por fecha descendente.
     * 
     * @param tipo Tipo de movimiento (ENTRADA, SALIDA, AJUSTE)
     * @return Lista de movimientos del tipo especificado
     */
    List<MovimientoInventario> findByTipoOrderByFechaDesc(MovimientoInventario.TipoMovimiento tipo);

    /**
     * Busca movimientos por usuario, ordenados por fecha descendente.
     * 
     * @param usuario Usuario que realizó los movimientos
     * @return Lista de movimientos del usuario
     */
    List<MovimientoInventario> findByUsuarioOrderByFechaDesc(Usuario usuario);

    /**
     * Busca movimientos de un producto en un rango de fechas.
     * 
     * @param producto Producto del cual buscar movimientos
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de movimientos en el rango de fechas
     */
    @Query("SELECT m FROM MovimientoInventario m " +
           "WHERE m.producto = :producto " +
           "AND m.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY m.fecha DESC")
    List<MovimientoInventario> findByProductoAndFechaBetween(
        @Param("producto") Producto producto,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca movimientos en un rango de fechas, ordenados por fecha descendente.
     * 
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de movimientos en el rango de fechas
     */
    @Query("SELECT m FROM MovimientoInventario m " +
           "WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY m.fecha DESC")
    List<MovimientoInventario> findByFechaBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca el último movimiento de un producto.
     * 
     * @param producto Producto del cual buscar el último movimiento
     * @return Último movimiento del producto o null
     */
    @Query("SELECT m FROM MovimientoInventario m " +
           "WHERE m.producto = :producto " +
           "ORDER BY m.fecha DESC LIMIT 1")
    MovimientoInventario findUltimoMovimientoByProducto(@Param("producto") Producto producto);

    /**
     * Calcula el total de entradas de un producto.
     * 
     * @param producto Producto del cual calcular entradas
     * @return Suma total de cantidades de entradas
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
           "WHERE m.producto = :producto AND m.tipo = 'ENTRADA'")
    java.math.BigDecimal calcularTotalEntradasByProducto(@Param("producto") Producto producto);

    /**
     * Calcula el total de salidas de un producto.
     * 
     * @param producto Producto del cual calcular salidas
     * @return Suma total de cantidades de salidas
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m " +
           "WHERE m.producto = :producto AND m.tipo = 'SALIDA'")
    java.math.BigDecimal calcularTotalSalidasByProducto(@Param("producto") Producto producto);

    /**
     * Cuenta el número de movimientos de un producto.
     * 
     * @param producto Producto del cual contar movimientos
     * @return Número de movimientos
     */
    long countByProducto(Producto producto);

    /**
     * Busca los productos más movidos (mayor número de movimientos) en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de arrays [Producto, cantidad de movimientos] ordenados por cantidad descendente
     */
    @Query(value = "SELECT m.producto_id, COUNT(m.id) as cantidad " +
           "FROM movimientos_inventario m " +
           "WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY m.producto_id " +
           "ORDER BY cantidad DESC " +
           "LIMIT 10", nativeQuery = true)
    List<Object[]> findProductosMasMovidos(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
}

