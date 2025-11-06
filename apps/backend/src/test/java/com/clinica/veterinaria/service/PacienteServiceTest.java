package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PacienteService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de PacienteService")
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Propietario propietario;
    private Paciente paciente1;
    private Paciente paciente2;

    @BeforeEach
    void setUp() {
        propietario = Propietario.builder()
            .id(1L)
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .activo(true)
            .build();

        paciente1 = Paciente.builder()
            .id(1L)
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .sexo("M")
            .edadMeses(36)
            .pesoKg(new BigDecimal("30.5"))
            .microchip("123456789")
            .propietario(propietario)
            .activo(true)
            .build();

        paciente2 = Paciente.builder()
            .id(2L)
            .nombre("Luna")
            .especie("Gato")
            .raza("Siamés")
            .sexo("F")
            .edadMeses(24)
            .pesoKg(new BigDecimal("4.2"))
            .propietario(propietario)
            .activo(true)
            .build();
    }

    @Test
    @DisplayName("Debe listar todos los pacientes")
    void testFindAll() {
        // Arrange
        when(pacienteRepository.findAll()).thenReturn(Arrays.asList(paciente1, paciente2));

        // Act
        List<PacienteDTO> resultado = pacienteService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Max", resultado.get(0).getNombre());
        assertEquals("Luna", resultado.get(1).getNombre());
        verify(pacienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener paciente por ID")
    void testFindById() {
        // Arrange
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente1));

        // Act
        PacienteDTO resultado = pacienteService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Max", resultado.getNombre());
        assertEquals("Perro", resultado.getEspecie());
        assertEquals("Labrador", resultado.getRaza());
        assertEquals(new BigDecimal("30.5"), resultado.getPesoKg());
        verify(pacienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando paciente no existe")
    void testFindById_NoExiste() {
        // Arrange
        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> pacienteService.findById(999L));
    }

    @Test
    @DisplayName("Debe crear un nuevo paciente")
    void testCreate() {
        // Arrange
        PacienteDTO pacienteDTO = PacienteDTO.builder()
            .nombre("Rocky")
            .especie("Perro")
            .raza("Bulldog")
            .sexo("M")
            .edadMeses(18)
            .pesoKg(new BigDecimal("25.0"))
            .propietarioId(1L)
            .build();

        when(propietarioRepository.findById(1L)).thenReturn(Optional.of(propietario));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente pac = invocation.getArgument(0);
            pac.setId(3L);
            return pac;
        });

        // Act
        PacienteDTO resultado = pacienteService.create(pacienteDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Rocky", resultado.getNombre());
        assertEquals("Perro", resultado.getEspecie());
        assertEquals(1L, resultado.getPropietarioId());
        verify(propietarioRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear paciente con propietario inexistente")
    void testCreate_PropietarioNoExiste() {
        // Arrange
        PacienteDTO pacienteDTO = PacienteDTO.builder()
            .nombre("Rocky")
            .especie("Perro")
            .propietarioId(999L)
            .build();

        when(propietarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> pacienteService.create(pacienteDTO));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    @DisplayName("Debe actualizar un paciente existente")
    void testUpdate() {
        // Arrange
        PacienteDTO actualizacionDTO = PacienteDTO.builder()
            .nombre("Max Actualizado")
            .especie("Perro")
            .raza("Labrador")
            .sexo("M")
            .edadMeses(40)
            .pesoKg(new BigDecimal("32.0"))
            .propietarioId(1L)
            .activo(true)
            .build();

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente1));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PacienteDTO resultado = pacienteService.update(1L, actualizacionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Max Actualizado", resultado.getNombre());
        assertEquals(40, resultado.getEdadMeses());
        assertEquals(new BigDecimal("32.0"), resultado.getPesoKg());
        verify(pacienteRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    @DisplayName("Debe eliminar (soft delete) un paciente")
    void testDelete() {
        // Arrange
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente1));

        // Act
        pacienteService.delete(1L);

        // Assert
        assertFalse(paciente1.getActivo());
        verify(pacienteRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    @DisplayName("Debe listar pacientes por propietario")
    void testFindByPropietario() {
        // Arrange
        when(pacienteRepository.findByPropietarioId(1L))
            .thenReturn(Arrays.asList(paciente1, paciente2));

        // Act
        List<PacienteDTO> resultado = pacienteService.findByPropietario(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pacienteRepository, times(1)).findByPropietarioId(1L);
    }

    @Test
    @DisplayName("Debe buscar pacientes por nombre")
    void testFindByNombre() {
        // Arrange
        when(pacienteRepository.findByNombreContainingIgnoreCase("Max"))
            .thenReturn(Arrays.asList(paciente1));

        // Act
        List<PacienteDTO> resultado = pacienteService.findByNombre("Max");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Max", resultado.get(0).getNombre());
        verify(pacienteRepository, times(1)).findByNombreContainingIgnoreCase("Max");
    }
}

