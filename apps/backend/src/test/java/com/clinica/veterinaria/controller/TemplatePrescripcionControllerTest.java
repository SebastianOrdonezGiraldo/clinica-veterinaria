package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.TemplatePrescripcionDTO;
import com.clinica.veterinaria.dto.TemplatePrescripcionItemDTO;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.service.TemplatePrescripcionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplatePrescripcionController.class)
@DisplayName("Tests de Integración de TemplatePrescripcionController")
class TemplatePrescripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplatePrescripcionService service;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TemplatePrescripcionDTO templateDTO;

    @BeforeEach
    void setUp() {
        TemplatePrescripcionItemDTO itemDTO = TemplatePrescripcionItemDTO.builder()
            .id(1L)
            .medicamento("Amoxicilina")
            .presentacion("Tabletas 250mg")
            .dosis("10 mg/kg")
            .frecuencia("Cada 8 horas")
            .duracion("7 días")
            .orden(0)
            .build();

        templateDTO = TemplatePrescripcionDTO.builder()
            .id(1L)
            .nombre("Template Antibióticos")
            .descripcion("Template para tratamientos antibióticos")
            .categoria("Antibióticos")
            .indicacionesGenerales("Tomar con alimentos")
            .activo(true)
            .items(Arrays.asList(itemDTO))
            .vecesUsado(0)
            .build();
    }

    @Test
    @DisplayName("Debe obtener todos los templates")
    @WithMockUser(roles = "VET")
    void testGetAll() throws Exception {
        // Arrange
        List<TemplatePrescripcionDTO> templates = Arrays.asList(templateDTO);
        when(service.findAll()).thenReturn(templates);

        // Act & Assert
        mockMvc.perform(get("/api/templates/prescripciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].nombre").value("Template Antibióticos"));

        verify(service, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener categorías")
    @WithMockUser(roles = "VET")
    void testGetCategorias() throws Exception {
        // Arrange
        List<String> categorias = Arrays.asList("Antibióticos", "Analgésicos");
        when(service.getCategorias()).thenReturn(categorias);

        // Act & Assert
        mockMvc.perform(get("/api/templates/prescripciones/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0]").value("Antibióticos"));

        verify(service, times(1)).getCategorias();
    }

    @Test
    @DisplayName("Debe crear un nuevo template")
    @WithMockUser(roles = "VET")
    void testCreate() throws Exception {
        // Arrange
        TemplatePrescripcionDTO createDTO = TemplatePrescripcionDTO.builder()
            .nombre("Nuevo Template")
            .categoria("Analgésicos")
            .build();

        when(service.create(any(TemplatePrescripcionDTO.class), eq(1L))).thenReturn(templateDTO);

        // Act & Assert
        mockMvc.perform(post("/api/templates/prescripciones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Template Antibióticos"));

        verify(service, times(1)).create(any(TemplatePrescripcionDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Debe incrementar el contador de uso")
    @WithMockUser(roles = "VET")
    void testIncrementarUso() throws Exception {
        // Arrange
        doNothing().when(service).incrementarUso(1L);

        // Act & Assert
        mockMvc.perform(post("/api/templates/prescripciones/1/usar")
                .with(csrf()))
            .andExpect(status().isOk());

        verify(service, times(1)).incrementarUso(1L);
    }
}

