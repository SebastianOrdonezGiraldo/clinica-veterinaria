package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
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
 * Tests de integración para ConsultaController
 */
@DisplayName("Tests de Integración - ConsultaController")
class ConsultaControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario;
    private Paciente paciente;
    private Consulta consulta1;

    @BeforeEach
    void setUp() {
        // Limpiar datos
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
        consulta1 = Consulta.builder()
            .fecha(LocalDateTime.now().minusDays(1))
            .frecuenciaCardiaca(80)
            .frecuenciaRespiratoria(20)
            .temperatura(new BigDecimal("38.5"))
            .pesoKg(new BigDecimal("30.5"))
            .examenFisico("Estado general bueno")
            .diagnostico("Gastritis leve")
            .tratamiento("Omeprazol 20mg cada 12 horas por 7 días")
            .paciente(paciente)
            .profesional(vetUser)
            .build();
        consulta1 = consultaRepository.save(consulta1);
    }

    @Test
    @DisplayName("GET /api/consultas - Debe listar consultas (ADMIN, VET)")
    void testListarConsultas() throws Exception {
        mockMvc.perform(get("/api/consultas")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].diagnostico").value("Gastritis leve"));
    }

    @Test
    @DisplayName("GET /api/consultas - Debe rechazar acceso sin rol ADMIN o VET")
    void testListarConsultas_SinPermiso() throws Exception {
        mockMvc.perform(get("/api/consultas")
                .header("Authorization", "Bearer " + recepcionToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/consultas/{id} - Debe obtener consulta por ID")
    void testObtenerConsultaPorId() throws Exception {
        mockMvc.perform(get("/api/consultas/" + consulta1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(consulta1.getId()))
            .andExpect(jsonPath("$.diagnostico").value("Gastritis leve"))
            .andExpect(jsonPath("$.pacienteId").value(paciente.getId()));
    }

    @Test
    @DisplayName("GET /api/consultas/{id} - Debe retornar 404 si consulta no existe")
    void testObtenerConsultaPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/consultas/999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/consultas/paciente/{id} - Debe obtener historia clínica del paciente")
    void testObtenerHistoriaClinica() throws Exception {
        mockMvc.perform(get("/api/consultas/paciente/" + paciente.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].diagnostico").value("Gastritis leve"));
    }

    @Test
    @DisplayName("GET /api/consultas/profesional/{id} - Debe listar consultas por profesional")
    void testListarConsultasPorProfesional() throws Exception {
        mockMvc.perform(get("/api/consultas/profesional/" + vetUser.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/consultas/search - Debe buscar con filtros y paginación")
    void testBuscarConFiltros() throws Exception {
        mockMvc.perform(get("/api/consultas/search")
                .param("pacienteId", paciente.getId().toString())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.content[0].pacienteId").value(paciente.getId()));
    }

    @Test
    @DisplayName("POST /api/consultas - Debe crear nueva consulta")
    void testCrearConsulta() throws Exception {
        ConsultaDTO nuevaConsulta = ConsultaDTO.builder()
            .fecha(LocalDateTime.now())
            .frecuenciaCardiaca(90)
            .frecuenciaRespiratoria(25)
            .temperatura(new BigDecimal("39.0"))
            .pesoKg(new BigDecimal("31.0"))
            .examenFisico("Examen físico normal")
            .diagnostico("Control de rutina")
            .tratamiento("Ninguno")
            .pacienteId(paciente.getId())
            .profesionalId(vetUser.getId())
            .build();

        mockMvc.perform(post("/api/consultas")
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevaConsulta)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.diagnostico").value("Control de rutina"))
            .andExpect(jsonPath("$.pacienteId").value(paciente.getId()))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/consultas - Debe rechazar creación sin rol ADMIN o VET")
    void testCrearConsulta_SinPermiso() throws Exception {
        ConsultaDTO nuevaConsulta = ConsultaDTO.builder()
            .fecha(LocalDateTime.now())
            .diagnostico("Test")
            .pacienteId(paciente.getId())
            .profesionalId(vetUser.getId())
            .build();

        mockMvc.perform(post("/api/consultas")
                .header("Authorization", "Bearer " + recepcionToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevaConsulta)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/consultas/{id} - Debe actualizar consulta")
    void testActualizarConsulta() throws Exception {
        ConsultaDTO actualizacion = ConsultaDTO.builder()
            .fecha(consulta1.getFecha())
            .frecuenciaCardiaca(85)
            .frecuenciaRespiratoria(22)
            .temperatura(new BigDecimal("38.0"))
            .pesoKg(new BigDecimal("30.5"))
            .examenFisico("Mejoría notable")
            .diagnostico("Gastritis resuelta")
            .tratamiento("Continuar tratamiento")
            .pacienteId(paciente.getId())
            .profesionalId(vetUser.getId())
            .build();

        mockMvc.perform(put("/api/consultas/" + consulta1.getId())
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.diagnostico").value("Gastritis resuelta"))
            .andExpect(jsonPath("$.examenFisico").value("Mejoría notable"));
    }

    @Test
    @DisplayName("DELETE /api/consultas/{id} - Solo ADMIN puede eliminar")
    void testEliminarConsulta() throws Exception {
        mockMvc.perform(delete("/api/consultas/" + consulta1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        // Verificar que fue eliminada
        mockMvc.perform(get("/api/consultas/" + consulta1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/consultas/{id} - Debe rechazar eliminación sin rol ADMIN")
    void testEliminarConsulta_SinPermiso() throws Exception {
        mockMvc.perform(delete("/api/consultas/" + consulta1.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/consultas/rango - Debe filtrar por rango de fechas")
    void testBuscarPorRangoFechas() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fin = LocalDateTime.now();

        mockMvc.perform(get("/api/consultas/rango")
                .param("inicio", inicio.toString())
                .param("fin", fin.toString())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}

