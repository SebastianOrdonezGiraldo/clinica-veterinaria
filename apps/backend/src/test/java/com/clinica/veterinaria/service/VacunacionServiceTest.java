package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.VacunacionDTO;
import com.clinica.veterinaria.entity.*;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacunacionServiceTest {

    @Mock
    private VacunacionRepository vacunacionRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private VacunaRepository vacunaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IAuditLogger auditLogger;

    @InjectMocks
    private VacunacionService vacunacionService;

    private Paciente paciente;
    private Vacuna vacuna;
    private Usuario profesional;
    private Vacunacion vacunacion;
    private VacunacionDTO vacunacionDTO;

    @BeforeEach
    void setUp() {
        paciente = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Canino")
            .build();

        vacuna = Vacuna.builder()
            .id(1L)
            .nombre("Antirrábica")
            .especie("Canino")
            .numeroDosis(1)
            .intervaloDias(365)
            .activo(true)
            .build();

        profesional = Usuario.builder()
            .id(1L)
            .nombre("Dr. Veterinario")
            .rol(com.clinica.veterinaria.entity.Usuario.Rol.VET)
            .activo(true)
            .build();

        vacunacion = Vacunacion.builder()
            .id(1L)
            .paciente(paciente)
            .vacuna(vacuna)
            .profesional(profesional)
            .fechaAplicacion(LocalDate.now())
            .numeroDosis(1)
            .proximaDosis(LocalDate.now().plusDays(365))
            .build();

        vacunacionDTO = VacunacionDTO.builder()
            .pacienteId(1L)
            .vacunaId(1L)
            .profesionalId(1L)
            .fechaAplicacion(LocalDate.now())
            .numeroDosis(1)
            .build();
    }

    @Test
    void testCreate_Success() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(vacunacionRepository.save(any(Vacunacion.class))).thenReturn(vacunacion);

        VacunacionDTO result = vacunacionService.create(vacunacionDTO);

        assertNotNull(result);
        assertEquals(1, result.getNumeroDosis());
        verify(vacunacionRepository, times(1)).save(any(Vacunacion.class));
        verify(auditLogger, times(1)).logCreate(anyString(), any(), anyString());
    }

    @Test
    void testCreate_PacienteNotFound() {
        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());
        vacunacionDTO.setPacienteId(999L);

        assertThrows(ResourceNotFoundException.class, () -> vacunacionService.create(vacunacionDTO));
        verify(vacunacionRepository, never()).save(any());
    }

    @Test
    void testCreate_VacunaNotFound() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(999L)).thenReturn(Optional.empty());
        vacunacionDTO.setVacunaId(999L);

        assertThrows(ResourceNotFoundException.class, () -> vacunacionService.create(vacunacionDTO));
    }

    @Test
    void testCreate_VacunaInactiva() {
        vacuna.setActivo(false);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));

        assertThrows(InvalidDataException.class, () -> vacunacionService.create(vacunacionDTO));
    }

    @Test
    void testCreate_NumeroDosisInvalido() {
        vacunacionDTO.setNumeroDosis(0);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));

        assertThrows(InvalidDataException.class, () -> vacunacionService.create(vacunacionDTO));
    }

    @Test
    void testCreate_NumeroDosisExcedeMaximo() {
        vacuna.setNumeroDosis(2);
        vacunacionDTO.setNumeroDosis(3);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));

        assertThrows(InvalidDataException.class, () -> vacunacionService.create(vacunacionDTO));
    }

    @Test
    void testCreate_FechaFutura() {
        vacunacionDTO.setFechaAplicacion(LocalDate.now().plusDays(1));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));

        assertThrows(InvalidDataException.class, () -> vacunacionService.create(vacunacionDTO));
    }

    @Test
    void testCreate_CalculaProximaDosis() {
        vacuna.setNumeroDosis(3);
        vacuna.setIntervaloDias(21);
        vacunacionDTO.setNumeroDosis(1);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(profesional));
        when(vacunacionRepository.save(any(Vacunacion.class))).thenAnswer(invocation -> {
            Vacunacion v = invocation.getArgument(0);
            assertNotNull(v.getProximaDosis());
            assertEquals(LocalDate.now().plusDays(21), v.getProximaDosis());
            return v;
        });

        vacunacionService.create(vacunacionDTO);

        verify(vacunacionRepository, times(1)).save(any(Vacunacion.class));
    }

    @Test
    void testUpdate_Success() {
        when(vacunacionRepository.findById(1L)).thenReturn(Optional.of(vacunacion));
        when(vacunacionRepository.save(any(Vacunacion.class))).thenReturn(vacunacion);

        vacunacionDTO.setNumeroDosis(2);
        VacunacionDTO result = vacunacionService.update(1L, vacunacionDTO);

        assertNotNull(result);
        verify(vacunacionRepository, times(1)).findById(1L);
        verify(vacunacionRepository, times(1)).save(any(Vacunacion.class));
        verify(auditLogger, times(1)).logUpdate(anyString(), any(), anyString(), anyString());
    }

    @Test
    void testUpdate_NotFound() {
        when(vacunacionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vacunacionService.update(999L, vacunacionDTO));
    }

    @Test
    void testDelete_Success() {
        when(vacunacionRepository.findById(1L)).thenReturn(Optional.of(vacunacion));

        vacunacionService.delete(1L);

        verify(vacunacionRepository, times(1)).findById(1L);
        verify(vacunacionRepository, times(1)).delete(vacunacion);
        verify(auditLogger, times(1)).logDelete(anyString(), any());
    }
}

