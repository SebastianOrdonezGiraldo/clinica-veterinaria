package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.FacturaDTO;
import com.clinica.veterinaria.dto.ItemFacturaDTO;
import com.clinica.veterinaria.dto.PagoDTO;
import com.clinica.veterinaria.entity.Factura;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<Page<FacturaDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(facturaService.findAll(pageable));
    }

    @GetMapping("/propietario/{propietarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<Page<FacturaDTO>> getByPropietario(
            @PathVariable Long propietarioId,
            Pageable pageable) {
        return ResponseEntity.ok(facturaService.findByPropietario(propietarioId, pageable));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<Page<FacturaDTO>> getByEstado(
            @PathVariable Factura.EstadoFactura estado,
            Pageable pageable) {
        return ResponseEntity.ok(facturaService.findByEstado(estado, pageable));
    }

    @GetMapping("/fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<Page<FacturaDTO>> getByFechaBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Pageable pageable) {
        return ResponseEntity.ok(facturaService.findByFechaBetween(fechaInicio, fechaFin, pageable));
    }

    @GetMapping("/numero/{numeroFactura}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<FacturaDTO> getByNumeroFactura(@PathVariable String numeroFactura) {
        return ResponseEntity.ok(facturaService.findByNumeroFactura(numeroFactura));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<FacturaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<FacturaDTO> create(@Valid @RequestBody FacturaDTO dto) {
        FacturaDTO created = facturaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/desde-consulta/{consultaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<FacturaDTO> createFromConsulta(
            @PathVariable Long consultaId,
            @RequestBody(required = false) List<ItemFacturaDTO> itemsAdicionales) {
        FacturaDTO created = facturaService.createFromConsulta(consultaId, itemsAdicionales);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<FacturaDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FacturaDTO dto) {
        return ResponseEntity.ok(facturaService.update(id, dto));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        facturaService.cancel(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<PagoDTO> registrarPago(
            @PathVariable Long id,
            @Valid @RequestBody PagoDTO pagoDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long usuarioId = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
            .getId();
        
        PagoDTO pago = facturaService.registrarPago(id, pagoDTO, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<Map<String, Object>> getEstadisticas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(facturaService.getEstadisticasFinancieras(fechaInicio, fechaFin));
    }
}

