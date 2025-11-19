package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ReporteController
 */
@DisplayName("Tests de Integración - ReporteController")
class ReporteControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario;
    private Paciente paciente;
    private Cita cita;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        citaRepository.deleteAll();
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

        // Crear cita
        cita = Cita.builder()
            .fecha(LocalDateTime.now())
            .motivo("Vacunación anual")
            .estado(Cita.EstadoCita.ATENDIDA)
            .paciente(paciente)
            .propietario(propietario)
            .profesional(vetUser)
            .build();
        cita = citaRepository.save(cita);
    }

    @Test
    @DisplayName("GET /api/reportes - Debe generar reporte con periodo por defecto (mes)")
    void testGenerarReporte_PorDefecto() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCitas").exists())
            .andExpect(jsonPath("$.totalConsultas").exists())
            .andExpect(jsonPath("$.totalPacientes").exists())
            .andExpect(jsonPath("$.totalVeterinarios").exists())
            .andExpect(jsonPath("$.citasPorEstado").isArray())
            .andExpect(jsonPath("$.tendenciaCitas").isArray())
            .andExpect(jsonPath("$.pacientesPorEspecie").isArray())
            .andExpect(jsonPath("$.atencionesPorVeterinario").isArray())
            .andExpect(jsonPath("$.topMotivosConsulta").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes?periodo=hoy - Debe generar reporte para periodo 'hoy'")
    void testGenerarReporte_Hoy() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .param("periodo", "hoy")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCitas").exists());
    }

    @Test
    @DisplayName("GET /api/reportes?periodo=semana - Debe generar reporte para periodo 'semana'")
    void testGenerarReporte_Semana() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .param("periodo", "semana")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCitas").exists());
    }

    @Test
    @DisplayName("GET /api/reportes?periodo=mes - Debe generar reporte para periodo 'mes'")
    void testGenerarReporte_Mes() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .param("periodo", "mes")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCitas").exists());
    }

    @Test
    @DisplayName("GET /api/reportes?periodo=año - Debe generar reporte para periodo 'año'")
    void testGenerarReporte_Ano() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .param("periodo", "año")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCitas").exists());
    }

    @Test
    @DisplayName("GET /api/reportes - Debe rechazar acceso sin rol ADMIN o VET")
    void testGenerarReporte_SinPermiso() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .header("Authorization", "Bearer " + recepcionToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reportes - Debe incluir citas por estado")
    void testGenerarReporte_CitasPorEstado() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.citasPorEstado", hasSize(4))) // PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA
            .andExpect(jsonPath("$.citasPorEstado[0].estado").exists())
            .andExpect(jsonPath("$.citasPorEstado[0].cantidad").exists());
    }

    @Test
    @DisplayName("GET /api/reportes - Debe incluir distribución de pacientes por especie")
    void testGenerarReporte_PacientesPorEspecie() throws Exception {
        mockMvc.perform(get("/api/reportes")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pacientesPorEspecie", hasSize(3))) // Canino, Felino, Otro
            .andExpect(jsonPath("$.pacientesPorEspecie[0].especie").exists())
            .andExpect(jsonPath("$.pacientesPorEspecie[0].cantidad").exists());
    }
}

