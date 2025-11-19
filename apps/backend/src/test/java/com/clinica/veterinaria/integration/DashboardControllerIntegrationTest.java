package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.ConsultaRepository;
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
 * Tests de integración para DashboardController
 */
@DisplayName("Tests de Integración - DashboardController")
class DashboardControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario;
    private Paciente paciente;
    private Cita cita;
    private Consulta consulta;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        citaRepository.deleteAll();
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

        // Crear cita de hoy
        cita = Cita.builder()
            .fecha(LocalDateTime.now().withHour(10).withMinute(0))
            .motivo("Consulta general")
            .estado(Cita.EstadoCita.CONFIRMADA)
            .paciente(paciente)
            .propietario(propietario)
            .profesional(vetUser)
            .build();
        cita = citaRepository.save(cita);

        // Crear consulta reciente
        consulta = Consulta.builder()
            .fecha(LocalDateTime.now().minusDays(2))
            .diagnostico("Control de rutina")
            .paciente(paciente)
            .profesional(vetUser)
            .build();
        consulta = consultaRepository.save(consulta);
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe obtener estadísticas del dashboard")
    void testObtenerEstadisticas() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.citasHoy").exists())
            .andExpect(jsonPath("$.pacientesActivos").exists())
            .andExpect(jsonPath("$.consultasPendientes").exists())
            .andExpect(jsonPath("$.totalPropietarios").exists())
            .andExpect(jsonPath("$.proximasCitas").isArray())
            .andExpect(jsonPath("$.consultasPorDia").isArray())
            .andExpect(jsonPath("$.distribucionEspecies").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe funcionar con cualquier usuario autenticado")
    void testObtenerEstadisticas_UsuarioCualquiera() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.citasHoy").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe incluir próximas citas de hoy")
    void testObtenerEstadisticas_ProximasCitas() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.proximasCitas", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.proximasCitas[0].pacienteNombre").exists())
            .andExpect(jsonPath("$.proximasCitas[0].hora").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe incluir distribución de especies")
    void testObtenerEstadisticas_DistribucionEspecies() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distribucionEspecies", hasSize(3))) // Caninos, Felinos, Otros
            .andExpect(jsonPath("$.distribucionEspecies[0].nombre").exists())
            .andExpect(jsonPath("$.distribucionEspecies[0].valor").exists());
    }
}

