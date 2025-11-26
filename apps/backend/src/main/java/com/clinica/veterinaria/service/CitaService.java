package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Cita.EstadoCita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

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
    private final EmailService emailService;
    private final SMSService smsService;
    
    // Configuración de horarios de atención
    private static final LocalTime HORARIO_INICIO = LocalTime.of(8, 0);  // 8:00 AM
    private static final LocalTime HORARIO_FIN = LocalTime.of(17, 0);     // 5:00 PM
    private static final int DURACION_CITA_MINUTOS = 30;                  // Duración estándar
    
    // Mensajes de log constantes
    private static final String MSG_CITA_NO_ENCONTRADA = "✗ Cita no encontrada con ID: {}";

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
            .toList();
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
    public CitaDTO findById(@NonNull Long id) {
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
    public List<CitaDTO> findByPaciente(@NonNull Long pacienteId) {
        log.debug("Buscando citas del paciente con ID: {}", pacienteId);
        return citaRepository.findByPacienteId(pacienteId).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .toList();
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
    public List<CitaDTO> findByProfesional(@NonNull Long profesionalId) {
        log.debug("Buscando citas del profesional con ID: {}", profesionalId);
        return citaRepository.findByProfesionalId(profesionalId).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .toList();
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
    public List<CitaDTO> findByEstado(@NonNull Cita.EstadoCita estado) {
        log.debug("Buscando citas con estado: {}", estado);
        return citaRepository.findByEstado(estado).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .toList();
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
    public List<CitaDTO> findByFechaRange(@NonNull LocalDateTime inicio, @NonNull LocalDateTime fin) {
        log.debug("Buscando citas entre {} y {}", inicio, fin);
        return citaRepository.findByFechaBetween(inicio, fin).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .toList();
    }

    /**
     * Valida las reglas de negocio para una cita.
     * 
     * <p>Realiza múltiples validaciones:</p>
     * <ul>
     *   <li>La fecha no puede ser en el pasado</li>
     *   <li>La cita debe estar dentro del horario de atención</li>
     *   <li>No puede haber solapamiento con otras citas del mismo profesional</li>
     *   <li>El paciente debe pertenecer al propietario especificado</li>
     * </ul>
     * 
     * @param fecha Fecha y hora de la cita
     * @param profesionalId ID del profesional
     * @param paciente Entidad del paciente
     * @param propietario Entidad del propietario
     * @param citaId ID de la cita (null para creación, ID para actualización)
     * @throws BusinessException si alguna validación falla
     */
    private void validarReglasDeNegocio(@NonNull LocalDateTime fecha, @NonNull Long profesionalId, 
                                         @NonNull Paciente paciente, @NonNull Propietario propietario, 
                                         Long citaId) {
        // 1. Validar que la fecha no sea en el pasado
        if (fecha.isBefore(LocalDateTime.now())) {
            log.error("✗ La fecha de la cita no puede ser en el pasado: {}", fecha);
            throw new BusinessException(
                "No se puede agendar una cita en el pasado. Fecha proporcionada: " + fecha);
        }
        
        // 2. Validar horario de atención (lunes a viernes, 8 AM - 6 PM)
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            log.error("✗ No se atiende los fines de semana: {}", fecha);
            throw new BusinessException(
                "La clínica no atiende los fines de semana. Por favor, seleccione un día hábil.");
        }
        
        LocalTime hora = fecha.toLocalTime();
        if (hora.isBefore(HORARIO_INICIO) || hora.isAfter(HORARIO_FIN)) {
            log.error("✗ Horario fuera de atención: {}", hora);
            throw new BusinessException(
                String.format("La cita debe estar dentro del horario de atención (%s - %s). Hora seleccionada: %s",
                    HORARIO_INICIO, HORARIO_FIN, hora));
        }
        
        // 3. Validar que no haya solapamiento de citas para el mismo profesional
        validarDisponibilidadProfesional(fecha, profesionalId, citaId);
        
        // 4. Validar que el paciente pertenezca al propietario
        if (!paciente.getPropietario().getId().equals(propietario.getId())) {
            log.error("✗ El paciente {} no pertenece al propietario {}", 
                paciente.getId(), propietario.getId());
            throw new BusinessException(
                String.format("El paciente '%s' no pertenece al propietario '%s'", 
                    paciente.getNombre(), propietario.getNombre()));
        }
    }
    
    /**
     * Valida que el profesional esté disponible en el horario solicitado.
     * 
     * <p>Busca citas existentes del mismo profesional en un rango de ±30 minutos
     * para detectar solapamientos.</p>
     * 
     * @param fecha Fecha y hora de la cita a validar
     * @param profesionalId ID del profesional
     * @param citaId ID de la cita actual (null para creación, se excluye en actualización)
     * @throws BusinessException si hay solapamiento
     */
    private void validarDisponibilidadProfesional(@NonNull LocalDateTime fecha, @NonNull Long profesionalId, Long citaId) {
        LocalDateTime inicio = fecha.minusMinutes(DURACION_CITA_MINUTOS);
        LocalDateTime fin = fecha.plusMinutes(DURACION_CITA_MINUTOS);
        
        List<Cita> citasSolapadas = citaRepository
            .findByProfesionalIdAndFechaBetween(profesionalId, inicio, fin)
            .stream()
            .filter(c -> !c.getEstado().equals(EstadoCita.CANCELADA))  // Ignorar canceladas
            .filter(c -> citaId == null || !c.getId().equals(citaId))  // Excluir la misma cita en actualización
            .toList();
        
        if (!citasSolapadas.isEmpty()) {
            Cita citaExistente = citasSolapadas.get(0);
            log.error("✗ El profesional ya tiene una cita a las {}", citaExistente.getFecha());
            throw new BusinessException(
                String.format("El profesional ya tiene una cita programada a las %s. " +
                    "Por favor, seleccione otro horario.",
                    citaExistente.getFecha().toLocalTime()));
        }
    }
    
    /**
     * Crea y registra una nueva cita en el sistema.
     * 
     * <p>Este método realiza las siguientes operaciones:</p>
     * <ol>
     *   <li>Valida la existencia del paciente, propietario y profesional</li>
     *   <li>Valida las reglas de negocio (horarios, disponibilidad, etc.)</li>
     *   <li>Asigna estado inicial PENDIENTE si no se especifica otro</li>
     *   <li>Persiste la cita en la base de datos</li>
     *   <li>Crea notificación automática para el veterinario</li>
     *   <li>Registra la operación en el log de auditoría</li>
     * </ol>
     * 
     * <p><strong>Validaciones realizadas:</strong></p>
     * <ul>
     *   <li>El paciente debe existir en el sistema y estar activo</li>
     *   <li>El propietario debe existir en el sistema</li>
     *   <li>El profesional (veterinario) debe existir y estar activo</li>
     *   <li>El paciente debe pertenecer al propietario</li>
     *   <li>La fecha no puede ser en el pasado</li>
     *   <li>La cita debe estar en horario de atención (L-V, 8AM-5PM)</li>
     *   <li>No debe haber solapamiento con otras citas del profesional</li>
     * </ul>
     * 
     * @param dto Datos de la nueva cita. No puede ser null. Debe incluir IDs válidos
     *            de paciente, propietario y profesional, además de fecha y motivo.
     * @return DTO con los datos de la cita creada, incluyendo el ID asignado.
     * @throws ResourceNotFoundException si alguna entidad relacionada no existe.
     * @throws BusinessException si alguna validación de negocio falla.
     * @see CitaDTO
     */
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public CitaDTO create(@NonNull CitaDTO dto) {
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
        
        // VALIDACIONES DE NEGOCIO
        validarReglasDeNegocio(dto.getFecha(), dto.getProfesionalId(), 
                               paciente, propietario, null);

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
        
        // Enviar email de confirmación al propietario
        if (propietario.getEmail() != null && !propietario.getEmail().trim().isEmpty()) {
            try {
                boolean emailEnviado = emailService.enviarEmailConfirmacionCita(
                        propietario.getEmail(),
                        propietario.getNombre(),
                        paciente.getNombre(),
                        dto.getFecha(),
                        dto.getMotivo(),
                        profesional.getNombre()
                );
                if (emailEnviado) {
                    log.info("✓ Email de confirmación enviado exitosamente a: {}", propietario.getEmail());
                } else {
                    log.warn("✗ No se pudo enviar email de confirmación a: {}", propietario.getEmail());
                }
            } catch (Exception e) {
                log.error("✗ Error al enviar email de confirmación: {}", e.getMessage(), e);
                // No lanzar excepción para no interrumpir el flujo principal
            }
        } else {
            log.debug("Propietario sin email, no se envía confirmación por correo");
        }
        
        // Enviar SMS de confirmación al propietario (si está habilitado)
        if (propietario.getTelefono() != null && !propietario.getTelefono().trim().isEmpty()) {
            try {
                String telefonoNormalizado = smsService.normalizePhoneNumber(propietario.getTelefono());
                boolean smsEnviado = smsService.sendCitaConfirmacionSMS(
                        telefonoNormalizado,
                        propietario.getNombre(),
                        paciente.getNombre(),
                        dto.getFecha(),
                        dto.getMotivo()
                );
                if (smsEnviado) {
                    log.info("✓ SMS de confirmación enviado a: {}", telefonoNormalizado);
                } else {
                    log.debug("SMS no enviado (puede estar deshabilitado o sin configuración)");
                }
            } catch (Exception e) {
                log.error("Error al enviar SMS de confirmación: {}", e.getMessage());
                // No lanzar excepción para no interrumpir el flujo principal
            }
        } else {
            log.debug("Propietario sin teléfono, no se envía confirmación por SMS");
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
     * <p><strong>Validaciones aplicadas:</strong></p>
     * <ul>
     *   <li>Mismas validaciones de negocio que en creación</li>
     *   <li>La fecha no puede ser en el pasado</li>
     *   <li>Debe estar en horario de atención (L-V, 8AM-5PM)</li>
     *   <li>No debe haber solapamiento con otras citas del profesional</li>
     *   <li>El paciente debe pertenecer al propietario</li>
     * </ul>
     * 
     * @param id ID de la cita a actualizar. No puede ser null.
     * @param dto Nuevos datos para la cita. No puede ser null.
     * @return DTO con los datos actualizados de la cita.
     * @throws ResourceNotFoundException si la cita no existe o alguna entidad relacionada no existe.
     * @throws BusinessException si alguna validación de negocio falla.
     */
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public CitaDTO update(@NonNull Long id, @NonNull CitaDTO dto) {
        log.info("→ Actualizando cita con ID: {}", id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> {
                log.error(MSG_CITA_NO_ENCONTRADA, id);
                return new ResourceNotFoundException("Cita", "id", id);
            });

        // Obtener entidades (las actuales o las nuevas si cambiaron)
        // Validar que los IDs no sean null antes de comparar
        if (dto.getPacienteId() == null) {
            throw new InvalidDataException("pacienteId", null, "El ID del paciente es requerido");
        }
        if (dto.getPropietarioId() == null) {
            throw new InvalidDataException("propietarioId", null, "El ID del propietario es requerido");
        }
        if (dto.getProfesionalId() == null) {
            throw new InvalidDataException("profesionalId", null, "El ID del profesional es requerido");
        }
        
        Paciente paciente = Objects.equals(dto.getPacienteId(), cita.getPaciente().getId())
            ? cita.getPaciente()
            : pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", dto.getPacienteId()));
        
        Propietario propietario = Objects.equals(dto.getPropietarioId(), cita.getPropietario().getId())
            ? cita.getPropietario()
            : propietarioRepository.findById(dto.getPropietarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Propietario", "id", dto.getPropietarioId()));
        
        Usuario profesional = Objects.equals(dto.getProfesionalId(), cita.getProfesional().getId())
            ? cita.getProfesional()
            : usuarioRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario/Profesional", "id", dto.getProfesionalId()));
        
        // VALIDACIONES DE NEGOCIO (incluyendo el ID de la cita para excluirla del solapamiento)
        validarReglasDeNegocio(dto.getFecha(), dto.getProfesionalId(), 
                               paciente, propietario, id);

        // Guardar valores anteriores para detectar cambios
        Cita.EstadoCita estadoAnterior = cita.getEstado();
        LocalDateTime fechaAnterior = cita.getFecha();
        String motivoAnterior = cita.getMotivo();

        // Actualizar todos los campos
        cita.setFecha(dto.getFecha());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(dto.getEstado());
        cita.setObservaciones(dto.getObservaciones());
        cita.setPaciente(paciente);
        cita.setPropietario(propietario);
        cita.setProfesional(profesional);

        cita = citaRepository.save(cita);
        log.info("✓ Cita actualizada exitosamente con ID: {}", id);
        
        // Detectar cambios importantes para notificar al propietario
        boolean fechaCambio = !fechaAnterior.equals(dto.getFecha());
        boolean motivoCambio = !Objects.equals(motivoAnterior, dto.getMotivo());
        boolean estadoCambio = estadoAnterior != dto.getEstado();
        
        log.info("📊 Cambios detectados en cita ID {}: Estado={}, Fecha={}, Motivo={}", 
            id, estadoCambio, fechaCambio, motivoCambio);
        
        // Enviar correos según los cambios
        if (estadoCambio) {
            // Cambio de estado: enviar email de actualización de estado
            enviarEmailPorCambioDeEstado(cita, estadoAnterior, dto.getEstado());
        } else if (fechaCambio || motivoCambio) {
            // Cambio de fecha o motivo sin cambio de estado: enviar email de actualización
            if (propietario != null && propietario.getEmail() != null && !propietario.getEmail().trim().isEmpty()) {
                try {
                    boolean emailEnviado = emailService.enviarEmailCambioEstadoCita(
                        propietario.getEmail(),
                        propietario.getNombre(),
                        cita.getPaciente().getNombre(),
                        cita.getFecha(),
                        cita.getMotivo(),
                        cita.getProfesional().getNombre(),
                        "ACTUALIZADA"
                    );
                    if (emailEnviado) {
                        log.info("✓ Email de actualización enviado a: {}", propietario.getEmail());
                    } else {
                        log.warn("No se pudo enviar email de actualización a: {}", propietario.getEmail());
                    }
                } catch (Exception e) {
                    log.error("Error al enviar email de actualización: {}", e.getMessage(), e);
                }
            }
        }
        
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
    public CitaDTO cambiarEstado(@NonNull Long id, @NonNull Cita.EstadoCita nuevoEstado) {
        log.info("🔄 Cambiando estado de cita ID: {} a {}", id, nuevoEstado);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> {
                log.error(MSG_CITA_NO_ENCONTRADA, id);
                return new ResourceNotFoundException("Cita", "id", id);
            });
        
        // Guardar el estado anterior para detectar cambios
        Cita.EstadoCita estadoAnterior = cita.getEstado();
        
        // Verificar si realmente hay un cambio
        if (estadoAnterior == nuevoEstado) {
            log.info("ℹ️ El estado de la cita ID {} ya es {}. No se realiza ningún cambio.", id, nuevoEstado);
            return CitaDTO.fromEntity(cita, true);
        }
        
        log.info("📝 Estado anterior: {} → Nuevo estado: {}", estadoAnterior, nuevoEstado);
        
        cita.setEstado(nuevoEstado);
        cita = citaRepository.save(cita);
        
        log.info("✅ Estado de cita ID {} actualizado exitosamente: {} → {}", id, estadoAnterior, nuevoEstado);
        
        // Enviar correo cuando el estado cambia
        enviarEmailPorCambioDeEstado(cita, estadoAnterior, nuevoEstado);
        
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
    public void delete(@NonNull Long id) {
        log.warn("→ Eliminando cita con ID: {}", id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> {
                log.error(MSG_CITA_NO_ENCONTRADA, id);
                return new ResourceNotFoundException("Cita", "id", id);
            });
        
        // Obtener información antes de eliminar para enviar email
        Propietario propietario = cita.getPropietario();
        
        // Enviar email de cancelación antes de eliminar
        if (propietario != null && propietario.getEmail() != null && !propietario.getEmail().trim().isEmpty()) {
            try {
                String razonCancelacion = cita.getObservaciones() != null 
                    ? cita.getObservaciones() 
                    : "La cita ha sido eliminada del sistema";
                
                boolean emailEnviado = emailService.enviarEmailCancelacionCita(
                    propietario.getEmail(),
                    propietario.getNombre(),
                    cita.getPaciente().getNombre(),
                    cita.getFecha(),
                    cita.getMotivo(),
                    cita.getProfesional().getNombre(),
                    razonCancelacion
                );
                
                if (emailEnviado) {
                    log.info("✓ Email de cancelación enviado a: {} antes de eliminar la cita", propietario.getEmail());
                } else {
                    log.warn("No se pudo enviar email de cancelación a: {}", propietario.getEmail());
                }
            } catch (Exception e) {
                log.error("Error al enviar email de cancelación antes de eliminar cita: {}", e.getMessage());
                // No lanzar excepción para no interrumpir la eliminación
            }
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
            @NonNull Pageable pageable) {
        
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

    /**
     * Método auxiliar para enviar correos electrónicos según el cambio de estado de una cita.
     * 
     * <p>Envía diferentes tipos de correos según el estado nuevo:</p>
     * <ul>
     *   <li>CANCELADA: Envía correo de cancelación</li>
     *   <li>CONFIRMADA: Envía correo de confirmación</li>
     *   <li>ATENDIDA/COMPLETADA: Envía correo de actualización de estado</li>
     *   <li>PENDIENTE: Envía correo de actualización de estado</li>
     * </ul>
     * 
     * @param cita Cita con el estado actualizado
     * @param estadoAnterior Estado anterior de la cita
     * @param nuevoEstado Nuevo estado de la cita
     */
    private void enviarEmailPorCambioDeEstado(Cita cita, Cita.EstadoCita estadoAnterior, Cita.EstadoCita nuevoEstado) {
        Propietario propietario = cita.getPropietario();
        
        // Validar que el propietario existe y tiene email
        if (propietario == null || propietario.getEmail() == null || propietario.getEmail().trim().isEmpty()) {
            log.debug("Propietario sin email, no se envía correo de cambio de estado");
            return;
        }

        try {
            String propietarioEmail = propietario.getEmail();
            String propietarioNombre = propietario.getNombre();
            String pacienteNombre = cita.getPaciente() != null ? cita.getPaciente().getNombre() : "N/A";
            LocalDateTime fecha = cita.getFecha();
            String motivo = cita.getMotivo() != null ? cita.getMotivo() : "N/A";
            String profesionalNombre = cita.getProfesional() != null ? cita.getProfesional().getNombre() : "N/A";
            String razonCancelacion = cita.getObservaciones() != null ? cita.getObservaciones() : "No especificada";

            boolean emailEnviado = false;

            // Enviar correo según el nuevo estado
            if (nuevoEstado == Cita.EstadoCita.CANCELADA) {
                emailEnviado = emailService.enviarEmailCancelacionCita(
                    propietarioEmail,
                    propietarioNombre,
                    pacienteNombre,
                    fecha,
                    motivo,
                    profesionalNombre,
                    razonCancelacion
                );
            } else {
                // Para otros estados (CONFIRMADA, ATENDIDA, COMPLETADA, PENDIENTE, etc.)
                emailEnviado = emailService.enviarEmailCambioEstadoCita(
                    propietarioEmail,
                    propietarioNombre,
                    pacienteNombre,
                    fecha,
                    motivo,
                    profesionalNombre,
                    nuevoEstado.name()
                );
            }

            if (emailEnviado) {
                log.info("✓ Email de cambio de estado enviado exitosamente a: {} ({} → {})", 
                    propietarioEmail, estadoAnterior, nuevoEstado);
            } else {
                log.warn("✗ No se pudo enviar email de cambio de estado a: {}", propietarioEmail);
            }
        } catch (Exception e) {
            log.error("✗ Error inesperado al enviar email por cambio de estado de cita: {}", e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }
}

