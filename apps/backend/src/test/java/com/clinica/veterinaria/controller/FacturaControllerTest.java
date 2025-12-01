package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.FacturaDTO;
import com.clinica.veterinaria.dto.ItemFacturaDTO;
import com.clinica.veterinaria.dto.PagoDTO;
import com.clinica.veterinaria.entity.Factura;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.service.FacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FacturaController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de FacturaController")
class FacturaControllerTest {

    @Mock
    private FacturaService facturaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FacturaController facturaController;

    private FacturaDTO facturaDTO;
    private Usuario usuario;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);

        usuario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Test")
            .email("test@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        facturaDTO = FacturaDTO.builder()
            .id(1L)
            .numeroFactura("FAC-202411-0001")
            .fechaEmision(LocalDate.now())
            .subtotal(BigDecimal.valueOf(50000))
            .total(BigDecimal.valueOf(50000))
            .montoPagado(BigDecimal.ZERO)
            .montoPendiente(BigDecimal.valueOf(50000))
            .estado(Factura.EstadoFactura.PENDIENTE)
            .propietarioId(1L)
            .propietarioNombre("Juan Pérez")
            .items(List.of(
                ItemFacturaDTO.builder()
                    .descripcion("Consulta médica")
                    .cantidad(BigDecimal.ONE)
                    .precioUnitario(BigDecimal.valueOf(50000))
                    .build()
            ))
            .build();

        // Configurar SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@clinica.com");
    }

    @Test
    @DisplayName("Debe obtener todas las facturas")
    void testGetAll() {
        // Arrange
        Page<FacturaDTO> page = new PageImpl<>(List.of(facturaDTO), pageable, 1);
        when(facturaService.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<FacturaDTO>> response = facturaController.getAll(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(facturaService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener factura por ID")
    void testGetById() {
        // Arrange
        when(facturaService.findById(1L)).thenReturn(facturaDTO);

        // Act
        ResponseEntity<FacturaDTO> response = facturaController.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FAC-202411-0001", response.getBody().getNumeroFactura());
        verify(facturaService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe crear una factura")
    void testCreate() {
        // Arrange
        FacturaDTO createDTO = FacturaDTO.builder()
            .propietarioId(1L)
            .fechaEmision(LocalDate.now())
            .items(List.of(
                ItemFacturaDTO.builder()
                    .descripcion("Consulta médica")
                    .cantidad(BigDecimal.ONE)
                    .precioUnitario(BigDecimal.valueOf(50000))
                    .build()
            ))
            .build();

        when(facturaService.create(any(FacturaDTO.class))).thenReturn(facturaDTO);

        // Act
        ResponseEntity<FacturaDTO> response = facturaController.create(createDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(facturaService, times(1)).create(any(FacturaDTO.class));
    }

    @Test
    @DisplayName("Debe crear factura desde consulta")
    void testCreateFromConsulta() {
        // Arrange
        List<ItemFacturaDTO> itemsAdicionales = List.of(
            ItemFacturaDTO.builder()
                .descripcion("Medicamento")
                .cantidad(BigDecimal.ONE)
                .precioUnitario(BigDecimal.valueOf(20000))
                .build()
        );

        when(facturaService.createFromConsulta(1L, itemsAdicionales)).thenReturn(facturaDTO);

        // Act
        ResponseEntity<FacturaDTO> response = facturaController.createFromConsulta(1L, itemsAdicionales);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(facturaService, times(1)).createFromConsulta(1L, itemsAdicionales);
    }

    @Test
    @DisplayName("Debe actualizar una factura")
    void testUpdate() {
        // Arrange
        FacturaDTO updateDTO = FacturaDTO.builder()
            .observaciones("Observaciones actualizadas")
            .build();

        when(facturaService.update(1L, updateDTO)).thenReturn(facturaDTO);

        // Act
        ResponseEntity<FacturaDTO> response = facturaController.update(1L, updateDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(facturaService, times(1)).update(1L, updateDTO);
    }

    @Test
    @DisplayName("Debe cancelar una factura")
    void testCancel() {
        // Arrange
        doNothing().when(facturaService).cancel(1L);

        // Act
        ResponseEntity<Void> response = facturaController.cancel(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(facturaService, times(1)).cancel(1L);
    }

    @Test
    @DisplayName("Debe registrar un pago")
    void testRegistrarPago() {
        // Arrange
        PagoDTO pagoDTO = PagoDTO.builder()
            .monto(BigDecimal.valueOf(30000))
            .fechaPago(LocalDateTime.now())
            .metodoPago("EFECTIVO")
            .build();

        PagoDTO pagoCreado = PagoDTO.builder()
            .id(1L)
            .monto(BigDecimal.valueOf(30000))
            .fechaPago(LocalDateTime.now())
            .metodoPago("EFECTIVO")
            .facturaId(1L)
            .build();

        when(usuarioRepository.findByEmail("test@clinica.com")).thenReturn(Optional.of(usuario));
        when(facturaService.registrarPago(1L, pagoDTO, 1L)).thenReturn(pagoCreado);

        // Act
        ResponseEntity<PagoDTO> response = facturaController.registrarPago(1L, pagoDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(30000), response.getBody().getMonto());
        verify(facturaService, times(1)).registrarPago(1L, pagoDTO, 1L);
    }

    @Test
    @DisplayName("Debe obtener estadísticas financieras")
    void testGetEstadisticas() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        LocalDate fechaFin = LocalDate.now();

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalFacturado", BigDecimal.valueOf(1000000));
        estadisticas.put("totalPagado", BigDecimal.valueOf(800000));
        estadisticas.put("totalPendiente", BigDecimal.valueOf(200000));
        estadisticas.put("fechaInicio", fechaInicio);
        estadisticas.put("fechaFin", fechaFin);

        when(facturaService.getEstadisticasFinancieras(fechaInicio, fechaFin)).thenReturn(estadisticas);

        // Act
        ResponseEntity<Map<String, Object>> response = facturaController.getEstadisticas(fechaInicio, fechaFin);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(1000000), response.getBody().get("totalFacturado"));
        verify(facturaService, times(1)).getEstadisticasFinancieras(fechaInicio, fechaFin);
    }

    @Test
    @DisplayName("Debe buscar facturas por propietario")
    void testGetByPropietario() {
        // Arrange
        Page<FacturaDTO> page = new PageImpl<>(List.of(facturaDTO), pageable, 1);
        when(facturaService.findByPropietario(1L, pageable)).thenReturn(page);

        // Act
        ResponseEntity<Page<FacturaDTO>> response = facturaController.getByPropietario(1L, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(facturaService, times(1)).findByPropietario(1L, pageable);
    }

    @Test
    @DisplayName("Debe buscar facturas por estado")
    void testGetByEstado() {
        // Arrange
        Page<FacturaDTO> page = new PageImpl<>(List.of(facturaDTO), pageable, 1);
        when(facturaService.findByEstado(Factura.EstadoFactura.PENDIENTE, pageable)).thenReturn(page);

        // Act
        ResponseEntity<Page<FacturaDTO>> response = facturaController.getByEstado(Factura.EstadoFactura.PENDIENTE, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(facturaService, times(1)).findByEstado(Factura.EstadoFactura.PENDIENTE, pageable);
    }

    @Test
    @DisplayName("Debe buscar facturas por rango de fechas")
    void testGetByFechaBetween() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().minusDays(30);
        LocalDate fechaFin = LocalDate.now();
        Page<FacturaDTO> page = new PageImpl<>(List.of(facturaDTO), pageable, 1);
        
        when(facturaService.findByFechaBetween(fechaInicio, fechaFin, pageable)).thenReturn(page);

        // Act
        ResponseEntity<Page<FacturaDTO>> response = facturaController.getByFechaBetween(fechaInicio, fechaFin, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(facturaService, times(1)).findByFechaBetween(fechaInicio, fechaFin, pageable);
    }
}

