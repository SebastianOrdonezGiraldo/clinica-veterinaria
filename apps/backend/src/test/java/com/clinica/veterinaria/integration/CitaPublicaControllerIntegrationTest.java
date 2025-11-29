package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.CitaPublicaRequestDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para CitaPublicaController
 * Estos tests verifican que el endpoint público funciona correctamente sin autenticación
 */
@DisplayName("Tests de Integración - CitaPublicaController")
class CitaPublicaControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietarioExistente;
    private Paciente pacienteExistente;
    
    /**
     * Genera una fecha válida para citas (día hábil, horario de atención 10 AM)
     */
    private LocalDateTime generarFechaValida(int diasEnFuturo) {
        LocalDateTime fecha = LocalDateTime.now()
            .plusDays(diasEnFuturo)
            .withHour(10)
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

        // Crear propietario existente para pruebas con IDs
        propietarioExistente = Propietario.builder()
            .nombre("María García")
            .email("maria@email.com")
            .telefono("555-1111")
            .activo(true)
            .build();
        propietarioExistente = propietarioRepository.save(propietarioExistente);

        // Crear paciente existente
        pacienteExistente = Paciente.builder()
            .nombre("Bella")
            .especie("Gato")
            .raza("Persa")
            .sexo("F")
            .edadMeses(18)
            .pesoKg(new BigDecimal("3.5"))
            .propietario(propietarioExistente)
            .activo(true)
            .build();
        pacienteExistente = pacienteRepository.save(pacienteExistente);
    }

    @Test
    @DisplayName("POST /api/public/citas - Debe crear cita con IDs existentes")
    void testCrearCitaConIdsExistentes() throws Exception {
        // Arrange
        LocalDateTime fecha = generarFechaValida(1);
        
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fecha)
            .motivo("Control anual")
            .observaciones("Primera visita del año")
            .profesionalId(vetUser.getId())
            .propietarioId(propietarioExistente.getId())
            .pacienteId(pacienteExistente.getId())
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/public/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.motivo").value("Control anual"))
            .andExpect(jsonPath("$.pacienteId").value(pacienteExistente.getId().intValue()))
            .andExpect(jsonPath("$.propietarioId").value(propietarioExistente.getId().intValue()))
            .andExpect(jsonPath("$.profesionalId").value(vetUser.getId().intValue()))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("POST /api/public/citas - Debe crear cita con datos nuevos")
    void testCrearCitaConDatosNuevos() throws Exception {
        // Arrange
        LocalDateTime fecha = generarFechaValida(2);
        
        CitaPublicaRequestDTO.PropietarioNuevoDTO propietarioNuevo = 
            CitaPublicaRequestDTO.PropietarioNuevoDTO.builder()
                .nombre("Carlos Rodríguez")
                .email("carlos@email.com")
                .telefono("555-2222")
                .documento("87654321")
                .direccion("Calle Principal 123")
                .build();

        CitaPublicaRequestDTO.PacienteNuevoDTO pacienteNuevo = 
            CitaPublicaRequestDTO.PacienteNuevoDTO.builder()
                .nombre("Rex")
                .especie("Perro")
                .raza("Pastor Alemán")
                .sexo("M")
                .edadMeses(48)
                .pesoKg(new BigDecimal("35.0"))
                .notas("Muy activo")
                .build();

        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fecha)
            .motivo("Vacunación anual")
            .profesionalId(vetUser.getId())
            .propietarioNuevo(propietarioNuevo)
            .pacienteNuevo(pacienteNuevo)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/public/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.motivo").value("Vacunación anual"))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        // Verificar que se crearon propietario y paciente
        Propietario propietarioCreado = propietarioRepository.findByEmail("carlos@email.com")
            .orElse(null);
        assertNotNull(propietarioCreado);
        assertEquals("Carlos Rodríguez", propietarioCreado.getNombre());
    }

    @Test
    @DisplayName("POST /api/public/citas - Debe reutilizar propietario existente por email")
    void testCrearCitaReutilizarPropietarioPorEmail() throws Exception {
        // Arrange
        LocalDateTime fecha = generarFechaValida(3);
        
        // Intentar crear con email que ya existe
        CitaPublicaRequestDTO.PropietarioNuevoDTO propietarioNuevo = 
            CitaPublicaRequestDTO.PropietarioNuevoDTO.builder()
                .nombre("María García Actualizada")
                .email("maria@email.com") // Email existente
                .telefono("555-9999")
                .build();

        CitaPublicaRequestDTO.PacienteNuevoDTO pacienteNuevo = 
            CitaPublicaRequestDTO.PacienteNuevoDTO.builder()
                .nombre("Nuevo Gato")
                .especie("Gato")
                .build();

        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(fecha)
            .motivo("Nueva mascota")
            .profesionalId(vetUser.getId())
            .propietarioNuevo(propietarioNuevo)
            .pacienteNuevo(pacienteNuevo)
            .build();

        long propietariosAntes = propietarioRepository.count();

        // Act & Assert
        mockMvc.perform(post("/api/public/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());

        // Verificar que NO se creó un nuevo propietario
        long propietariosDespues = propietarioRepository.count();
        assertEquals(propietariosAntes, propietariosDespues, 
            "No debe crear un nuevo propietario si ya existe por email");
    }

    @Test
    @DisplayName("POST /api/public/citas - Debe validar datos requeridos")
    void testCrearCitaValidacionDatos() throws Exception {
        // Arrange - Request sin motivo
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(generarFechaValida(1))
            // Sin motivo
            .profesionalId(vetUser.getId())
            .propietarioId(propietarioExistente.getId())
            .pacienteId(pacienteExistente.getId())
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/public/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/public/citas - Debe rechazar cuando propietario no existe")
    void testCrearCitaPropietarioNoExiste() throws Exception {
        // Arrange
        CitaPublicaRequestDTO request = CitaPublicaRequestDTO.builder()
            .fecha(generarFechaValida(1))
            .motivo("Consulta")
            .profesionalId(vetUser.getId())
            .propietarioId(99999L) // ID inexistente
            .pacienteId(pacienteExistente.getId())
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/public/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/public/veterinarios - Debe listar veterinarios sin autenticación")
    void testListarVeterinariosPublico() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/public/veterinarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].rol").value("VET"))
            .andExpect(jsonPath("$[0].activo").value(true));
    }
}

