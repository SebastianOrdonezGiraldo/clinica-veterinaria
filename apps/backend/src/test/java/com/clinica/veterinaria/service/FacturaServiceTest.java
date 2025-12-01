package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.FacturaDTO;
import com.clinica.veterinaria.dto.ItemFacturaDTO;
import com.clinica.veterinaria.dto.PagoDTO;
import com.clinica.veterinaria.entity.*;
import com.clinica.veterinaria.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FacturaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de FacturaService")
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Propietario propietario;
    private Paciente paciente;
    private Usuario veterinario;
    private Consulta consulta;
    private Factura factura;
    private ItemFactura itemFactura;

    @BeforeEach
    void setUp() {
        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .telefono("1234567890")
            .activo(true)
            .build();

        paciente = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Canino")
            .propietario(propietario)
            .activo(true)
            .build();

        veterinario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        consulta = Consulta.builder()
            .id(1L)
            .paciente(paciente)
            .profesional(veterinario)
            .fecha(LocalDateTime.now())
            .diagnostico("Consulta general")
            .build();

        itemFactura = ItemFactura.builder()
            .id(1L)
            .descripcion("Consulta médica")
            .tipoItem("SERVICIO")
            .cantidad(BigDecimal.ONE)
            .precioUnitario(BigDecimal.valueOf(50000))
            .descuento(BigDecimal.ZERO)
            .subtotal(BigDecimal.valueOf(50000))
            .orden(0)
            .build();

        factura = Factura.builder()
            .id(1L)
            .numeroFactura("FAC-202411-0001")
            .fechaEmision(LocalDate.now())
            .subtotal(BigDecimal.valueOf(50000))
            .descuento(BigDecimal.ZERO)
            .impuesto(BigDecimal.ZERO)
            .total(BigDecimal.valueOf(50000))
            .montoPagado(BigDecimal.ZERO)
            .estado(Factura.EstadoFactura.PENDIENTE)
            .propietario(propietario)
            .consulta(consulta)
            .items(new ArrayList<>())
            .pagos(new ArrayList<>())
            .build();

        factura.getItems().add(itemFactura);
        itemFactura.setFactura(factura);
    }

    @Test
    @DisplayName("Debe crear una factura correctamente")
    void testCreate() {
        // Arrange
        FacturaDTO dto = FacturaDTO.builder()
            .propietarioId(1L)
            .fechaEmision(LocalDate.now())
            .subtotal(BigDecimal.valueOf(50000))
            .total(BigDecimal.valueOf(50000))
            .items(List.of(
                ItemFacturaDTO.builder()
                    .descripcion("Consulta médica")
                    .cantidad(BigDecimal.ONE)
                    .precioUnitario(BigDecimal.valueOf(50000))
                    .build()
            ))
            .build();

        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(facturaRepository.count()).thenReturn(0L);
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        FacturaDTO result = facturaService.create(dto);

        // Assert
        assertNotNull(result);
        verify(propietarioRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el propietario no existe")
    void testCreate_PropietarioNoExiste() {
        // Arrange
        FacturaDTO dto = FacturaDTO.builder()
            .propietarioId(999L)
            .fechaEmision(LocalDate.now())
            .items(new ArrayList<>())
            .build();

        when(propietarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> facturaService.create(dto));
        verify(propietarioRepository, times(1)).findById(999L);
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("Debe crear factura desde consulta")
    void testCreateFromConsulta() {
        // Arrange
        ItemFacturaDTO itemAdicional = ItemFacturaDTO.builder()
            .descripcion("Medicamento")
            .cantidad(BigDecimal.ONE)
            .precioUnitario(BigDecimal.valueOf(20000))
            .build();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(facturaRepository.findByConsultaId(1L)).thenReturn(Optional.empty());
        when(facturaRepository.count()).thenReturn(0L);
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        FacturaDTO result = facturaService.createFromConsulta(1L, List.of(itemAdicional));

        // Assert
        assertNotNull(result);
        verify(consultaRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).findByConsultaId(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si ya existe factura para la consulta")
    void testCreateFromConsulta_FacturaYaExiste() {
        // Arrange
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(facturaRepository.findByConsultaId(1L)).thenReturn(Optional.of(factura));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> facturaService.createFromConsulta(1L, null));
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("Debe registrar un pago correctamente")
    void testRegistrarPago() {
        // Arrange
        PagoDTO pagoDTO = PagoDTO.builder()
            .monto(BigDecimal.valueOf(30000))
            .fechaPago(LocalDateTime.now())
            .metodoPago("EFECTIVO")
            .build();

        Pago pago = Pago.builder()
            .id(1L)
            .monto(BigDecimal.valueOf(30000))
            .fechaPago(LocalDateTime.now())
            .metodoPago("EFECTIVO")
            .factura(factura)
            .registradoPor(veterinario)
            .build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        PagoDTO result = facturaService.registrarPago(1L, pagoDTO, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(30000), result.getMonto());
        verify(facturaRepository, times(1)).findById(1L);
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("Debe actualizar estado a PAGADA cuando el pago completa el total")
    void testRegistrarPago_CompletaTotal() {
        // Arrange
        PagoDTO pagoDTO = PagoDTO.builder()
            .monto(BigDecimal.valueOf(50000))
            .fechaPago(LocalDateTime.now())
            .metodoPago("EFECTIVO")
            .build();

        Pago pago = Pago.builder()
            .id(1L)
            .monto(BigDecimal.valueOf(50000))
            .fechaPago(LocalDateTime.now())
            .factura(factura)
            .registradoPor(veterinario)
            .build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        
        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        facturaService.registrarPago(1L, pagoDTO, 1L);

        // Assert
        verify(facturaRepository, times(1)).save(facturaCaptor.capture());
        Factura facturaActualizada = facturaCaptor.getValue();
        assertEquals(Factura.EstadoFactura.PAGADA, facturaActualizada.getEstado());
        assertEquals(BigDecimal.valueOf(50000), facturaActualizada.getMontoPagado());
    }

    @Test
    @DisplayName("Debe actualizar estado a PARCIAL cuando el pago es parcial")
    void testRegistrarPago_PagoParcial() {
        // Arrange
        PagoDTO pagoDTO = PagoDTO.builder()
            .monto(BigDecimal.valueOf(20000))
            .fechaPago(LocalDateTime.now())
            .metodoPago("EFECTIVO")
            .build();

        Pago pago = Pago.builder()
            .id(1L)
            .monto(BigDecimal.valueOf(20000))
            .fechaPago(LocalDateTime.now())
            .factura(factura)
            .registradoPor(veterinario)
            .build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        
        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        facturaService.registrarPago(1L, pagoDTO, 1L);

        // Assert
        verify(facturaRepository, times(1)).save(facturaCaptor.capture());
        Factura facturaActualizada = facturaCaptor.getValue();
        assertEquals(Factura.EstadoFactura.PARCIAL, facturaActualizada.getEstado());
        assertEquals(BigDecimal.valueOf(20000), facturaActualizada.getMontoPagado());
    }

    @Test
    @DisplayName("Debe lanzar excepción al registrar pago en factura cancelada")
    void testRegistrarPago_FacturaCancelada() {
        // Arrange
        factura.setEstado(Factura.EstadoFactura.CANCELADA);
        PagoDTO pagoDTO = PagoDTO.builder()
            .monto(BigDecimal.valueOf(10000))
            .fechaPago(LocalDateTime.now())
            .build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> facturaService.registrarPago(1L, pagoDTO, 1L));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debe cancelar una factura correctamente")
    void testCancel() {
        // Arrange
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        facturaService.cancel(1L);

        // Assert
        ArgumentCaptor<Factura> facturaCaptor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository, times(1)).save(facturaCaptor.capture());
        assertEquals(Factura.EstadoFactura.CANCELADA, facturaCaptor.getValue().getEstado());
    }

    @Test
    @DisplayName("Debe lanzar excepción al cancelar factura pagada")
    void testCancel_FacturaPagada() {
        // Arrange
        factura.setEstado(Factura.EstadoFactura.PAGADA);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> facturaService.cancel(1L));
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("Debe obtener estadísticas financieras correctamente")
    void testGetEstadisticasFinancieras() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        LocalDate fechaFin = LocalDate.now();
        
        when(facturaRepository.sumTotalByFechaBetween(fechaInicio, fechaFin))
            .thenReturn(BigDecimal.valueOf(1000000));
        when(facturaRepository.sumMontoPagadoByFechaBetween(fechaInicio, fechaFin))
            .thenReturn(BigDecimal.valueOf(800000));

        // Act
        var estadisticas = facturaService.getEstadisticasFinancieras(fechaInicio, fechaFin);

        // Assert
        assertNotNull(estadisticas);
        assertEquals(BigDecimal.valueOf(1000000), estadisticas.get("totalFacturado"));
        assertEquals(BigDecimal.valueOf(800000), estadisticas.get("totalPagado"));
        assertEquals(BigDecimal.valueOf(200000), estadisticas.get("totalPendiente"));
    }

    @Test
    @DisplayName("Debe buscar facturas por propietario")
    void testFindByPropietario() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Factura> page = new PageImpl<>(List.of(factura), pageable, 1);
        
        when(facturaRepository.findByPropietarioId(1L, pageable)).thenReturn(page);

        // Act
        Page<FacturaDTO> result = facturaService.findByPropietario(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(facturaRepository, times(1)).findByPropietarioId(1L, pageable);
    }

    @Test
    @DisplayName("Debe buscar facturas por estado")
    void testFindByEstado() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Factura> page = new PageImpl<>(List.of(factura), pageable, 1);
        
        when(facturaRepository.findByEstado(Factura.EstadoFactura.PENDIENTE, pageable)).thenReturn(page);

        // Act
        Page<FacturaDTO> result = facturaService.findByEstado(Factura.EstadoFactura.PENDIENTE, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(facturaRepository, times(1)).findByEstado(Factura.EstadoFactura.PENDIENTE, pageable);
    }
}

