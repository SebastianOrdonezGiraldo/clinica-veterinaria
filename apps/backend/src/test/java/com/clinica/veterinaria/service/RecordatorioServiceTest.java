package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.NotificacionCreateDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RecordatorioService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de RecordatorioService")
class RecordatorioServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private VacunacionRepository vacunacionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RecordatorioService recordatorioService;

    private Usuario veterinario;
    private Paciente paciente;
    private Propietario propietario;
    private Cita cita;
    private Vacunacion vacunacion;
    private Producto producto;
    private CategoriaProducto categoria;

    @BeforeEach
    void setUp() {
        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
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

        cita = Cita.builder()
            .id(1L)
            .fecha(LocalDateTime.now().plusHours(24))
            .motivo("Consulta general")
            .estado(Cita.EstadoCita.CONFIRMADA)
            .paciente(paciente)
            .propietario(propietario)
            .profesional(veterinario)
            .build();

        Vacuna vacuna = Vacuna.builder()
            .id(1L)
            .nombre("Antirrábica")
            .especie("Canino")
            .activo(true)
            .build();

        vacunacion = Vacunacion.builder()
            .id(1L)
            .paciente(paciente)
            .vacuna(vacuna)
            .proximaDosis(LocalDate.now().minusDays(5))
            .build();

        categoria = CategoriaProducto.builder()
            .id(1L)
            .nombre("Medicamentos")
            .build();

        producto = Producto.builder()
            .id(1L)
            .nombre("Medicamento A")
            .stockActual(BigDecimal.valueOf(5))
            .stockMinimo(BigDecimal.valueOf(10))
            .activo(true)
            .categoria(categoria)
            .build();
    }

    @Test
    @DisplayName("Debe enviar recordatorios de citas en 24 horas")
    void testEnviarRecordatoriosCitas_24Horas() {
        // Arrange
        when(citaRepository.findByFechaBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(cita))
            .thenReturn(Arrays.asList()); // Primera llamada (24h), segunda llamada (1h)

        // Act
        recordatorioService.enviarRecordatoriosCitas();

        // Assert
        ArgumentCaptor<NotificacionCreateDTO> captor = ArgumentCaptor.forClass(NotificacionCreateDTO.class);
        verify(notificacionService, atLeastOnce()).create(captor.capture());
        
        NotificacionCreateDTO dto = captor.getValue();
        assertNotNull(dto);
        assertEquals(veterinario.getId(), dto.getUsuarioId());
        assertTrue(dto.getTitulo().contains("24 horas"));
        assertEquals(Notificacion.Tipo.CITA, dto.getTipo());
    }

    @Test
    @DisplayName("Debe enviar alertas de vacunaciones vencidas")
    void testEnviarAlertasVacunaciones_Vencidas() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        
        when(vacunacionRepository.findVencidas(hoy))
            .thenReturn(Arrays.asList(vacunacion));
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true))
            .thenReturn(Arrays.asList(veterinario));

        // Act
        recordatorioService.enviarAlertasVacunaciones();

        // Assert
        ArgumentCaptor<NotificacionCreateDTO> captor = ArgumentCaptor.forClass(NotificacionCreateDTO.class);
        verify(notificacionService, atLeastOnce()).create(captor.capture());
        
        NotificacionCreateDTO dto = captor.getValue();
        assertNotNull(dto);
        assertEquals(veterinario.getId(), dto.getUsuarioId());
        assertTrue(dto.getTitulo().contains("vencida"));
        assertEquals(Notificacion.Tipo.SISTEMA, dto.getTipo());
    }

    @Test
    @DisplayName("Debe enviar alertas de productos con stock bajo")
    void testEnviarAlertasStockBajo() {
        // Arrange
        Usuario admin = Usuario.builder()
            .id(2L)
            .nombre("Admin")
            .email("admin@clinica.com")
            .rol(Usuario.Rol.ADMIN)
            .activo(true)
            .build();

        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList(producto));
        when(usuarioRepository.findByRolInAndActivo(any(), eq(true)))
            .thenReturn(Arrays.asList(admin));

        // Act
        recordatorioService.enviarAlertasStockBajo();

        // Assert
        ArgumentCaptor<NotificacionCreateDTO> captor = ArgumentCaptor.forClass(NotificacionCreateDTO.class);
        verify(notificacionService, atLeastOnce()).create(captor.capture());
        
        NotificacionCreateDTO dto = captor.getValue();
        assertNotNull(dto);
        assertTrue(dto.getTitulo().contains("stock bajo"));
        assertEquals(Notificacion.Tipo.SISTEMA, dto.getTipo());
    }

    @Test
    @DisplayName("No debe enviar notificaciones si no hay datos")
    void testEnviarRecordatorios_SinDatos() {
        // Arrange
        when(citaRepository.findByFechaBetween(any(), any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findVencidas(any()))
            .thenReturn(Arrays.asList());
        when(vacunacionRepository.findProximasAVencer(any(), any()))
            .thenReturn(Arrays.asList());
        when(productoRepository.findProductosConStockBajo())
            .thenReturn(Arrays.asList());

        // Act
        recordatorioService.enviarRecordatoriosCitas();
        recordatorioService.enviarAlertasVacunaciones();
        recordatorioService.enviarAlertasStockBajo();

        // Assert
        verify(notificacionService, never()).create(any());
    }
}

