package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.NotificacionCreateDTO;
import com.clinica.veterinaria.dto.NotificacionDTO;
import com.clinica.veterinaria.entity.Notificacion;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.NotificacionRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para NotificacionService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de NotificacionService")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private Usuario usuario;
    private Notificacion notificacion1;
    private Notificacion notificacion2;
    private Notificacion notificacionLeida;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        notificacion1 = Notificacion.builder()
            .id(1L)
            .usuario(usuario)
            .titulo("Nueva cita programada")
            .mensaje("Tienes una nueva cita con Max el 2025-11-20")
            .tipo(Notificacion.Tipo.CITA)
            .leida(false)
            .entidadTipo("CITA")
            .entidadId(1L)
            .build();

        notificacion2 = Notificacion.builder()
            .id(2L)
            .usuario(usuario)
            .titulo("Recordatorio de vacunación")
            .mensaje("Max necesita vacunación anual")
            .tipo(Notificacion.Tipo.RECORDATORIO)
            .leida(false)
            .entidadTipo("PACIENTE")
            .entidadId(1L)
            .build();

        notificacionLeida = Notificacion.builder()
            .id(3L)
            .usuario(usuario)
            .titulo("Cita completada")
            .mensaje("La cita con Luna fue completada")
            .tipo(Notificacion.Tipo.CITA)
            .leida(true)
            .entidadTipo("CITA")
            .entidadId(2L)
            .build();
    }

    @Test
    @DisplayName("Debe listar todas las notificaciones de un usuario")
    void testFindByUsuarioId() {
        // Arrange
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L))
            .thenReturn(Arrays.asList(notificacion1, notificacion2, notificacionLeida));

        // Act
        List<NotificacionDTO> resultado = notificacionService.findByUsuarioId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("Nueva cita programada", resultado.get(0).getTitulo());
        verify(notificacionRepository, times(1)).findByUsuarioIdOrderByFechaCreacionDesc(1L);
    }

    @Test
    @DisplayName("Debe listar solo notificaciones no leídas")
    void testFindNoLeidasByUsuarioId() {
        // Arrange
        when(notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(1L))
            .thenReturn(Arrays.asList(notificacion1, notificacion2));

        // Act
        List<NotificacionDTO> resultado = notificacionService.findNoLeidasByUsuarioId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertFalse(resultado.get(0).getLeida());
        assertFalse(resultado.get(1).getLeida());
        verify(notificacionRepository, times(1))
            .findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(1L);
    }

    @Test
    @DisplayName("Debe contar notificaciones no leídas")
    void testCountNoLeidasByUsuarioId() {
        // Arrange
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(2L);

        // Act
        long resultado = notificacionService.countNoLeidasByUsuarioId(1L);

        // Assert
        assertEquals(2L, resultado);
        verify(notificacionRepository, times(1)).countByUsuarioIdAndLeidaFalse(1L);
    }

    @Test
    @DisplayName("Debe crear una nueva notificación")
    void testCreate() {
        // Arrange
        NotificacionCreateDTO createDTO = NotificacionCreateDTO.builder()
            .usuarioId(1L)
            .titulo("Nueva notificación")
            .mensaje("Este es un mensaje de prueba")
            .tipo(Notificacion.Tipo.SISTEMA)
            .entidadTipo("PACIENTE")
            .entidadId(1L)
            .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(4L);
            return n;
        });

        // Act
        NotificacionDTO resultado = notificacionService.create(createDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nueva notificación", resultado.getTitulo());
        assertEquals("Este es un mensaje de prueba", resultado.getMensaje());
        assertFalse(resultado.getLeida()); // Por defecto no leída
        assertEquals(Notificacion.Tipo.SISTEMA, resultado.getTipo());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear notificación con usuario inexistente")
    void testCreateUsuarioNoExiste() {
        // Arrange
        NotificacionCreateDTO createDTO = NotificacionCreateDTO.builder()
            .usuarioId(999L)
            .titulo("Notificación")
            .mensaje("Mensaje")
            .tipo(Notificacion.Tipo.SISTEMA)
            .build();

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> notificacionService.create(createDTO));
        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe marcar una notificación como leída")
    void testMarcarComoLeida() {
        // Arrange
        when(notificacionRepository.marcarComoLeida(1L, 1L)).thenReturn(1);

        // Act
        notificacionService.marcarComoLeida(1L, 1L);

        // Assert
        verify(notificacionRepository, times(1)).marcarComoLeida(1L, 1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al marcar notificación inexistente como leída")
    void testMarcarComoLeidaNoExiste() {
        // Arrange
        when(notificacionRepository.marcarComoLeida(999L, 1L)).thenReturn(0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> notificacionService.marcarComoLeida(999L, 1L));
        assertTrue(exception.getMessage().contains("Notificación no encontrada"));
    }

    @Test
    @DisplayName("Debe marcar todas las notificaciones como leídas")
    void testMarcarTodasComoLeidas() {
        // Arrange
        when(notificacionRepository.marcarTodasComoLeidas(1L)).thenReturn(3); // Retorna número de filas actualizadas

        // Act
        notificacionService.marcarTodasComoLeidas(1L);

        // Assert
        verify(notificacionRepository, times(1)).marcarTodasComoLeidas(1L);
    }

    @Test
    @DisplayName("Debe eliminar una notificación")
    void testDelete() {
        // Arrange
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion1));
        doNothing().when(notificacionRepository).delete(any(Notificacion.class));

        // Act
        notificacionService.delete(1L, 1L);

        // Assert
        verify(notificacionRepository, times(1)).findById(1L);
        verify(notificacionRepository, times(1)).delete(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar notificación inexistente")
    void testDeleteNoExiste() {
        // Arrange
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> notificacionService.delete(999L, 1L));
        assertTrue(exception.getMessage().contains("Notificación no encontrada"));
        verify(notificacionRepository, never()).delete(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar notificación de otro usuario")
    void testDeleteDeOtroUsuario() {
        // Arrange
        Usuario otroUsuario = Usuario.builder()
            .id(2L)
            .nombre("Otro Usuario")
            .email("otro@email.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        Notificacion notificacionOtroUsuario = Notificacion.builder()
            .id(1L)
            .usuario(otroUsuario)
            .titulo("Notificación de otro")
            .mensaje("Mensaje")
            .tipo(Notificacion.Tipo.SISTEMA)
            .leida(false)
            .build();

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionOtroUsuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> notificacionService.delete(1L, 1L)); // Usuario 1 intenta eliminar notificación de usuario 2
        assertTrue(exception.getMessage().contains("no pertenece al usuario"));
        verify(notificacionRepository, never()).delete(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe crear notificación usando método helper")
    void testCrearNotificacion() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(4L);
            return n;
        });

        // Act
        notificacionService.crearNotificacion(
            1L,
            "Título helper",
            "Mensaje helper",
            Notificacion.Tipo.CITA,
            "CITA",
            1L
        );

        // Assert
        verify(usuarioRepository, times(1)).findById(1L);
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe manejar errores silenciosamente en método helper")
    void testCrearNotificacionConError() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act - No debe lanzar excepción
        assertDoesNotThrow(() -> notificacionService.crearNotificacion(
            999L,
            "Título",
            "Mensaje",
            Notificacion.Tipo.SISTEMA,
            "TIPO",
            1L
        ));

        // Assert - El error se maneja internamente sin propagarse
        verify(usuarioRepository, times(1)).findById(999L);
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }
}

