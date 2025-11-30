package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.VacunaDTO;
import com.clinica.veterinaria.entity.Vacuna;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.VacunaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacunaServiceTest {

    @Mock
    private VacunaRepository vacunaRepository;

    @Mock
    private IAuditLogger auditLogger;

    @InjectMocks
    private VacunaService vacunaService;

    private Vacuna vacuna;
    private VacunaDTO vacunaDTO;

    @BeforeEach
    void setUp() {
        vacuna = Vacuna.builder()
            .id(1L)
            .nombre("Antirrábica")
            .especie("Canino")
            .numeroDosis(1)
            .intervaloDias(365)
            .descripcion("Vacuna antirrábica anual")
            .fabricante("Laboratorio XYZ")
            .activo(true)
            .build();

        vacunaDTO = VacunaDTO.builder()
            .nombre("Antirrábica")
            .especie("Canino")
            .numeroDosis(1)
            .intervaloDias(365)
            .descripcion("Vacuna antirrábica anual")
            .fabricante("Laboratorio XYZ")
            .activo(true)
            .build();
    }

    @Test
    void testFindById_Success() {
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));

        VacunaDTO result = vacunaService.findById(1L);

        assertNotNull(result);
        assertEquals("Antirrábica", result.getNombre());
        assertEquals("Canino", result.getEspecie());
        verify(vacunaRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(vacunaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vacunaService.findById(999L));
    }

    @Test
    void testCreate_Success() {
        when(vacunaRepository.save(any(Vacuna.class))).thenReturn(vacuna);

        VacunaDTO result = vacunaService.create(vacunaDTO);

        assertNotNull(result);
        assertEquals("Antirrábica", result.getNombre());
        verify(vacunaRepository, times(1)).save(any(Vacuna.class));
        verify(auditLogger, times(1)).logCreate(anyString(), any(), anyString());
    }

    @Test
    void testCreate_InvalidNumeroDosis() {
        vacunaDTO.setNumeroDosis(0);

        assertThrows(InvalidDataException.class, () -> vacunaService.create(vacunaDTO));
        verify(vacunaRepository, never()).save(any());
    }

    @Test
    void testCreate_InvalidIntervaloDias() {
        vacunaDTO.setIntervaloDias(-1);

        assertThrows(InvalidDataException.class, () -> vacunaService.create(vacunaDTO));
        verify(vacunaRepository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));
        when(vacunaRepository.save(any(Vacuna.class))).thenReturn(vacuna);

        vacunaDTO.setNombre("Antirrábica Actualizada");
        VacunaDTO result = vacunaService.update(1L, vacunaDTO);

        assertNotNull(result);
        verify(vacunaRepository, times(1)).findById(1L);
        verify(vacunaRepository, times(1)).save(any(Vacuna.class));
        verify(auditLogger, times(1)).logUpdate(anyString(), any(), anyString(), anyString());
    }

    @Test
    void testUpdate_NotFound() {
        when(vacunaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vacunaService.update(999L, vacunaDTO));
    }

    @Test
    void testDelete_Success() {
        when(vacunaRepository.findById(1L)).thenReturn(Optional.of(vacuna));
        when(vacunaRepository.save(any(Vacuna.class))).thenReturn(vacuna);

        vacunaService.delete(1L);

        verify(vacunaRepository, times(1)).findById(1L);
        verify(vacunaRepository, times(1)).save(any(Vacuna.class));
        verify(auditLogger, times(1)).logDelete(anyString(), any());
        assertFalse(vacuna.getActivo());
    }

    @Test
    void testFindByEspecie_Success() {
        List<Vacuna> vacunas = Arrays.asList(vacuna);
        when(vacunaRepository.findByEspecieAndActivo("Canino", true)).thenReturn(vacunas);

        List<VacunaDTO> result = vacunaService.findByEspecie("Canino");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Canino", result.get(0).getEspecie());
    }

    @Test
    void testSearchWithFilters_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Vacuna> page = new PageImpl<>(Arrays.asList(vacuna), pageable, 1);
        when(vacunaRepository.findByFilters("Antirrábica", "Canino", true, pageable)).thenReturn(page);

        Page<VacunaDTO> result = vacunaService.searchWithFilters("Antirrábica", "Canino", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(vacunaRepository, times(1)).findByFilters(anyString(), anyString(), eq(true), any(Pageable.class));
    }
}

