package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.dto.CitaPublicaRequestDTO;
import com.clinica.veterinaria.service.CitaPublicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST público para agendar citas sin autenticación.
 * 
 * <p>Este controlador expone un endpoint público que permite a los clientes
 * agendar citas sin necesidad de estar autenticados. Incluye la opción de
 * registrar nuevos propietarios y pacientes automáticamente.</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>POST /api/public/citas:</b> Crea una nueva cita con registro opcional</li>
 * </ul>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>No requiere autenticación (endpoint público)</li>
 *   <li>Permite usar IDs existentes o crear nuevos registros</li>
 *   <li>Valida datos antes de crear la cita</li>
 *   <li>Registra automáticamente propietario y paciente si no existen</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see CitaPublicaService
 * @see CitaPublicaRequestDTO
 */
@RestController
@RequestMapping("/api/public/citas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CitaPublicaController {

    private final CitaPublicaService citaPublicaService;

    /**
     * Crea una nueva cita con registro opcional de propietario y paciente.
     * 
     * <p>Este endpoint permite dos formas de uso:</p>
     * <ol>
     *   <li><b>Con IDs existentes:</b> Si el cliente ya está registrado, puede usar
     *       propietarioId y pacienteId para agendar la cita directamente.</li>
     *   <li><b>Con datos nuevos:</b> Si es un cliente nuevo, puede proporcionar los datos
     *       del propietario y paciente en propietarioNuevo y pacienteNuevo, y se crearán
     *       automáticamente antes de agendar la cita.</li>
     * </ol>
     * 
     * <p><strong>Request Body (con IDs existentes):</strong></p>
     * <pre>
     * {
     *   "fecha": "2025-01-20T10:00:00",
     *   "motivo": "Control anual",
     *   "profesionalId": 1,
     *   "propietarioId": 5,
     *   "pacienteId": 10,
     *   "observaciones": "Primera visita"
     * }
     * </pre>
     * 
     * <p><strong>Request Body (con datos nuevos):</strong></p>
     * <pre>
     * {
     *   "fecha": "2025-01-20T10:00:00",
     *   "motivo": "Control anual",
     *   "profesionalId": 1,
     *   "propietarioNuevo": {
     *     "nombre": "Juan Pérez",
     *     "email": "juan@email.com",
     *     "telefono": "555-1234",
     *     "documento": "12345678"
     *   },
     *   "pacienteNuevo": {
     *     "nombre": "Max",
     *     "especie": "Perro",
     *     "raza": "Labrador",
     *     "sexo": "M",
     *     "edadMeses": 36
     *   },
     *   "observaciones": "Primera visita"
     * }
     * </pre>
     * 
     * <p><strong>Response (201 Created):</strong></p>
     * <pre>
     * {
     *   "id": 123,
     *   "fecha": "2025-01-20T10:00:00",
     *   "motivo": "Control anual",
     *   "estado": "PENDIENTE",
     *   "pacienteId": 10,
     *   "propietarioId": 5,
     *   "profesionalId": 1,
     *   ...
     * }
     * </pre>
     * 
     * <p><strong>Códigos de respuesta:</strong></p>
     * <ul>
     *   <li><b>201 Created:</b> Cita creada exitosamente</li>
     *   <li><b>400 Bad Request:</b> Datos de entrada inválidos o faltantes</li>
     *   <li><b>404 Not Found:</b> Propietario, paciente o profesional no encontrado (si se usan IDs)</li>
     * </ul>
     * 
     * @param request Datos de la solicitud de cita pública. Debe incluir fecha, motivo,
     *                profesionalId, y ya sea IDs existentes o datos nuevos de propietario/paciente.
     * @return Respuesta con los datos de la cita creada.
     * @throws RuntimeException si los datos son inválidos o alguna entidad relacionada no existe.
     */
    @PostMapping
    public ResponseEntity<CitaDTO> crearCita(@Valid @RequestBody CitaPublicaRequestDTO request) {
        log.info("POST /api/public/citas - Profesional: {}, Fecha: {}", 
            request.getProfesionalId(), request.getFecha());
        
        CitaDTO citaCreada = citaPublicaService.crearCitaPublica(request);
        
        log.info("✓ Cita pública creada exitosamente - ID: {}", citaCreada.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
    }
}

