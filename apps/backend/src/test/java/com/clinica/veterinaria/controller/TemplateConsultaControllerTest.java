package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.TemplateConsultaDTO;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.service.TemplateConsultaService;
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

@WebMvcTest(TemplateConsultaController.class)
@DisplayName("Tests de Integración de TemplateConsultaController")
class TemplateConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateConsultaService service;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TemplateConsultaDTO templateDTO;

    @BeforeEach
    void setUp() {
        templateDTO = TemplateConsultaDTO.builder()
            .id(1L)
            .nombre("Template General")
            .descripcion("Template para consultas generales")
            .categoria("General")
            .examenFisico("Examen físico normal")
            .diagnostico("Diagnóstico general")
            .tratamiento("Tratamiento estándar")
            .observaciones("Observaciones generales")
            .activo(true)
            .vecesUsado(0)
            .build();
    }

    @Test
    @DisplayName("Debe obtener todos los templates")
    @WithMockUser(roles = "VET")
    void testGetAll() throws Exception {
        // Arrange
        List<TemplateConsultaDTO> templates = Arrays.asList(templateDTO);
        when(service.findAll()).thenReturn(templates);

        // Act & Assert
        mockMvc.perform(get("/api/templates/consultas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].nombre").value("Template General"));

        verify(service, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener templates por categoría")
    @WithMockUser(roles = "VET")
    void testGetByCategoria() throws Exception {
        // Arrange
        List<TemplateConsultaDTO> templates = Arrays.asList(templateDTO);
        when(service.findByCategoria("General")).thenReturn(templates);

        // Act & Assert
        mockMvc.perform(get("/api/templates/consultas/categoria/General"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].categoria").value("General"));

        verify(service, times(1)).findByCategoria("General");
    }

    @Test
    @DisplayName("Debe buscar templates por nombre")
    @WithMockUser(roles = "VET")
    void testSearch() throws Exception {
        // Arrange
        List<TemplateConsultaDTO> templates = Arrays.asList(templateDTO);
        when(service.search("Template")).thenReturn(templates);

        // Act & Assert
        mockMvc.perform(get("/api/templates/consultas/search")
                .param("nombre", "Template"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).search("Template");
    }

    @Test
    @DisplayName("Debe crear un nuevo template")
    @WithMockUser(roles = "VET")
    void testCreate() throws Exception {
        // Arrange
        TemplateConsultaDTO createDTO = TemplateConsultaDTO.builder()
            .nombre("Nuevo Template")
            .categoria("Emergencia")
            .build();

        when(service.create(any(TemplateConsultaDTO.class), eq(1L))).thenReturn(templateDTO);

        // Act & Assert
        mockMvc.perform(post("/api/templates/consultas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Template General"));

        verify(service, times(1)).create(any(TemplateConsultaDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Debe actualizar un template")
    @WithMockUser(roles = "VET")
    void testUpdate() throws Exception {
        // Arrange
        TemplateConsultaDTO updateDTO = TemplateConsultaDTO.builder()
            .nombre("Template Actualizado")
            .categoria("Cirugía")
            .build();

        templateDTO.setNombre("Template Actualizado");
        when(service.update(eq(1L), any(TemplateConsultaDTO.class))).thenReturn(templateDTO);

        // Act & Assert
        mockMvc.perform(put("/api/templates/consultas/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Template Actualizado"));

        verify(service, times(1)).update(eq(1L), any(TemplateConsultaDTO.class));
    }

    @Test
    @DisplayName("Debe eliminar un template")
    @WithMockUser(roles = "VET")
    void testDelete() throws Exception {
        // Arrange
        doNothing().when(service).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/templates/consultas/1")
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(service, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Debe incrementar el contador de uso")
    @WithMockUser(roles = "VET")
    void testIncrementarUso() throws Exception {
        // Arrange
        doNothing().when(service).incrementarUso(1L);

        // Act & Assert
        mockMvc.perform(post("/api/templates/consultas/1/usar")
                .with(csrf()))
            .andExpect(status().isOk());

        verify(service, times(1)).incrementarUso(1L);
    }
}

