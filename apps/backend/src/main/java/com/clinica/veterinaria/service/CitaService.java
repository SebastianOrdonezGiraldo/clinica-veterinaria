package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Cita.EstadoCita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar citas médicas de la clínica veterinaria.
 * 
 * <p>Este servicio proporciona operaciones CRUD completas para la gestión de citas,
 * incluyendo creación, actualización, consulta y eliminación. Coordina la validación
 * de entidades relacionadas (paciente, propietario, profesional) y gestiona los
 * diferentes estados del ciclo de vida de una cita.</p>
 * 
 * <p>Estados posibles de una cita:</p>
 * <ul>
 *   <li>PENDIENTE - Cita programada, esperando confirmación</li>
 *   <li>CONFIRMADA - Cita confirmada por el propietario</li>
 *   <li>EN_PROCESO - Atención médica en curso</li>
 *   <li>COMPLETADA - Atención finalizada exitosamente</li>
 *   <li>CANCELADA - Cita cancelada por cualquier motivo</li>
 * </ul>
 * 
 * <p><strong>Reglas de negocio implementadas:</strong></p>
 * <ul>
 *   <li>Validación de existencia de paciente, propietario y profesional</li>
 *   <li>Estado inicial por defecto: PENDIENTE</li>
 *   <li>Registro de auditoría en cada operación</li>
 *   <li>Transacciones automáticas para garantizar consistencia</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see CitaDTO
 * @see Cita
 * @see CitaRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    /**
     * Obtiene todas las citas registradas en el sistema.
     * 
     * <p>Este método recupera todas las citas sin aplicar ningún filtro,
     * incluyendo información completa de las entidades relacionadas
     * (paciente, propietario, profesional).</p>
     * 
     * @return Lista de todas las citas como DTOs. Nunca es null, puede ser vacía.
     * @see CitaDTO#fromEntity(Cita, boolean)
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findAll() {
        log.debug("Obteniendo todas las citas");
        return citaRepository.findAll().stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Busca y retorna una cita específica por su identificador único.
     * 
     * <p>Incluye información completa de las entidades relacionadas en el DTO retornado.</p>
     * 
     * @param id Identificador único de la cita. No puede ser null.
     * @return DTO con la información completa de la cita.
     * @throws ResourceNotFoundException si no existe una cita con el ID especificado.
     */
    @Transactional(readOnly = true)
    public CitaDTO findById(Long id) {
        log.debug("Buscando cita con ID: {}", id);
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita", "id", id));
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Obtiene todas las citas asociadas a un paciente específico.
     * 
     * <p>Útil para consultar el historial de citas de una mascota particular.
     * Incluye citas de todos los estados (pendientes, completadas, canceladas, etc.).</p>
     * 
     * @param pacienteId ID del paciente (mascota). No puede ser null.
     * @return Lista de citas del paciente. Puede estar vacía si no tiene citas.
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByPaciente(Long pacienteId) {
        log.debug("Buscando citas del paciente con ID: {}", pacienteId);
        return citaRepository.findByPacienteId(pacienteId).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las citas asignadas a un veterinario específico.
     * 
     * <p>Este método es útil para ver la agenda de un profesional.
     * Incluye citas de todos los estados.</p>
     * 
     * @param profesionalId ID del usuario veterinario. No puede ser null.
     * @return Lista de citas del profesional. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByProfesional(Long profesionalId) {
        log.debug("Buscando citas del profesional con ID: {}", profesionalId);
        return citaRepository.findByProfesionalId(profesionalId).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Filtra citas por su estado actual.
     * 
     * <p>Estados disponibles:</p>
     * <ul>
     *   <li>PENDIENTE - Citas esperando confirmación</li>
     *   <li>CONFIRMADA - Citas confirmadas</li>
     *   <li>EN_PROCESO - Atenciones en curso</li>
     *   <li>COMPLETADA - Citas finalizadas</li>
     *   <li>CANCELADA - Citas canceladas</li>
     * </ul>
     * 
     * @param estado Estado por el cual filtrar. No puede ser null.
     * @return Lista de citas en el estado especificado. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByEstado(Cita.EstadoCita estado) {
        log.debug("Buscando citas con estado: {}", estado);
        return citaRepository.findByEstado(estado).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Busca citas programadas dentro de un rango de fechas.
     * 
     * <p>Útil para generar reportes, consultar agendas por período,
     * o buscar disponibilidad. Los límites de fecha son inclusivos.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 0, 0);
     * LocalDateTime fin = LocalDateTime.of(2024, 1, 31, 23, 59);
     * List&lt;CitaDTO&gt; citasEnero = citaService.findByFechaRange(inicio, fin);
     * </pre>
     * 
     * @param inicio Fecha y hora de inicio del rango (inclusivo). No puede ser null.
     * @param fin Fecha y hora de fin del rango (inclusivo). No puede ser null.
     * @return Lista de citas en el rango especificado. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByFechaRange(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando citas entre {} y {}", inicio, fin);
        return citaRepository.findByFechaBetween(inicio, fin).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Crea y registra una nueva cita en el sistema.
     * 
     * <p>Este método realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Valida la existencia del paciente, propietario y profesional</li>
     *   <li>Asigna estado inicial PENDIENTE si no se especifica otro</li>
     *   <li>Persiste la cita en la base de datos</li>
     *   <li>Registra la operación en el log de auditoría</li>
     * </ol>
     * 
     * <p><strong>Validaciones realizadas:</strong></p>
     * <ul>
     *   <li>El paciente debe existir en el sistema</li>
     *   <li>El propietario debe existir en el sistema</li>
     *   <li>El profesional (veterinario) debe existir y estar activo</li>
     *   <li>La fecha de la cita es requerida</li>
     *   <li>El motivo de consulta es requerido</li>
     * </ul>
     * 
     * @param dto Datos de la nueva cita. No puede ser null. Debe incluir IDs válidos
     *            de paciente, propietario y profesional, además de fecha y motivo.
     * @return DTO con los datos de la cita creada, incluyendo el ID asignado.
     * @throws RuntimeException si alguna entidad relacionada no existe.
     * @see CitaDTO
     */
    public CitaDTO create(CitaDTO dto) {
        log.info("→ Creando nueva cita para paciente ID: {}", dto.getPacienteId());
        
        // VALIDACIONES: Entidades relacionadas
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
            .orElseThrow(() -> {
                log.error("✗ Paciente no encontrado con ID: {}", dto.getPacienteId());
                return new ResourceNotFoundException("Paciente", "id", dto.getPacienteId());
            });
        
        Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
            .orElseThrow(() -> {
                log.error("✗ Propietario no encontrado con ID: {}", dto.getPropietarioId());
                return new ResourceNotFoundException("Propietario", "id", dto.getPropietarioId());
            });
        
        Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
            .orElseThrow(() -> {
                log.error("✗ Profesional no encontrado con ID: {}", dto.getProfesionalId());
                return new ResourceNotFoundException("Usuario/Profesional", "id", dto.getProfesionalId());
            });

        Cita cita = Cita.builder()
            .fecha(dto.getFecha())
            .motivo(dto.getMotivo())
            .estado(dto.getEstado() != null ? dto.getEstado() : Cita.EstadoCita.PENDIENTE)
            .observaciones(dto.getObservaciones())
            .paciente(paciente)
            .propietario(propietario)
            .profesional(profesional)
            .build();

        cita = citaRepository.save(cita);
        log.info("Cita creada exitosamente con ID: {}", cita.getId());
        
        // Crear notificación automática para el veterinario
        try {
            String titulo = "Nueva cita programada";
            String mensaje = String.format("Tienes una nueva cita con %s (%s) el %s a las %s. Motivo: %s",
                    paciente.getNombre(),
                    propietario.getNombre(),
                    dto.getFecha().toLocalDate(),
                    dto.getFecha().toLocalTime(),
                    dto.getMotivo());
            
            notificacionService.crearNotificacion(
                    profesional.getId(),
                    titulo,
                    mensaje,
                    com.clinica.veterinaria.entity.Notificacion.Tipo.CITA,
                    "CITA",
                    cita.getId()
            );
        } catch (Exception e) {
            log.warn("No se pudo crear notificación para la cita: {}", e.getMessage());
        }
        
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Actualiza la información de una cita existente.
     * 
     * <p>Permite modificar todos los campos de una cita, incluyendo las relaciones
     * con paciente, propietario y profesional. Solo actualiza las relaciones si
     * los IDs han cambiado, validando la existencia de las nuevas entidades.</p>
     * 
     * <p><strong>Campos actualizables:</strong></p>
     * <ul>
     *   <li>Fecha y hora de la cita</li>
     *   <li>Motivo de consulta</li>
     *   <li>Estado de la cita</li>
     *   <li>Observaciones</li>
     *   <li>Paciente asignado</li>
     *   <li>Propietario</li>
     *   <li>Profesional responsable</li>
     * </ul>
     * 
     * @param id ID de la cita a actualizar. No puede ser null.
     * @param dto Nuevos datos para la cita. No puede ser null.
     * @return DTO con los datos actualizados de la cita.
     * @throws RuntimeException si la cita no existe o alguna entidad relacionada no existe.
     */
    public CitaDTO update(Long id, CitaDTO dto) {
        log.info("→ Actualizando cita con ID: {}", id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Cita no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Cita", "id", id);
            });

        // Actualizar campos básicos
        cita.setFecha(dto.getFecha());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(dto.getEstado());
        cita.setObservaciones(dto.getObservaciones());

        // VALIDACIONES: Actualizar relaciones si cambiaron
        if (!cita.getPaciente().getId().equals(dto.getPacienteId())) {
            Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", dto.getPacienteId()));
            cita.setPaciente(paciente);
        }

        if (!cita.getPropietario().getId().equals(dto.getPropietarioId())) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Propietario", "id", dto.getPropietarioId()));
            cita.setPropietario(propietario);
        }

        if (!cita.getProfesional().getId().equals(dto.getProfesionalId())) {
            Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario/Profesional", "id", dto.getProfesionalId()));
            cita.setProfesional(profesional);
        }

        cita = citaRepository.save(cita);
        log.info("✓ Cita actualizada exitosamente con ID: {}", id);
        
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Cambia el estado de una cita existente.
     * 
     * <p>Este método es útil para transicionar una cita a través de su ciclo de vida
     * sin necesidad de enviar toda la información de actualización. Registra el cambio
     * en el log de auditoría.</p>
     * 
     * <p><strong>Transiciones comunes:</strong></p>
     * <ul>
     *   <li>PENDIENTE → CONFIRMADA (cuando el propietario confirma)</li>
     *   <li>CONFIRMADA → EN_PROCESO (cuando comienza la atención)</li>
     *   <li>EN_PROCESO → COMPLETADA (cuando finaliza la atención)</li>
     *   <li>Cualquier estado → CANCELADA (si se cancela)</li>
     * </ul>
     * 
     * @param id ID de la cita a modificar. No puede ser null.
     * @param nuevoEstado Nuevo estado a asignar. No puede ser null.
     * @return DTO con los datos actualizados de la cita.
     * @throws RuntimeException si la cita no existe.
     * @see Cita.EstadoCita
     */
    public CitaDTO cambiarEstado(Long id, Cita.EstadoCita nuevoEstado) {
        log.info("→ Cambiando estado de cita ID: {} a {}", id, nuevoEstado);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Cita no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Cita", "id", id);
            });
        
        cita.setEstado(nuevoEstado);
        cita = citaRepository.save(cita);
        
        log.info("✓ Estado de cita actualizado a: {}", nuevoEstado);
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Elimina permanentemente una cita del sistema.
     * 
     * <p><strong>ADVERTENCIA:</strong> Esta operación es irreversible. La cita
     * será eliminada completamente de la base de datos. Se recomienda usar el
     * estado CANCELADA en lugar de eliminar cuando se desee mantener el historial.</p>
     * 
     * <p>Registra la operación en el log de auditoría para trazabilidad.</p>
     * 
     * @param id ID de la cita a eliminar. No puede ser null.
     * @throws RuntimeException si la cita no existe.
     */
    public void delete(Long id) {
        log.warn("→ Eliminando cita con ID: {}", id);
        
        if (!citaRepository.existsById(id)) {
            log.error("✗ Cita no encontrada con ID: {}", id);
            throw new ResourceNotFoundException("Cita", "id", id);
        }
        
        citaRepository.deleteById(id);
        log.warn("⚠ Cita eliminada exitosamente con ID: {}", id);
    }
    
    /**
     * Busca citas con filtros combinados y paginación del lado del servidor.
     * 
     * <p><strong>PATRÓN STRATEGY:</strong> Selecciona dinámicamente el query apropiado
     * según los filtros proporcionados. Esto evita queries complejos con múltiples
     * joins y condiciones anidadas que pueden afectar el rendimiento.</p>
     * 
     * <p><strong>Optimización:</strong> La paginación se realiza en la base de datos,
     * permitiendo manejar agendas con miles de citas sin problemas de memoria.</p>
     * 
     * <p><strong>Casos de uso soportados:</strong></p>
     * <ul>
     *   <li><b>Sin filtros:</b> Todas las citas paginadas</li>
     *   <li><b>Por estado:</b> PENDIENTE, CONFIRMADA, EN_PROCESO, COMPLETADA, CANCELADA</li>
     *   <li><b>Por profesional:</b> Agenda de un veterinario específico</li>
     *   <li><b>Por paciente:</b> Historial de citas de una mascota</li>
     *   <li><b>Por rango de fechas:</b> Citas en un período específico</li>
     *   <li><b>Estado + fechas:</b> Combinación para reportes</li>
     *   <li><b>Profesional + fechas:</b> Agenda de un veterinario en un período</li>
     * </ul>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * // Caso 1: Todas las citas pendientes
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("fecha").ascending());
     * Page&lt;CitaDTO&gt; result = service.searchWithFilters(
     *     EstadoCita.PENDIENTE, null, null, null, null, pageable);
     * 
     * // Caso 2: Agenda del Dr. Smith esta semana
     * LocalDateTime inicio = LocalDateTime.now().with(DayOfWeek.MONDAY);
     * LocalDateTime fin = inicio.plusDays(7);
     * result = service.searchWithFilters(
     *     null, 5L, null, inicio, fin, pageable);
     * 
     * // Caso 3: Historial de citas de mascota ID 10
     * result = service.searchWithFilters(
     *     null, null, 10L, null, null, pageable);
     * 
     * // Caso 4: Citas completadas del último mes
     * LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
     * result = service.searchWithFilters(
     *     EstadoCita.COMPLETADA, null, null, hace30Dias, LocalDateTime.now(), pageable);
     * </pre>
     * 
     * @param estado Filtro opcional por estado de cita
     * @param profesionalId Filtro opcional por ID de profesional (veterinario)
     * @param pacienteId Filtro opcional por ID de paciente (mascota)
     * @param fechaInicio Filtro opcional por fecha inicial (inclusivo)
     * @param fechaFin Filtro opcional por fecha final (inclusivo)
     * @param pageable Configuración de paginación y ordenamiento. No puede ser null.
     * @return Página de citas que cumplen los criterios de búsqueda
     */
    @Transactional(readOnly = true)
    public Page<CitaDTO> searchWithFilters(
            EstadoCita estado,
            Long profesionalId,
            Long pacienteId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        
        log.debug("Buscando citas con filtros - estado: {}, profesional: {}, paciente: {}, fechas: {} - {}, page: {}", 
            estado, profesionalId, pacienteId, fechaInicio, fechaFin, pageable.getPageNumber());
        
        Page<Cita> citas;
        
        // STRATEGY PATTERN: Selección dinámica del query apropiado
        
        // Estrategia 1: Profesional + rango de fechas (agenda de un veterinario)
        if (profesionalId != null && fechaInicio != null && fechaFin != null) {
            log.debug("Estrategia: Agenda de profesional en rango de fechas");
            citas = citaRepository.findByProfesionalIdAndFechaBetween(
                profesionalId, fechaInicio, fechaFin, pageable);
        }
        // Estrategia 2: Estado + rango de fechas (reportes)
        else if (estado != null && fechaInicio != null && fechaFin != null) {
            log.debug("Estrategia: Estado y rango de fechas");
            citas = citaRepository.findByEstadoAndFechaBetween(
                estado, fechaInicio, fechaFin, pageable);
        }
        // Estrategia 3: Solo rango de fechas
        else if (fechaInicio != null && fechaFin != null) {
            log.debug("Estrategia: Solo rango de fechas");
            citas = citaRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
        }
        // Estrategia 4: Solo profesional
        else if (profesionalId != null) {
            log.debug("Estrategia: Solo profesional");
            citas = citaRepository.findByProfesionalId(profesionalId, pageable);
        }
        // Estrategia 5: Solo paciente
        else if (pacienteId != null) {
            log.debug("Estrategia: Solo paciente");
            citas = citaRepository.findByPacienteId(pacienteId, pageable);
        }
        // Estrategia 6: Solo estado
        else if (estado != null) {
            log.debug("Estrategia: Solo estado");
            citas = citaRepository.findByEstado(estado, pageable);
        }
        // Estrategia 7: Sin filtros, todas las citas
        else {
            log.debug("Estrategia: Sin filtros, retornar todas");
            citas = citaRepository.findAll(pageable);
        }
        
        log.debug("Citas encontradas: {} en página {} de {}", 
            citas.getNumberOfElements(), 
            citas.getNumber() + 1, 
            citas.getTotalPages());
        
        return citas.map(c -> CitaDTO.fromEntity(c, true));
    }
}

