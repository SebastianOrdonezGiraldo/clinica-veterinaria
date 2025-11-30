package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    
    Page<Factura> findByPropietarioId(Long propietarioId, Pageable pageable);
    
    Page<Factura> findByEstado(Factura.EstadoFactura estado, Pageable pageable);
    
    Page<Factura> findByFechaEmisionBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);
    
    @Query("SELECT f FROM Factura f WHERE f.propietario.id = :propietarioId AND f.estado = :estado")
    List<Factura> findByPropietarioIdAndEstado(@Param("propietarioId") Long propietarioId, 
                                                 @Param("estado") Factura.EstadoFactura estado);
    
    @Query("SELECT f FROM Factura f WHERE f.consulta.id = :consultaId")
    Optional<Factura> findByConsultaId(@Param("consultaId") Long consultaId);
    
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE f.fechaEmision BETWEEN :fechaInicio AND :fechaFin AND f.estado != 'CANCELADA'")
    BigDecimal sumTotalByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                      @Param("fechaFin") LocalDate fechaFin);
    
    @Query("SELECT COALESCE(SUM(f.montoPagado), 0) FROM Factura f WHERE f.fechaEmision BETWEEN :fechaInicio AND :fechaFin AND f.estado != 'CANCELADA'")
    BigDecimal sumMontoPagadoByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                             @Param("fechaFin") LocalDate fechaFin);
}

