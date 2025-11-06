package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PropietarioController
 */
@DisplayName("Tests de Integración - PropietarioController")
class PropietarioControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Propietario propietario1;

    @BeforeEach
    void setUp() {
        // Limpiar propietarios
        propietarioRepository.deleteAll();

        // Crear propietario de prueba
        propietario1 = Propietario.builder()
            .nombre("Juan Pérez")
            .documento("12345678")
            .email("juan@email.com")
            .telefono("555-1234")
            .direccion("Calle 123")
            .activo(true)
            .build();
        propietario1 = propietarioRepository.save(propietario1);
    }

    @Test
    @DisplayName("GET /api/propietarios - Debe listar propietarios con token ADMIN")
    void testListarPropietarios_ConTokenAdmin() throws Exception {
        mockMvc.perform(get("/api/propietarios")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("GET /api/propietarios - Debe retornar 401/403 sin token")
    void testListarPropietarios_SinToken() throws Exception {
        mockMvc.perform(get("/api/propietarios"))
            .andExpect(status().is4xxClientError()); // Acepta 401 o 403
    }

    @Test
    @DisplayName("GET /api/propietarios/{id} - Debe obtener propietario por ID")
    void testObtenerPropietarioPorId() throws Exception {
        mockMvc.perform(get("/api/propietarios/" + propietario1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
            .andExpect(jsonPath("$.email").value("juan@email.com"))
            .andExpect(jsonPath("$.documento").value("12345678"));
    }

    @Test
    @DisplayName("GET /api/propietarios/{id} - Debe retornar 404 para ID inexistente")
    void testObtenerPropietarioPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/propietarios/99999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/propietarios - Debe crear propietario con token ADMIN")
    void testCrearPropietario() throws Exception {
        PropietarioDTO nuevoDTO = PropietarioDTO.builder()
            .nombre("María García")
            .documento("87654321")
            .email("maria@email.com")
            .telefono("555-5678")
            .direccion("Avenida 456")
            .build();

        mockMvc.perform(post("/api/propietarios")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevoDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("María García"))
            .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    @DisplayName("POST /api/propietarios - Debe validar datos requeridos")
    void testCrearPropietario_DatosInvalidos() throws Exception {
        PropietarioDTO dtoInvalido = PropietarioDTO.builder()
            .nombre("") // Nombre vacío
            .email("invalido") // Email inválido
            .build();

        mockMvc.perform(post("/api/propietarios")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dtoInvalido)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/propietarios/{id} - Debe actualizar propietario")
    void testActualizarPropietario() throws Exception {
        PropietarioDTO actualizacionDTO = PropietarioDTO.builder()
            .nombre("Juan Pérez Actualizado")
            .documento("12345678")
            .email("juan.nuevo@email.com")
            .telefono("555-9999")
            .direccion("Nueva Dirección")
            .activo(true)
            .build();

        mockMvc.perform(put("/api/propietarios/" + propietario1.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacionDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
            .andExpect(jsonPath("$.email").value("juan.nuevo@email.com"));
    }

    @Test
    @DisplayName("DELETE /api/propietarios/{id} - Debe eliminar con token ADMIN")
    void testEliminarPropietario_ConAdmin() throws Exception {
        mockMvc.perform(delete("/api/propietarios/" + propietario1.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/propietarios/{id} - Debe denegar con token VET")
    void testEliminarPropietario_ConVet() throws Exception {
        mockMvc.perform(delete("/api/propietarios/" + propietario1.getId())
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/propietarios/buscar - Debe buscar por nombre")
    void testBuscarPropietarios() throws Exception {
        mockMvc.perform(get("/api/propietarios/buscar")
                .param("nombre", "Juan")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].nombre", containsString("Juan")));
    }
}

