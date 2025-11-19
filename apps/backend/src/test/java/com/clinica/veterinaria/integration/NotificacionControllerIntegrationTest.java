package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.dto.NotificacionCreateDTO;
import com.clinica.veterinaria.entity.Notificacion;
import com.clinica.veterinaria.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para NotificacionController
 */
@DisplayName("Tests de Integración - NotificacionController")
class NotificacionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificacionRepository notificacionRepository;

    private Notificacion notificacion1;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        notificacionRepository.deleteAll();

        // Crear notificación para el usuario veterinario
        notificacion1 = Notificacion.builder()
            .titulo("Nueva cita programada")
            .mensaje("Tienes una nueva cita programada para mañana")
            .tipo(Notificacion.Tipo.CITA)
            .leida(false)
            .usuario(vetUser)
            .build();
        notificacion1 = notificacionRepository.save(notificacion1);
    }

    @Test
    @DisplayName("GET /api/notificaciones - Debe obtener notificaciones del usuario autenticado")
    void testObtenerMisNotificaciones() throws Exception {
        mockMvc.perform(get("/api/notificaciones")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].titulo").value("Nueva cita programada"));
    }

    @Test
    @DisplayName("GET /api/notificaciones/no-leidas - Debe obtener notificaciones no leídas")
    void testObtenerNoLeidas() throws Exception {
        mockMvc.perform(get("/api/notificaciones/no-leidas")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    @DisplayName("GET /api/notificaciones/no-leidas/count - Debe contar notificaciones no leídas")
    void testContarNoLeidas() throws Exception {
        mockMvc.perform(get("/api/notificaciones/no-leidas/count")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/notificaciones - Debe crear nueva notificación")
    void testCrearNotificacion() throws Exception {
        NotificacionCreateDTO nuevaNotificacion = NotificacionCreateDTO.builder()
            .usuarioId(vetUser.getId())
            .titulo("Recordatorio importante")
            .mensaje("No olvides la cita de hoy")
            .tipo(Notificacion.Tipo.RECORDATORIO)
            .build();

        mockMvc.perform(post("/api/notificaciones")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nuevaNotificacion)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.titulo").value("Recordatorio importante"))
            .andExpect(jsonPath("$.leida").value(false))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("PUT /api/notificaciones/{id}/leer - Debe marcar notificación como leída")
    void testMarcarComoLeida() throws Exception {
        mockMvc.perform(put("/api/notificaciones/" + notificacion1.getId() + "/leer")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isNoContent());

        // Verificar que fue marcada como leída - ya no debe aparecer en no leídas
        mockMvc.perform(get("/api/notificaciones/no-leidas")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + notificacion1.getId() + ")]").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/notificaciones/leer-todas - Debe marcar todas como leídas")
    void testMarcarTodasComoLeidas() throws Exception {
        mockMvc.perform(put("/api/notificaciones/leer-todas")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isNoContent());

        // Verificar que no hay no leídas
        mockMvc.perform(get("/api/notificaciones/no-leidas/count")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0L));
    }

    @Test
    @DisplayName("DELETE /api/notificaciones/{id} - Debe eliminar notificación")
    void testEliminarNotificacion() throws Exception {
        Long idEliminar = notificacion1.getId();
        
        mockMvc.perform(delete("/api/notificaciones/" + idEliminar)
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isNoContent());

        // Verificar que fue eliminada - no debe aparecer en la lista
        mockMvc.perform(get("/api/notificaciones")
                .header("Authorization", "Bearer " + vetToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + idEliminar + ")]").doesNotExist());
    }
}

