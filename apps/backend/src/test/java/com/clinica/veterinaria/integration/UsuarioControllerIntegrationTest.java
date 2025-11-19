package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.ResetPasswordDTO;
import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioUpdateDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para UsuarioController
 */
@DisplayName("Tests de Integración - UsuarioController")
class UsuarioControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("GET /api/usuarios - Debe listar usuarios (solo ADMIN)")
    void testListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3)))) // admin, vet, recepcion
            .andExpect(jsonPath("$[?(@.email == 'admin@test.com')]").exists());
    }

    @Test
    @DisplayName("GET /api/usuarios - Debe rechazar acceso sin rol ADMIN")
    void testListarUsuarios_SinPermiso() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Debe obtener usuario por ID")
    void testObtenerUsuarioPorId() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + vetUser.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(vetUser.getId()))
            .andExpect(jsonPath("$.email").value("vet@test.com"))
            .andExpect(jsonPath("$.rol").value("VET"))
            .andExpect(jsonPath("$.password").doesNotExist()); // Password no debe exponerse
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Debe retornar 404 si usuario no existe")
    void testObtenerUsuarioPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/usuarios/999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/usuarios/email/{email} - Debe buscar usuario por email")
    void testBuscarPorEmail() throws Exception {
        mockMvc.perform(get("/api/usuarios/email/vet@test.com")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("vet@test.com"))
            .andExpect(jsonPath("$.rol").value("VET"));
    }

    @Test
    @DisplayName("GET /api/usuarios/veterinarios - Debe listar veterinarios activos")
    void testListarVeterinarios() throws Exception {
        mockMvc.perform(get("/api/usuarios/veterinarios")
                .header("Authorization", "Bearer " + vetToken)) // Cualquier usuario autenticado
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[?(@.rol == 'VET')]").exists())
            .andExpect(jsonPath("$[?(@.activo == true)]").exists());
    }

    @Test
    @DisplayName("GET /api/usuarios/search - Debe buscar con filtros")
    void testBuscarConFiltros() throws Exception {
        mockMvc.perform(get("/api/usuarios/search")
                .param("rol", "VET")
                .param("activo", "true")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.content[?(@.rol == 'VET')]").exists());
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe crear nuevo usuario")
    void testCrearUsuario() throws Exception {
        UsuarioCreateDTO nuevoUsuario = UsuarioCreateDTO.builder()
            .nombre("Nuevo Veterinario")
            .email("nuevo@test.com")
            .password("nuevo123")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevoUsuario)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("nuevo@test.com"))
            .andExpect(jsonPath("$.rol").value("VET"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe rechazar email duplicado")
    void testCrearUsuario_EmailDuplicado() throws Exception {
        UsuarioCreateDTO usuarioDuplicado = UsuarioCreateDTO.builder()
            .nombre("Otro Admin")
            .email("admin@test.com") // Email ya existe
            .password("password123")
            .rol(Usuario.Rol.ADMIN)
            .activo(true)
            .build();

        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(usuarioDuplicado)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Debe actualizar usuario")
    void testActualizarUsuario() throws Exception {
        UsuarioUpdateDTO actualizacion = UsuarioUpdateDTO.builder()
            .nombre("Veterinario Actualizado")
            .email("vet@test.com")
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();

        mockMvc.perform(put("/api/usuarios/" + vetUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Veterinario Actualizado"))
            .andExpect(jsonPath("$.id").value(vetUser.getId()));
    }

    @Test
    @DisplayName("PUT /api/usuarios/me - Debe actualizar perfil propio")
    void testActualizarPerfilPropio() throws Exception {
        UsuarioUpdateDTO actualizacion = UsuarioUpdateDTO.builder()
            .nombre("Mi Nombre Actualizado")
            .email("vet@test.com")
            .rol(Usuario.Rol.VET) // Incluir rol aunque no cambie
            .activo(true) // Incluir activo aunque no cambie
            .build();

        mockMvc.perform(put("/api/usuarios/me")
                .header("Authorization", "Bearer " + vetToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(actualizacion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Mi Nombre Actualizado"))
            .andExpect(jsonPath("$.rol").value("VET")); // El rol no debe cambiar
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Debe desactivar usuario")
    void testEliminarUsuario() throws Exception {
        // Crear usuario temporal para eliminar
        Usuario usuarioTemp = Usuario.builder()
            .nombre("Usuario Temporal")
            .email("temp@test.com")
            .password(passwordEncoder.encode("temp123"))
            .rol(Usuario.Rol.RECEPCION)
            .activo(true)
            .build();
        usuarioTemp = usuarioRepository.save(usuarioTemp);

        mockMvc.perform(delete("/api/usuarios/" + usuarioTemp.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        // Verificar que fue desactivado
        mockMvc.perform(get("/api/usuarios/" + usuarioTemp.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @DisplayName("POST /api/usuarios/{id}/reset-password - Debe resetear contraseña")
    void testResetearContrasena() throws Exception {
        ResetPasswordDTO resetDTO = ResetPasswordDTO.builder()
            .password("nuevaPassword123")
            .build();

        mockMvc.perform(post("/api/usuarios/" + vetUser.getId() + "/reset-password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(resetDTO)))
            .andExpect(status().isNoContent());

        // Verificar que la contraseña fue cambiada intentando login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"vet@test.com\",\"password\":\"nuevaPassword123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("GET /api/usuarios - Debe requerir autenticación")
    void testRequerirAutenticacion() throws Exception {
        // Spring Security puede retornar 403 (Forbidden) o 401 (Unauthorized) dependiendo de la configuración
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().is4xxClientError()); // Acepta cualquier error 4xx (401 o 403)
    }
}

