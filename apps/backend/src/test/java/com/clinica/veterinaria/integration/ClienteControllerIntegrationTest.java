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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ClienteController
 * Verifica que los endpoints del portal del cliente funcionen correctamente
 */
@DisplayName("Tests de Integración - ClienteController")
class ClienteControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario clientePropietario;
    private Paciente mascota1;
    private Paciente mascota2;
    private Cita citaProxima;
    private Cita citaPasada;
    private Cita citaCancelada;
    private String clienteToken;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        citaRepository.deleteAll();
        pacienteRepository.deleteAll();
        propietarioRepository.deleteAll();

        // Crear propietario cliente con password
        clientePropietario = Propietario.builder()
            .nombre("María García")
            .email("maria.cliente@test.com")
            .telefono("555-1234")
            .direccion("Calle Principal 123")
            .password(passwordEncoder.encode("cliente123"))
            .activo(true)
            .build();
        clientePropietario = propietarioRepository.save(clientePropietario);

        // Generar token JWT para el cliente
        UserDetails clienteDetails = User.builder()
            .username(clientePropietario.getEmail())
            .password(clientePropietario.getPassword())
            .authorities(Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE")
            ))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();

        Map<String, Object> clienteClaims = new HashMap<>();
        clienteClaims.put("rol", "CLIENTE");
        clienteClaims.put("propietarioId", clientePropietario.getId());
        clienteToken = jwtUtil.generateToken(clienteDetails, clienteClaims);

        // Crear mascotas para el cliente
        mascota1 = Paciente.builder()
            .nombre("Max")
            .especie("Perro")
            .raza("Labrador")
            .sexo("M")
            .edadMeses(36)
            .pesoKg(new BigDecimal("30.5"))
            .propietario(clientePropietario)
            .activo(true)
            .build();
        mascota1 = pacienteRepository.save(mascota1);

        mascota2 = Paciente.builder()
            .nombre("Luna")
            .especie("Gato")
            .raza("Persa")
            .sexo("F")
            .edadMeses(18)
            .pesoKg(new BigDecimal("4.2"))
            .propietario(clientePropietario)
            .activo(true)
            .build();
        mascota2 = pacienteRepository.save(mascota2);

        // Crear citas para el cliente
        // Cita próxima (futura)
        citaProxima = Cita.builder()
            .fecha(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0))
            .motivo("Control anual")
            .estado(Cita.EstadoCita.CONFIRMADA)
            .paciente(mascota1)
            .propietario(clientePropietario)
            .profesional(vetUser)
            .build();
        citaProxima = citaRepository.save(citaProxima);

        // Cita pasada (historial)
        citaPasada = Cita.builder()
            .fecha(LocalDateTime.now().minusDays(30).withHour(14).withMinute(30))
            .motivo("Vacunación")
            .estado(Cita.EstadoCita.ATENDIDA)
            .paciente(mascota1)
            .propietario(clientePropietario)
            .profesional(vetUser)
            .build();
        citaPasada = citaRepository.save(citaPasada);

        // Cita cancelada
        citaCancelada = Cita.builder()
            .fecha(LocalDateTime.now().plusDays(14).withHour(16).withMinute(0))
            .motivo("Consulta general")
            .estado(Cita.EstadoCita.CANCELADA)
            .paciente(mascota2)
            .propietario(clientePropietario)
            .profesional(vetUser)
            .build();
        citaCancelada = citaRepository.save(citaCancelada);
    }

    @Test
    @DisplayName("GET /api/clientes/mi-perfil - Debe retornar perfil del cliente autenticado")
    void testGetMiPerfil_Success() throws Exception {
        mockMvc.perform(get("/api/clientes/mi-perfil")
                .header("Authorization", "Bearer " + clienteToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clientePropietario.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value("María García"))
            .andExpect(jsonPath("$.email").value("maria.cliente@test.com"))
            .andExpect(jsonPath("$.telefono").value("555-1234"))
            .andExpect(jsonPath("$.direccion").value("Calle Principal 123"));
    }

    @Test
    @DisplayName("GET /api/clientes/mi-perfil - Debe retornar 401/403 sin token")
    void testGetMiPerfil_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/clientes/mi-perfil"))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 403
    }

    @Test
    @DisplayName("GET /api/clientes/mi-perfil - Debe retornar 401/403 con token inválido")
    void testGetMiPerfil_InvalidToken() throws Exception {
        mockMvc.perform(get("/api/clientes/mi-perfil")
                .header("Authorization", "Bearer token-invalido"))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 403
    }

    @Test
    @DisplayName("GET /api/clientes/mis-citas - Debe retornar todas las citas del cliente")
    void testGetMisCitas_Success() throws Exception {
        mockMvc.perform(get("/api/clientes/mis-citas")
                .header("Authorization", "Bearer " + clienteToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].propietarioId", everyItem(is(clientePropietario.getId().intValue()))))
            .andExpect(jsonPath("$[*].id").value(hasItems(
                citaProxima.getId().intValue(),
                citaPasada.getId().intValue(),
                citaCancelada.getId().intValue()
            )));
    }

    @Test
    @DisplayName("GET /api/clientes/mis-citas - Debe retornar lista vacía si no hay citas")
    void testGetMisCitas_EmptyList() throws Exception {
        // Crear otro cliente sin citas
        Propietario otroCliente = Propietario.builder()
            .nombre("Otro Cliente")
            .email("otro@test.com")
            .password(passwordEncoder.encode("password123"))
            .activo(true)
            .build();
        otroCliente = propietarioRepository.save(otroCliente);

        UserDetails otroClienteDetails = User.builder()
            .username(otroCliente.getEmail())
            .password(otroCliente.getPassword())
            .authorities(Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE")
            ))
            .build();

        Map<String, Object> otroClienteClaims = new HashMap<>();
        otroClienteClaims.put("rol", "CLIENTE");
        otroClienteClaims.put("propietarioId", otroCliente.getId());
        String otroClienteToken = jwtUtil.generateToken(otroClienteDetails, otroClienteClaims);

        mockMvc.perform(get("/api/clientes/mis-citas")
                .header("Authorization", "Bearer " + otroClienteToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/clientes/mis-citas - Debe retornar 401/403 sin token")
    void testGetMisCitas_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/clientes/mis-citas"))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 403
    }

    @Test
    @DisplayName("GET /api/clientes/mis-mascotas - Debe retornar todas las mascotas del cliente")
    void testGetMisMascotas_Success() throws Exception {
        mockMvc.perform(get("/api/clientes/mis-mascotas")
                .header("Authorization", "Bearer " + clienteToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].propietarioId", everyItem(is(clientePropietario.getId().intValue()))))
            .andExpect(jsonPath("$[*].id").value(hasItems(
                mascota1.getId().intValue(),
                mascota2.getId().intValue()
            )))
            .andExpect(jsonPath("$[*].nombre").value(hasItems("Max", "Luna")));
    }

    @Test
    @DisplayName("GET /api/clientes/mis-mascotas - Debe retornar lista vacía si no hay mascotas")
    void testGetMisMascotas_EmptyList() throws Exception {
        // Crear otro cliente sin mascotas
        Propietario otroCliente = Propietario.builder()
            .nombre("Cliente Sin Mascotas")
            .email("sinmascotas@test.com")
            .password(passwordEncoder.encode("password123"))
            .activo(true)
            .build();
        otroCliente = propietarioRepository.save(otroCliente);

        UserDetails otroClienteDetails = User.builder()
            .username(otroCliente.getEmail())
            .password(otroCliente.getPassword())
            .authorities(Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE")
            ))
            .build();

        Map<String, Object> otroClienteClaims = new HashMap<>();
        otroClienteClaims.put("rol", "CLIENTE");
        otroClienteClaims.put("propietarioId", otroCliente.getId());
        String otroClienteToken = jwtUtil.generateToken(otroClienteDetails, otroClienteClaims);

        mockMvc.perform(get("/api/clientes/mis-mascotas")
                .header("Authorization", "Bearer " + otroClienteToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/clientes/mis-mascotas - Debe retornar 401/403 sin token")
    void testGetMisMascotas_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/clientes/mis-mascotas"))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 403
    }

    @Test
    @DisplayName("GET /api/clientes/* - Debe retornar 403/404 con token de usuario del sistema")
    void testEndpoints_WithSystemUserToken() throws Exception {
        // Intentar acceder con token de admin (usuario del sistema)
        // Nota: Puede retornar 403 (Forbidden) o 404 (Not Found) dependiendo de la configuración de seguridad
        mockMvc.perform(get("/api/clientes/mi-perfil")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().is4xxClientError()); // Acepta 403 o 404

        mockMvc.perform(get("/api/clientes/mis-citas")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().is4xxClientError()); // Acepta 403 o 404

        mockMvc.perform(get("/api/clientes/mis-mascotas")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().is4xxClientError()); // Acepta 403 o 404
    }

    @Test
    @DisplayName("GET /api/clientes/mis-citas - Debe incluir información de nombres relacionados")
    void testGetMisCitas_WithRelatedInfo() throws Exception {
        mockMvc.perform(get("/api/clientes/mis-citas")
                .header("Authorization", "Bearer " + clienteToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pacienteNombre").exists())
            .andExpect(jsonPath("$[0].profesionalNombre").exists());
    }
}

