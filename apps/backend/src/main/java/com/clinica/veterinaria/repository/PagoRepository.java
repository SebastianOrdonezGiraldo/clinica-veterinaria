package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByFacturaIdOrderByFechaPagoDesc(Long facturaId);
    
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal sumMontoByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                                 @Param("fechaFin") LocalDate fechaFin);
}

