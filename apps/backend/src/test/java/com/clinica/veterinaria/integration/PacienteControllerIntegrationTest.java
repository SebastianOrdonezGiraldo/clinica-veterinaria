package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PacienteController
 */
@DisplayName("Tests de Integración - PacienteController")
class PacienteControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario;
    private Paciente paciente1;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        pacienteRepository.deleteAll();
        propietarioRepository.deleteAll();

        // Crear propietario
        propietario = Propietario.builder()
            .nombre("Juan Pérez")
            .email("juan@email.com")
            .telefono("555-1234")
            .activo(true)
            .build();
        propietario = propietarioRepository.save(propietario);

        // Crear paciente de prueba
        paciente1 = Paciente.builder()
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .sexo("M")
            .edadMeses(36)
            .pesoKg(new BigDecimal("30.5"))
            .propietario(propietario)
            .activo(true)
            .build();
        paciente1 = pacienteRepository.save(paciente1);
    }

    @Test
    @DisplayName("GET /api/pacientes - Debe listar pacientes")
    void testListarPacientes() throws Exception {
        mockMvc.perform(get("/api/pacientes")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].nombre").value("Max"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} - Debe obtener paciente por ID")
    void testObtenerPacientePorId() throws Exception {
        mockMvc.perform(get("/api/pacientes/" + paciente1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Max"))
            .andExpect(jsonPath("$.especie").value("Perro"))
            .andExpect(jsonPath("$.raza").value("Labrador"));
    }

    @Test
    @DisplayName("POST /api/pacientes - Debe crear paciente")
    void testCrearPaciente() throws Exception {
        PacienteDTO nuevoDTO = PacienteDTO.builder()
            .nombre("Luna")
            .especie("Gato")
            .raza("Siamés")
            .sexo("F")
            .edadMeses(24)
            .pesoKg(new BigDecimal("4.2"))
            .propietarioId(propietario.getId())
            .build();

        mockMvc.perform(post("/api/pacientes")
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevoDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Luna"))
            .andExpect(jsonPath("$.especie").value("Gato"));
    }

    @Test
    @DisplayName("POST /api/pacientes - Debe validar propietario existente")
    void testCrearPaciente_PropietarioInexistente() throws Exception {
        PacienteDTO dtoInvalido = PacienteDTO.builder()
            .nombre("Rocky")
            .especie("Perro")
            .propietarioId(99999L) // ID inexistente
            .build();

        mockMvc.perform(post("/api/pacientes")
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dtoInvalido)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id} - Debe actualizar paciente")
    void testActualizarPaciente() throws Exception {
        PacienteDTO actualizacionDTO = PacienteDTO.builder()
            .nombre("Max Actualizado")
            .especie("Perro")
            .raza("Labrador")
            .sexo("M")
            .edadMeses(40)
            .pesoKg(new BigDecimal("32.0"))
            .propietarioId(propietario.getId())
            .activo(true)
            .build();

        mockMvc.perform(put("/api/pacientes/" + paciente1.getId())
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacionDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Max Actualizado"))
            .andExpect(jsonPath("$.edadMeses").value(40));
    }

    @Test
    @DisplayName("DELETE /api/pacientes/{id} - Solo ADMIN puede eliminar")
    void testEliminarPaciente() throws Exception {
        mockMvc.perform(delete("/api/pacientes/" + paciente1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/pacientes/propietario/{id} - Debe listar pacientes por propietario")
    void testListarPacientesPorPropietario() throws Exception {
        mockMvc.perform(get("/api/pacientes/propietario/" + propietario.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].nombre").value("Max"));
    }

    @Test
    @DisplayName("GET /api/pacientes/buscar - Debe buscar por nombre")
    void testBuscarPacientes() throws Exception {
        mockMvc.perform(get("/api/pacientes/buscar")
                .param("nombre", "Max")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].nombre", containsString("Max")));
    }
}

