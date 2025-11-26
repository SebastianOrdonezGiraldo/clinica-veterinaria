package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.*;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CitaPublicaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de CitaPublicaService")
class CitaPublicaServiceTest {

    @Mock
    private CitaService citaService;

    @Mock
    private PropietarioService propietarioService;

    @Mock
    private PacienteService pacienteService;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private CitaPublicaService citaPublicaService;

    private Propietario propietario;
    private Paciente paciente;
    private Usuario profesional;
    private LocalDateTime fechaFutura;

    @BeforeEach
    void setUp() {
        fechaFutura = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .telefono("555-1234")
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
    }

    @Test
    @DisplayName("Debe crear cita con IDs existentes")
    void testCrearCitaConIdsExistentes() {
        // Arrange
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Consulta general")
            .profesionalId(1L)
            .propietarioId(1L)
            .pacienteId(1L)
            .build();

        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        CitaDTO citaDTO = CitaDTO.builder()
            .id(1L)
            .fecha(fechaFutura)
            .motivo("Consulta general")
            .pacienteId(1L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(citaService.create(any(CitaDTO.class))).thenReturn(citaDTO);

        // Act
        CitaDTO resultado = citaPublicaService.crearCitaPublica(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Consulta general", resultado.getMotivo());
        verify(propietarioRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).findById(1L);
        verify(citaService, times(1)).create(any(CitaDTO.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando propietario no existe (IDs existentes)")
    void testCrearCitaPropietarioNoExiste() {
        // Arrange
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Consulta general")
            .profesionalId(1L)
            .propietarioId(999L)
            .pacienteId(1L)
            .build();

        when(propietarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> citaPublicaService.crearCitaPublica(request));
        verify(citaService, never()).create(any(CitaDTO.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando paciente no pertenece al propietario")
    void testCrearCitaPacienteNoPertenecePropietario() {
        // Arrange
        Propietario otroPropietario = Propietario.builder()
            .id(2L)
            .nombre("María García")
            .email("maria@email.com")
            .build();

        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Consulta general")
            .profesionalId(1L)
            .propietarioId(2L)
            .pacienteId(1L)
            .build();

        when(propietarioRepository.findById(2L)).thenReturn(Optional.of(otroPropietario));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        // Act & Assert
        assertThrows(BusinessException.class, 
            () -> citaPublicaService.crearCitaPublica(request));
        verify(citaService, never()).create(any(CitaDTO.class));
    }

    @Test
    @DisplayName("Debe crear cita con datos nuevos de propietario y paciente")
    void testCrearCitaConDatosNuevos() {
        // Arrange
        CitaPublicaRequestDTO.PropietarioNuevoDTO propietarioNuevo = 
            CitaPublicaRequestDTO.PropietarioNuevoDTO.builder()
                .nombre("Carlos López")
                .email("carlos@email.com")
                .telefono("555-5678")
                .documento("12345678")
                .build();

        CitaPublicaRequestDTO.PacienteNuevoDTO pacienteNuevo = 
            CitaPublicaRequestDTO.PacienteNuevoDTO.builder()
                .nombre("Luna")
                .especie("Gato")
                .raza("Siamés")
                .sexo("F")
                .edadMeses(24)
                .pesoKg(new BigDecimal("4.2"))
                .build();

        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Primera consulta")
            .profesionalId(1L)
            .propietarioNuevo(propietarioNuevo)
            .pacienteNuevo(pacienteNuevo)
            .build();

        // Propietario no existe
        when(propietarioRepository.findByEmail("carlos@email.com"))
            .thenReturn(Optional.empty());

        PropietarioDTO propietarioCreado = PropietarioDTO.builder()
            .id(2L)
            .nombre("Carlos López")
            .email("carlos@email.com")
            .build();

        when(propietarioService.create(any(PropietarioDTO.class)))
            .thenReturn(propietarioCreado);

        PacienteDTO pacienteCreado = PacienteDTO.builder()
            .id(2L)
            .nombre("Luna")
            .especie("Gato")
            .propietarioId(2L)
            .build();

        when(pacienteService.create(any(PacienteDTO.class)))
            .thenReturn(pacienteCreado);

        CitaDTO citaDTO = CitaDTO.builder()
            .id(1L)
            .fecha(fechaFutura)
            .motivo("Primera consulta")
            .pacienteId(2L)
            .propietarioId(2L)
            .profesionalId(1L)
            .build();

        when(citaService.create(any(CitaDTO.class))).thenReturn(citaDTO);

        // Act
        CitaDTO resultado = citaPublicaService.crearCitaPublica(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Primera consulta", resultado.getMotivo());
        verify(propietarioRepository, times(1)).findByEmail("carlos@email.com");
        verify(propietarioService, times(1)).create(any(PropietarioDTO.class));
        verify(pacienteService, times(1)).create(any(PacienteDTO.class));
        verify(citaService, times(1)).create(any(CitaDTO.class));
    }

    @Test
    @DisplayName("Debe reutilizar propietario existente cuando se encuentra por email")
    void testCrearCitaReutilizarPropietarioExistente() {
        // Arrange
        CitaPublicaRequestDTO.PropietarioNuevoDTO propietarioNuevo = 
            CitaPublicaRequestDTO.PropietarioNuevoDTO.builder()
                .nombre("Juan Pérez Actualizado")
                .email("juan@email.com") // Mismo email que propietario existente
                .telefono("555-9999")
                .build();

        CitaPublicaRequestDTO.PacienteNuevoDTO pacienteNuevo = 
            CitaPublicaRequestDTO.PacienteNuevoDTO.builder()
                .nombre("Nueva Mascota")
                .especie("Perro")
                .build();

        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Nueva mascota")
            .profesionalId(1L)
            .propietarioNuevo(propietarioNuevo)
            .pacienteNuevo(pacienteNuevo)
            .build();

        // Propietario ya existe
        when(propietarioRepository.findByEmail("juan@email.com"))
            .thenReturn(Optional.of(propietario));

        PacienteDTO pacienteCreado = PacienteDTO.builder()
            .id(2L)
            .nombre("Nueva Mascota")
            .especie("Perro")
            .propietarioId(1L)
            .build();

        when(pacienteService.create(any(PacienteDTO.class)))
            .thenReturn(pacienteCreado);

        CitaDTO citaDTO = CitaDTO.builder()
            .id(1L)
            .fecha(fechaFutura)
            .motivo("Nueva mascota")
            .pacienteId(2L)
            .propietarioId(1L)
            .profesionalId(1L)
            .build();

        when(citaService.create(any(CitaDTO.class))).thenReturn(citaDTO);

        // Act
        CitaDTO resultado = citaPublicaService.crearCitaPublica(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nueva mascota", resultado.getMotivo());
        verify(propietarioRepository, times(1)).findByEmail("juan@email.com");
        verify(propietarioService, never()).create(any(PropietarioDTO.class)); // No debe crear
        verify(pacienteService, times(1)).create(any(PacienteDTO.class));
        verify(citaService, times(1)).create(any(CitaDTO.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando faltan datos requeridos")
    void testCrearCitaFaltanDatos() {
        // Arrange - Solo tiene IDs pero no propietarioNuevo ni pacienteNuevo
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Consulta")
            .profesionalId(1L)
            // Sin propietarioId ni propietarioNuevo
            .build();

        // Act & Assert
        assertThrows(BusinessException.class, 
            () -> citaPublicaService.crearCitaPublica(request));
        verify(citaService, never()).create(any(CitaDTO.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando paciente no existe (IDs existentes)")
    void testCrearCitaPacienteNoExiste() {
        // Arrange
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fechaFutura)
            .motivo("Consulta general")
            .profesionalId(1L)
            .propietarioId(1L)
            .pacienteId(999L)
            .build();

        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> citaPublicaService.crearCitaPublica(request));
        verify(citaService, never()).create(any(CitaDTO.class));
    }
}

