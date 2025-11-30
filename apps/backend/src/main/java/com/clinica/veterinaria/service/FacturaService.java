package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.FacturaDTO;
import com.clinica.veterinaria.dto.ItemFacturaDTO;
import com.clinica.veterinaria.dto.PagoDTO;
import com.clinica.veterinaria.entity.*;
import com.clinica.veterinaria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final PropietarioRepository propietarioRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Genera un número de factura único
     */
    private String generarNumeroFactura() {
        String prefijo = "FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Long ultimoNumero = facturaRepository.count();
        String numero = String.format("%04d", ultimoNumero + 1);
        return prefijo + "-" + numero;
    }

    public Page<FacturaDTO> findAll(Pageable pageable) {
        return facturaRepository.findAll(pageable)
            .map(this::toDTO);
    }

    public Page<FacturaDTO> findByPropietario(Long propietarioId, Pageable pageable) {
        return facturaRepository.findByPropietarioId(propietarioId, pageable)
            .map(this::toDTO);
    }

    public Page<FacturaDTO> findByEstado(Factura.EstadoFactura estado, Pageable pageable) {
        return facturaRepository.findByEstado(estado, pageable)
            .map(this::toDTO);
    }

    public Page<FacturaDTO> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        return facturaRepository.findByFechaEmisionBetween(fechaInicio, fechaFin, pageable)
            .map(this::toDTO);
    }

    public FacturaDTO findById(Long id) {
        Factura factura = facturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        return toDTO(factura);
    }

    public FacturaDTO findByNumeroFactura(String numeroFactura) {
        Factura factura = facturaRepository.findByNumeroFactura(numeroFactura)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        return toDTO(factura);
    }

    public FacturaDTO create(FacturaDTO dto) {
        Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado"));

        Consulta consulta = null;
        if (dto.getConsultaId() != null) {
            consulta = consultaRepository.findById(dto.getConsultaId())
                .orElse(null);
        }

        // Calcular totales
        BigDecimal subtotal = calcularSubtotal(dto.getItems());
        BigDecimal descuento = dto.getDescuento() != null ? dto.getDescuento() : BigDecimal.ZERO;
        BigDecimal impuesto = dto.getImpuesto() != null ? dto.getImpuesto() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(descuento).add(impuesto);

        Factura factura = Factura.builder()
            .numeroFactura(generarNumeroFactura())
            .fechaEmision(dto.getFechaEmision() != null ? dto.getFechaEmision() : LocalDate.now())
            .fechaVencimiento(dto.getFechaVencimiento())
            .subtotal(subtotal)
            .descuento(descuento)
            .impuesto(impuesto)
            .total(total)
            .montoPagado(BigDecimal.ZERO)
            .observaciones(dto.getObservaciones())
            .estado(Factura.EstadoFactura.PENDIENTE)
            .propietario(propietario)
            .consulta(consulta)
            .build();

        // Agregar items
        if (dto.getItems() != null) {
            for (int i = 0; i < dto.getItems().size(); i++) {
                ItemFacturaDTO itemDTO = dto.getItems().get(i);
                ItemFactura item = ItemFactura.builder()
                    .descripcion(itemDTO.getDescripcion())
                    .tipoItem(itemDTO.getTipoItem())
                    .codigoProducto(itemDTO.getCodigoProducto())
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .descuento(itemDTO.getDescuento() != null ? itemDTO.getDescuento() : BigDecimal.ZERO)
                    .orden(i)
                    .factura(factura)
                    .build();
                item.calcularSubtotal();
                factura.getItems().add(item);
            }
        }

        factura = facturaRepository.save(factura);
        log.info("Factura creada: {}", factura.getNumeroFactura());
        return toDTO(factura);
    }

    /**
     * Crea una factura desde una consulta
     */
    public FacturaDTO createFromConsulta(Long consultaId, List<ItemFacturaDTO> itemsAdicionales) {
        Consulta consulta = consultaRepository.findById(consultaId)
            .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));

        // Verificar si ya existe una factura para esta consulta
        facturaRepository.findByConsultaId(consultaId)
            .ifPresent(f -> {
                throw new RuntimeException("Ya existe una factura para esta consulta");
            });

        FacturaDTO dto = FacturaDTO.builder()
            .propietarioId(consulta.getPaciente().getPropietario().getId())
            .consultaId(consultaId)
            .fechaEmision(LocalDate.now())
            .items(new java.util.ArrayList<>())
            .build();

        // Agregar item de consulta
        ItemFacturaDTO itemConsulta = ItemFacturaDTO.builder()
            .descripcion("Consulta médica - " + consulta.getDiagnostico())
            .tipoItem("SERVICIO")
            .cantidad(BigDecimal.ONE)
            .precioUnitario(BigDecimal.valueOf(50000)) // Precio por defecto, debería ser configurable
            .orden(0)
            .build();
        dto.getItems().add(itemConsulta);

        // Agregar items adicionales
        if (itemsAdicionales != null) {
            dto.getItems().addAll(itemsAdicionales);
        }

        return create(dto);
    }

    public FacturaDTO update(Long id, FacturaDTO dto) {
        Factura factura = facturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        // Solo permitir actualizar ciertos campos si no está pagada
        if (factura.getEstado() != Factura.EstadoFactura.PAGADA) {
            if (dto.getObservaciones() != null) {
                factura.setObservaciones(dto.getObservaciones());
            }
            if (dto.getFechaVencimiento() != null) {
                factura.setFechaVencimiento(dto.getFechaVencimiento());
            }
        }

        factura = facturaRepository.save(factura);
        log.info("Factura actualizada: {}", id);
        return toDTO(factura);
    }

    public void cancel(Long id) {
        Factura factura = facturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        if (factura.getEstado() == Factura.EstadoFactura.PAGADA) {
            throw new RuntimeException("No se puede cancelar una factura pagada");
        }

        factura.setEstado(Factura.EstadoFactura.CANCELADA);
        facturaRepository.save(factura);
        log.info("Factura cancelada: {}", id);
    }

    /**
     * Registra un pago sobre una factura
     */
    public PagoDTO registrarPago(Long facturaId, PagoDTO pagoDTO, Long usuarioId) {
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        if (factura.getEstado() == Factura.EstadoFactura.CANCELADA) {
            throw new RuntimeException("No se puede registrar un pago en una factura cancelada");
        }

        if (factura.getEstado() == Factura.EstadoFactura.PAGADA) {
            throw new RuntimeException("La factura ya está pagada completamente");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Pago pago = Pago.builder()
            .monto(pagoDTO.getMonto())
            .fechaPago(pagoDTO.getFechaPago() != null ? pagoDTO.getFechaPago() : LocalDateTime.now())
            .metodoPago(pagoDTO.getMetodoPago())
            .referencia(pagoDTO.getReferencia())
            .observaciones(pagoDTO.getObservaciones())
            .factura(factura)
            .registradoPor(usuario)
            .build();

        pago = pagoRepository.save(pago);

        // Actualizar monto pagado y estado de la factura
        BigDecimal nuevoMontoPagado = factura.getMontoPagado().add(pagoDTO.getMonto());
        factura.setMontoPagado(nuevoMontoPagado);

        if (factura.isPagadaCompletamente()) {
            factura.setEstado(Factura.EstadoFactura.PAGADA);
        } else if (nuevoMontoPagado.compareTo(BigDecimal.ZERO) > 0) {
            factura.setEstado(Factura.EstadoFactura.PARCIAL);
        }

        facturaRepository.save(factura);
        log.info("Pago registrado: {} en factura {}", pagoDTO.getMonto(), facturaId);

        return toPagoDTO(pago);
    }

    /**
     * Tarea programada para marcar facturas vencidas
     */
    @Scheduled(cron = "0 0 1 * * *") // Diariamente a la 1:00 AM
    public void marcarFacturasVencidas() {
        List<Factura> facturas = facturaRepository.findAll();
        int contador = 0;

        for (Factura factura : facturas) {
            if (factura.isVencida() && factura.getEstado() != Factura.EstadoFactura.VENCIDA) {
                factura.setEstado(Factura.EstadoFactura.VENCIDA);
                facturaRepository.save(factura);
                contador++;
            }
        }

        if (contador > 0) {
            log.info("Facturas vencidas marcadas: {}", contador);
        }
    }

    /**
     * Obtiene estadísticas financieras
     */
    public java.util.Map<String, Object> getEstadisticasFinancieras(LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal totalFacturado = facturaRepository.sumTotalByFechaBetween(fechaInicio, fechaFin);
        BigDecimal totalPagado = facturaRepository.sumMontoPagadoByFechaBetween(fechaInicio, fechaFin);
        BigDecimal totalPendiente = totalFacturado.subtract(totalPagado);

        return java.util.Map.of(
            "totalFacturado", totalFacturado,
            "totalPagado", totalPagado,
            "totalPendiente", totalPendiente,
            "fechaInicio", fechaInicio,
            "fechaFin", fechaFin
        );
    }

    private BigDecimal calcularSubtotal(List<ItemFacturaDTO> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
            .map(item -> {
                BigDecimal subtotal = item.getCantidad().multiply(item.getPrecioUnitario());
                if (item.getDescuento() != null && item.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                    subtotal = subtotal.subtract(item.getDescuento());
                }
                return subtotal;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FacturaDTO toDTO(Factura factura) {
        List<PagoDTO> pagosDTO = factura.getPagos().stream()
            .map(this::toPagoDTO)
            .collect(Collectors.toList());

        return FacturaDTO.builder()
            .id(factura.getId())
            .numeroFactura(factura.getNumeroFactura())
            .fechaEmision(factura.getFechaEmision())
            .fechaVencimiento(factura.getFechaVencimiento())
            .subtotal(factura.getSubtotal())
            .descuento(factura.getDescuento())
            .impuesto(factura.getImpuesto())
            .total(factura.getTotal())
            .montoPagado(factura.getMontoPagado())
            .montoPendiente(factura.getMontoPendiente())
            .observaciones(factura.getObservaciones())
            .estado(factura.getEstado())
            .propietarioId(factura.getPropietario().getId())
            .propietarioNombre(factura.getPropietario().getNombre())
            .consultaId(factura.getConsulta() != null ? factura.getConsulta().getId() : null)
            .items(factura.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .map(this::toItemDTO)
                .collect(Collectors.toList()))
            .pagos(pagosDTO)
            .createdAt(factura.getCreatedAt())
            .updatedAt(factura.getUpdatedAt())
            .build();
    }

    private ItemFacturaDTO toItemDTO(ItemFactura item) {
        return ItemFacturaDTO.builder()
            .id(item.getId())
            .descripcion(item.getDescripcion())
            .tipoItem(item.getTipoItem())
            .codigoProducto(item.getCodigoProducto())
            .cantidad(item.getCantidad())
            .precioUnitario(item.getPrecioUnitario())
            .descuento(item.getDescuento())
            .subtotal(item.getSubtotal())
            .orden(item.getOrden())
            .build();
    }

    private PagoDTO toPagoDTO(Pago pago) {
        return PagoDTO.builder()
            .id(pago.getId())
            .monto(pago.getMonto())
            .fechaPago(pago.getFechaPago())
            .metodoPago(pago.getMetodoPago())
            .referencia(pago.getReferencia())
            .observaciones(pago.getObservaciones())
            .facturaId(pago.getFactura().getId())
            .usuarioId(pago.getRegistradoPor() != null ? pago.getRegistradoPor().getId() : null)
            .usuarioNombre(pago.getRegistradoPor() != null ? pago.getRegistradoPor().getNombre() : null)
            .createdAt(pago.getCreatedAt())
            .build();
    }
}

