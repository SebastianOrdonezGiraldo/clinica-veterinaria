package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.CitaDTO;
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
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para CitaController
 */
@DisplayName("Tests de Integración - CitaController")
class CitaControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario;
    private Paciente paciente;
    private Cita cita1;
    
    /**
     * Genera una fecha válida para citas (día hábil, horario de atención 10 AM)
     */
    private LocalDateTime generarFechaValida(int diasEnFuturo) {
        LocalDateTime fecha = LocalDateTime.now()
            .plusDays(diasEnFuturo)
            .withHour(10)  // 10 AM, dentro del horario 8 AM - 6 PM
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        // Si cae en fin de semana, mover al próximo lunes
        while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || 
               fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fecha = fecha.plusDays(1);
        }
        
        return fecha;
    }

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
        cita1 = Cita.builder()
            .fecha(generarFechaValida(1))
            .motivo("Vacunación")
            .estado(Cita.EstadoCita.PENDIENTE)
            .paciente(paciente)
            .propietario(propietario)
            .profesional(vetUser)
            .build();
        cita1 = citaRepository.save(cita1);
    }

    @Test
    @DisplayName("GET /api/citas - Debe listar citas")
    void testListarCitas() throws Exception {
        mockMvc.perform(get("/api/citas")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].motivo").value("Vacunación"));
    }

    @Test
    @DisplayName("GET /api/citas/{id} - Debe obtener cita por ID")
    void testObtenerCitaPorId() throws Exception {
        mockMvc.perform(get("/api/citas/" + cita1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.motivo").value("Vacunación"))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("POST /api/citas - Debe crear cita")
    void testCrearCita() throws Exception {
        CitaDTO nuevaDTO = CitaDTO.builder()
            .fecha(generarFechaValida(2))
            .motivo("Control general")
            .estado(Cita.EstadoCita.PENDIENTE)
            .pacienteId(paciente.getId())
            .propietarioId(propietario.getId())
            .profesionalId(vetUser.getId())
            .build();

        mockMvc.perform(post("/api/citas")
                .header("Authorization", "Bearer " + recepcionToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevaDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.motivo").value("Control general"))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("PUT /api/citas/{id} - Debe actualizar cita")
    void testActualizarCita() throws Exception {
        CitaDTO actualizacionDTO = CitaDTO.builder()
            .fecha(generarFechaValida(3))
            .motivo("Vacunación y desparasitación")
            .estado(Cita.EstadoCita.CONFIRMADA)
            .pacienteId(paciente.getId())
            .propietarioId(propietario.getId())
            .profesionalId(vetUser.getId())
            .build();

        mockMvc.perform(put("/api/citas/" + cita1.getId())
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacionDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.motivo").value("Vacunación y desparasitación"))
            .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("DELETE /api/citas/{id} - Solo ADMIN puede eliminar")
    void testEliminarCita() throws Exception {
        mockMvc.perform(delete("/api/citas/" + cita1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/citas/estado/{estado} - Debe filtrar por estado")
    void testListarCitasPorEstado() throws Exception {
        mockMvc.perform(get("/api/citas/estado/PENDIENTE")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /api/citas/profesional/{id} - Debe listar citas por profesional")
    void testListarCitasPorProfesional() throws Exception {
        mockMvc.perform(get("/api/citas/profesional/" + vetUser.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/citas/paciente/{id} - Debe listar citas por paciente")
    void testListarCitasPorPaciente() throws Exception {
        mockMvc.perform(get("/api/citas/paciente/" + paciente.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].motivo").value("Vacunación"));
    }
}

