package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.PrescripcionDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Prescripcion;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PrescripcionRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PrescripcionController
 */
@DisplayName("Tests de Integración - PrescripcionController")
class PrescripcionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PrescripcionRepository prescripcionRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario;
    private Paciente paciente;
    private Consulta consulta;
    private Prescripcion prescripcion1;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        prescripcionRepository.deleteAll();
        consultaRepository.deleteAll();
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

        // Crear paciente
        paciente = Paciente.builder()
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .sexo("M")
            .edadMeses(36)
            .pesoKg(new BigDecimal("30.5"))
            .propietario(propietario)
            .activo(true)
            .build();
        paciente = pacienteRepository.save(paciente);

        // Crear consulta
        consulta = Consulta.builder()
            .fecha(LocalDateTime.now().minusDays(1))
            .frecuenciaCardiaca(80)
            .frecuenciaRespiratoria(20)
            .temperatura(new BigDecimal("38.5"))
            .pesoKg(new BigDecimal("30.5"))
            .examenFisico("Estado general bueno")
            .diagnostico("Gastritis leve")
            .tratamiento("Omeprazol")
            .paciente(paciente)
            .profesional(vetUser)
            .build();
        consulta = consultaRepository.save(consulta);

        // Crear prescripción
        prescripcion1 = Prescripcion.builder()
            .fechaEmision(LocalDateTime.now())
            .indicacionesGenerales("Administrar con comida")
            .consulta(consulta)
            .build();
        prescripcion1 = prescripcionRepository.save(prescripcion1);
    }

    @Test
    @DisplayName("GET /api/prescripciones - Debe listar prescripciones (ADMIN, VET)")
    void testListarPrescripciones() throws Exception {
        mockMvc.perform(get("/api/prescripciones")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].indicacionesGenerales").value("Administrar con comida"));
    }

    @Test
    @DisplayName("GET /api/prescripciones - Debe rechazar acceso sin rol ADMIN o VET")
    void testListarPrescripciones_SinPermiso() throws Exception {
        mockMvc.perform(get("/api/prescripciones")
                .header("Authorization", "Bearer " + recepcionToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/prescripciones/{id} - Debe obtener prescripción por ID")
    void testObtenerPrescripcionPorId() throws Exception {
        mockMvc.perform(get("/api/prescripciones/" + prescripcion1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(prescripcion1.getId()))
            .andExpect(jsonPath("$.consultaId").value(consulta.getId()))
            .andExpect(jsonPath("$.indicacionesGenerales").value("Administrar con comida"));
    }

    @Test
    @DisplayName("GET /api/prescripciones/consulta/{id} - Debe obtener prescripciones por consulta")
    void testObtenerPrescripcionesPorConsulta() throws Exception {
        mockMvc.perform(get("/api/prescripciones/consulta/" + consulta.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].consultaId").value(consulta.getId()));
    }

    @Test
    @DisplayName("GET /api/prescripciones/paciente/{id} - Debe obtener prescripciones por paciente")
    void testObtenerPrescripcionesPorPaciente() throws Exception {
        mockMvc.perform(get("/api/prescripciones/paciente/" + paciente.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/prescripciones/search - Debe buscar con filtros y paginación")
    void testBuscarConFiltros() throws Exception {
        mockMvc.perform(get("/api/prescripciones/search")
                .param("consultaId", consulta.getId().toString())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.content[0].consultaId").value(consulta.getId()));
    }

    @Test
    @DisplayName("POST /api/prescripciones - Debe crear nueva prescripción")
    void testCrearPrescripcion() throws Exception {
        PrescripcionDTO nuevaPrescripcion = PrescripcionDTO.builder()
            .fechaEmision(LocalDateTime.now())
            .indicacionesGenerales("Nuevas indicaciones")
            .consultaId(consulta.getId())
            .build();

        mockMvc.perform(post("/api/prescripciones")
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevaPrescripcion)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.indicacionesGenerales").value("Nuevas indicaciones"))
            .andExpect(jsonPath("$.consultaId").value(consulta.getId()))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/prescripciones - Debe rechazar creación sin rol ADMIN o VET")
    void testCrearPrescripcion_SinPermiso() throws Exception {
        PrescripcionDTO nuevaPrescripcion = PrescripcionDTO.builder()
            .fechaEmision(LocalDateTime.now())
            .consultaId(consulta.getId())
            .build();

        mockMvc.perform(post("/api/prescripciones")
                .header("Authorization", "Bearer " + recepcionToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevaPrescripcion)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/prescripciones/{id} - Debe actualizar prescripción")
    void testActualizarPrescripcion() throws Exception {
        PrescripcionDTO actualizacion = PrescripcionDTO.builder()
            .fechaEmision(prescripcion1.getFechaEmision())
            .indicacionesGenerales("Indicaciones actualizadas")
            .consultaId(consulta.getId())
            .build();

        mockMvc.perform(put("/api/prescripciones/" + prescripcion1.getId())
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.indicacionesGenerales").value("Indicaciones actualizadas"));
    }

    @Test
    @DisplayName("DELETE /api/prescripciones/{id} - Solo ADMIN puede eliminar")
    void testEliminarPrescripcion() throws Exception {
        mockMvc.perform(delete("/api/prescripciones/" + prescripcion1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        // Verificar que fue eliminada
        mockMvc.perform(get("/api/prescripciones/" + prescripcion1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/prescripciones/{id} - Debe rechazar eliminación sin rol ADMIN")
    void testEliminarPrescripcion_SinPermiso() throws Exception {
        mockMvc.perform(delete("/api/prescripciones/" + prescripcion1.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isForbidden());
    }
}

