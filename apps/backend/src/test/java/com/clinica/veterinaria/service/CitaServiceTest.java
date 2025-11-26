package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Cita.EstadoCita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CitaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de CitaService")
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock(lenient = true)
    private NotificacionService notificacionService;

    @InjectMocks
    private CitaService citaService;

    private Propietario propietario;
    private Paciente paciente;
    private Usuario profesional;
    private Cita cita1;
    private Cita cita2;
    private LocalDateTime fechaFutura;

    @BeforeEach
    void setUp() {
        fechaFutura = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .activo(true)
            .build();

        paciente = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .propietario(propietario)
            .activo(true)
            .build();

        profesional = Usuario.builder()
            .id(1L)
            .nombre("Dr. Smith")
            .email("dr.smith@clinica.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        cita1 = Cita.builder()
            .id(1L)
            .fecha(fechaFutura)
            .motivo("Consulta general")
            .estado(EstadoCita.PENDIENTE)
            .paciente(paciente)
            .propietario(propietario)
            .profesional(profesional)
            .build();

        cita2 = Cita.builder()
            .id(2L)
            .fecha(fechaFutura.plusHours(2))
            .motivo("Vacunación")
            .estado(EstadoCita.CONFIRMADA)
            .paciente(paciente)
            .propietario(propietario)
            .profesional(profesional)
            .build();
    }

    @Test
    @DisplayName("Debe listar todas las citas")
    void testFindAll() {
        // Arrange
        when(citaRepository.findAll()).thenReturn(Arrays.asList(cita1, cita2));

        // Act
        List<CitaDTO> resultado = citaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Consulta general", resultado.get(0).getMotivo());
        verify(citaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener cita por ID")
    void testFindById() {
        // Arrange
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita1));

        // Act
        CitaDTO resultado = citaService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Consulta general", resultado.getMotivo());
        assertEquals(EstadoCita.PENDIENTE, resultado.getEstado());
        verify(citaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cita no existe")
    void testFindByIdNoExiste() {
        // Arrange
        when(citaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> citaService.findById(999L));
        verify(citaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe crear una nueva cita")
    void testCreate() {
        // Arrange
        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(fechaFutura)
            .motivo("Nueva consulta")
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(citaRepository.findByProfesionalIdAndFechaBetween(any(), any(), any()))
            .thenReturn(List.of()); // No hay citas solapadas
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> {
            Cita c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });
        doNothing().when(notificacionService).crearNotificacion(any(), any(), any(), any(), any(), any());

        // Act
        CitaDTO resultado = citaService.create(citaDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nueva consulta", resultado.getMotivo());
        verify(pacienteRepository, times(1)).findById(1L);
        verify(propietarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).findById(1L);
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear cita con paciente inexistente")
    void testCreatePacienteNoExiste() {
        // Arrange
        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(fechaFutura)
            .motivo("Consulta")
            .pacienteId(999L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> citaService.create(citaDTO));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear cita en el pasado")
    void testCreateFechaPasado() {
        // Arrange
        LocalDateTime fechaPasado = LocalDateTime.now().minusDays(1);
        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(fechaPasado)
            .motivo("Consulta")
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));

        // Act & Assert
        assertThrows(BusinessException.class, () -> citaService.create(citaDTO));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear cita en domingo")
    void testCreateDomingo() {
        // Arrange - Calcular próximo domingo
        LocalDateTime ahora = LocalDateTime.now();
        int diasHastaDomingo = DayOfWeek.SUNDAY.getValue() - ahora.getDayOfWeek().getValue();
        if (diasHastaDomingo <= 0) {
            diasHastaDomingo += 7; // Si ya pasó el domingo, usar el próximo
        }
        LocalDateTime fechaDomingo = ahora.plusDays(diasHastaDomingo).withHour(10).withMinute(0);

        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(fechaDomingo)
            .motivo("Consulta")
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));

        // Act & Assert
        assertThrows(BusinessException.class, () -> citaService.create(citaDTO));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear cita en sábado fuera del horario permitido")
    void testCreateSabadoFueraHorario() {
        // Arrange - Calcular próximo sábado a las 2pm (fuera del horario permitido)
        LocalDateTime ahora = LocalDateTime.now();
        int diasHastaSabado = DayOfWeek.SATURDAY.getValue() - ahora.getDayOfWeek().getValue();
        if (diasHastaSabado <= 0) {
            diasHastaSabado += 7; // Si ya pasó el sábado, usar el próximo
        }
        LocalDateTime fechaSabado = ahora.plusDays(diasHastaSabado).withHour(14).withMinute(0);

        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(fechaSabado)
            .motivo("Consulta")
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));

        // Act & Assert
        assertThrows(BusinessException.class, () -> citaService.create(citaDTO));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear cita en día hábil fuera del horario permitido")
    void testCreateDiaHabilFueraHorario() {
        // Arrange - Calcular próximo lunes a las 1pm (fuera del horario permitido)
        LocalDateTime ahora = LocalDateTime.now();
        int diasHastaLunes = DayOfWeek.MONDAY.getValue() - ahora.getDayOfWeek().getValue();
        if (diasHastaLunes <= 0) {
            diasHastaLunes += 7; // Si ya pasó el lunes, usar el próximo
        }
        LocalDateTime fechaLunes = ahora.plusDays(diasHastaLunes).withHour(13).withMinute(0);

        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(fechaLunes)
            .motivo("Consulta")
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));

        // Act & Assert
        assertThrows(BusinessException.class, () -> citaService.create(citaDTO));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe actualizar una cita existente")
    void testUpdate() {
        // Arrange
        CitaDTO actualizacionDTO = CitaDTO.builder()
            .fecha(fechaFutura.plusHours(1))
            .motivo("Consulta actualizada")
            .estado(EstadoCita.CONFIRMADA)
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita1));
        // La cita ya tiene paciente y propietario, solo se necesita si cambian
        // Como la cita ya tiene los mismos IDs, no se necesitan estos stubbings
        when(citaRepository.findByProfesionalIdAndFechaBetween(any(), any(), any()))
            .thenReturn(List.of()); // No hay citas solapadas
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CitaDTO resultado = citaService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Consulta actualizada", resultado.getMotivo());
        assertEquals(EstadoCita.CONFIRMADA, resultado.getEstado());
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe cambiar el estado de una cita")
    void testCambiarEstado() {
        // Arrange
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita1));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CitaDTO resultado = citaService.cambiarEstado(1L, EstadoCita.ATENDIDA);

        // Assert
        assertNotNull(resultado);
        assertEquals(EstadoCita.ATENDIDA, resultado.getEstado());
        verify(citaRepository, times(1)).findById(1L);
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("Debe eliminar una cita")
    void testDelete() {
        // Arrange
        when(citaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(citaRepository).deleteById(1L);

        // Act
        citaService.delete(1L);

        // Assert
        verify(citaRepository, times(1)).existsById(1L);
        verify(citaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe buscar citas por paciente")
    void testFindByPaciente() {
        // Arrange
        when(citaRepository.findByPacienteId(1L)).thenReturn(Arrays.asList(cita1, cita2));

        // Act
        List<CitaDTO> resultado = citaService.findByPaciente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(citaRepository, times(1)).findByPacienteId(1L);
    }

    @Test
    @DisplayName("Debe buscar citas por profesional")
    void testFindByProfesional() {
        // Arrange
        when(citaRepository.findByProfesionalId(1L)).thenReturn(Arrays.asList(cita1, cita2));

        // Act
        List<CitaDTO> resultado = citaService.findByProfesional(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(citaRepository, times(1)).findByProfesionalId(1L);
    }

    @Test
    @DisplayName("Debe buscar citas por estado")
    void testFindByEstado() {
        // Arrange
        when(citaRepository.findByEstado(EstadoCita.PENDIENTE)).thenReturn(Arrays.asList(cita1));

        // Act
        List<CitaDTO> resultado = citaService.findByEstado(EstadoCita.PENDIENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(EstadoCita.PENDIENTE, resultado.get(0).getEstado());
        verify(citaRepository, times(1)).findByEstado(EstadoCita.PENDIENTE);
    }

    @Test
    @DisplayName("Debe buscar citas con filtros y paginación")
    void testSearchWithFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cita> paginaCitas = new PageImpl<>(Arrays.asList(cita1, cita2), pageable, 2);
        
        when(citaRepository.findByProfesionalIdAndFechaBetween(any(), any(), any(), any()))
            .thenReturn(paginaCitas);

        LocalDateTime inicio = fechaFutura.minusDays(1);
        LocalDateTime fin = fechaFutura.plusDays(1);

        // Act
        Page<CitaDTO> resultado = citaService.searchWithFilters(
            EstadoCita.PENDIENTE, 1L, 1L, inicio, fin, pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        verify(citaRepository, times(1))
            .findByProfesionalIdAndFechaBetween(any(), any(), any(), any());
    }
}

