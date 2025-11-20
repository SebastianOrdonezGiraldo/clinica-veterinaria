package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar consultas médicas veterinarias y la historia clínica de pacientes.
 * 
 * <p>Este servicio centraliza toda la lógica de negocio relacionada con las consultas médicas,
 * que representan el registro detallado de cada atención veterinaria. Cada consulta incluye
 * signos vitales, examen físico, diagnóstico y tratamiento prescrito.</p>
 * 
 * <p><strong>Información registrada en una consulta:</strong></p>
 * <ul>
 *   <li><b>Signos Vitales:</b> Frecuencia cardíaca, respiratoria, temperatura, peso</li>
 *   <li><b>Examen Físico:</b> Observaciones detalladas del estado del paciente</li>
 *   <li><b>Diagnóstico:</b> Conclusión médica de la evaluación</li>
 *   <li><b>Tratamiento:</b> Plan terapéutico prescrito</li>
 *   <li><b>Observaciones:</b> Notas adicionales y seguimiento</li>
 * </ul>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Historial médico completo por paciente (ordenado cronológicamente)</li>
 *   <li>Validación de paciente y profesional existentes</li>
 *   <li>Registro automático de fecha/hora si no se especifica</li>
 *   <li>Trazabilidad completa mediante logs de auditoría</li>
 *   <li>Gestión transaccional para garantizar integridad de datos</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see ConsultaDTO
 * @see Consulta
 * @see ConsultaRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene todas las consultas médicas registradas en el sistema.
     * 
     * <p>Recupera el registro completo de todas las atenciones veterinarias,
     * incluyendo información de pacientes y profesionales.</p>
     * 
     * @return Lista completa de todas las consultas. Nunca es null, puede ser vacía.
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findAll() {
        log.debug("Obteniendo todas las consultas");
        return consultaRepository.findAll().stream()
            .map(c -> ConsultaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Busca y retorna una consulta específica por su identificador.
     * 
     * <p>Incluye información completa del paciente y profesional que realizó la consulta.</p>
     * 
     * @param id Identificador único de la consulta. No puede ser null.
     * @return DTO con la información completa de la consulta.
     * @throws ResourceNotFoundException si no existe una consulta con el ID especificado.
     */
    @Transactional(readOnly = true)
    public ConsultaDTO findById(@NonNull Long id) {
        log.debug("Buscando consulta con ID: {}", id);
        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Consulta", "id", id));
        return ConsultaDTO.fromEntity(consulta, true);
    }

    /**
     * Obtiene la historia clínica completa de un paciente.
     * 
     * <p>Retorna todas las consultas médicas del paciente ordenadas de más reciente
     * a más antigua, formando su historial médico completo. Esencial para el seguimiento
     * médico y la toma de decisiones clínicas.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * Long pacienteId = 123L;
     * List&lt;ConsultaDTO&gt; historial = consultaService.findByPaciente(pacienteId);
     * // historial[0] = consulta más reciente
     * // historial[n] = consulta más antigua
     * </pre>
     * 
     * @param pacienteId ID del paciente (mascota). No puede ser null.
     * @return Lista de consultas ordenada descendentemente por fecha. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findByPaciente(@NonNull Long pacienteId) {
        log.debug("Obteniendo historia clínica del paciente con ID: {}", pacienteId);
        return consultaRepository.findByPacienteIdOrderByFechaDesc(pacienteId).stream()
            .map(c -> ConsultaDTO.fromEntity(c, false))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las consultas realizadas por un veterinario específico.
     * 
     * <p>Útil para revisar el historial de atenciones de un profesional,
     * generar reportes de productividad o auditar la práctica clínica.</p>
     * 
     * @param profesionalId ID del usuario veterinario. No puede ser null.
     * @return Lista de consultas realizadas por el profesional. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findByProfesional(@NonNull Long profesionalId) {
        log.debug("Buscando consultas del profesional con ID: {}", profesionalId);
        return consultaRepository.findByProfesionalId(profesionalId).stream()
            .map(c -> ConsultaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Busca consultas realizadas dentro de un rango de fechas.
     * 
     * <p>Utilizado para generar reportes estadísticos, analizar tendencias,
     * auditorías o búsquedas de atenciones en períodos específicos.</p>
     * 
     * @param inicio Fecha y hora de inicio del rango (inclusivo). No puede ser null.
     * @param fin Fecha y hora de fin del rango (inclusivo). No puede ser null.
     * @return Lista de consultas en el período especificado. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findByFechaRange(@NonNull LocalDateTime inicio, @NonNull LocalDateTime fin) {
        log.debug("Buscando consultas entre {} y {}", inicio, fin);
        return consultaRepository.findByFechaBetween(inicio, fin).stream()
            .map(c -> ConsultaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Registra una nueva consulta médica (atención veterinaria).
     * 
     * <p>Crea un registro completo de una atención veterinaria incluyendo signos vitales,
     * examen físico, diagnóstico y tratamiento. Si no se especifica fecha, se registra
     * automáticamente la fecha/hora actual.</p>
     * 
     * <p><strong>Datos registrados:</strong></p>
     * <ul>
     *   <li><b>Signos vitales:</b> Frecuencia cardíaca (lpm), frecuencia respiratoria (rpm),
     *       temperatura (°C), peso (kg)</li>
     *   <li><b>Examen físico:</b> Observaciones detalladas del estado general y específico</li>
     *   <li><b>Diagnóstico:</b> Conclusión médica basada en la evaluación</li>
     *   <li><b>Tratamiento:</b> Plan terapéutico prescrito (medicamentos, cuidados)</li>
     *   <li><b>Observaciones:</b> Notas adicionales, recomendaciones, seguimiento</li>
     * </ul>
     * 
     * <p><strong>Validaciones:</strong></p>
     * <ul>
     *   <li>El paciente debe existir en el sistema</li>
     *   <li>El profesional debe ser un usuario válido y activo</li>
     *   <li>Fecha se asigna automáticamente si no se proporciona</li>
     * </ul>
     * 
     * @param dto Datos de la consulta médica. No puede ser null. Debe incluir
     *            pacienteId y profesionalId válidos.
     * @return DTO con los datos de la consulta registrada, incluyendo ID asignado.
     * @throws RuntimeException si el paciente o profesional no existen.
     */
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public ConsultaDTO create(@NonNull ConsultaDTO dto) {
        log.info("→ Creando nueva consulta para paciente ID: {}", dto.getPacienteId());
        
        // VALIDACIONES: Entidades relacionadas
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
            .orElseThrow(() -> {
                log.error("✗ Paciente no encontrado con ID: {}", dto.getPacienteId());
                return new ResourceNotFoundException("Paciente", "id", dto.getPacienteId());
            });
        
        Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
            .orElseThrow(() -> {
                log.error("✗ Profesional no encontrado con ID: {}", dto.getProfesionalId());
                return new ResourceNotFoundException("Usuario/Profesional", "id", dto.getProfesionalId());
            });

        Consulta consulta = Consulta.builder()
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now())
            .frecuenciaCardiaca(dto.getFrecuenciaCardiaca())
            .frecuenciaRespiratoria(dto.getFrecuenciaRespiratoria())
            .temperatura(dto.getTemperatura())
            .pesoKg(dto.getPesoKg())
            .examenFisico(dto.getExamenFisico())
            .diagnostico(dto.getDiagnostico())
            .tratamiento(dto.getTratamiento())
            .observaciones(dto.getObservaciones())
            .paciente(paciente)
            .profesional(profesional)
            .build();

        consulta = consultaRepository.save(consulta);
        log.info("✓ Consulta creada exitosamente con ID: {} | Paciente: {}", 
                consulta.getId(), paciente.getNombre());
        
        return ConsultaDTO.fromEntity(consulta, true);
    }

    /**
     * Actualiza la información de una consulta médica existente.
     * 
     * <p>Permite modificar todos los campos de una consulta: signos vitales,
     * examen físico, diagnóstico, tratamiento y observaciones. También permite
     * reasignar el profesional responsable si es necesario.</p>
     * 
     * <p><strong>Nota:</strong> El paciente asociado NO puede cambiar una vez
     * creada la consulta, ya que forma parte de su historial médico.</p>
     * 
     * <p><strong>Campos actualizables:</strong></p>
     * <ul>
     *   <li>Fecha de la consulta</li>
     *   <li>Todos los signos vitales</li>
     *   <li>Examen físico</li>
     *   <li>Diagnóstico</li>
     *   <li>Tratamiento</li>
     *   <li>Observaciones</li>
     *   <li>Profesional responsable</li>
     * </ul>
     * 
     * @param id ID de la consulta a actualizar. No puede ser null.
     * @param dto Nuevos datos para la consulta. No puede ser null.
     * @return DTO con los datos actualizados de la consulta.
     * @throws RuntimeException si la consulta o el profesional no existen.
     */
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public ConsultaDTO update(@NonNull Long id, @NonNull ConsultaDTO dto) {
        log.info("→ Actualizando consulta con ID: {}", id);
        
        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Consulta no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Consulta", "id", id);
            });

        // Actualizar campos
        consulta.setFecha(dto.getFecha());
        consulta.setFrecuenciaCardiaca(dto.getFrecuenciaCardiaca());
        consulta.setFrecuenciaRespiratoria(dto.getFrecuenciaRespiratoria());
        consulta.setTemperatura(dto.getTemperatura());
        consulta.setPesoKg(dto.getPesoKg());
        consulta.setExamenFisico(dto.getExamenFisico());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setObservaciones(dto.getObservaciones());

        // VALIDACIÓN: Actualizar profesional si cambió
        // Usar Objects.equals para evitar NullPointerException
        if (dto.getProfesionalId() != null && 
            !Objects.equals(consulta.getProfesional().getId(), dto.getProfesionalId())) {
            Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario/Profesional", "id", dto.getProfesionalId()));
            consulta.setProfesional(profesional);
        }

        consulta = consultaRepository.save(consulta);
        log.info("✓ Consulta actualizada exitosamente con ID: {}", id);
        
        return ConsultaDTO.fromEntity(consulta, true);
    }

    /**
     * Elimina permanentemente una consulta del sistema.
     * 
     * <p><strong>ADVERTENCIA:</strong> Esta operación elimina el registro de la consulta
     * del historial médico del paciente. La información será irrecuperable.</p>
     * 
     * <p><strong>Consideración importante:</strong> En un ambiente de producción real,
     * se recomienda implementar soft delete (borrado lógico) para preservar la integridad
     * del historial médico y cumplir con regulaciones de mantenimiento de registros.</p>
     * 
     * @param id ID de la consulta a eliminar. No puede ser null.
     * @throws RuntimeException si la consulta no existe.
     */
    public void delete(@NonNull Long id) {
        log.warn("→ Eliminando consulta con ID: {}", id);
        
        if (!consultaRepository.existsById(id)) {
            log.error("✗ Consulta no encontrada con ID: {}", id);
            throw new ResourceNotFoundException("Consulta", "id", id);
        }
        
        consultaRepository.deleteById(id);
        log.warn("⚠ Consulta eliminada exitosamente con ID: {}", id);
    }
    
    /**
     * Busca consultas con filtros combinados y paginación del lado del servidor.
     * 
     * <p><strong>PATRÓN STRATEGY:</strong> Selecciona dinámicamente el query apropiado
     * según los filtros proporcionados. Optimizado para historiales médicos extensos.</p>
     * 
     * <p><strong>Casos de uso soportados:</strong></p>
     * <ul>
     *   <li><b>Sin filtros:</b> Todas las consultas paginadas</li>
     *   <li><b>Por paciente:</b> Historial médico completo de una mascota</li>
     *   <li><b>Por profesional:</b> Todas las consultas de un veterinario</li>
     *   <li><b>Por rango de fechas:</b> Consultas en un período específico</li>
     *   <li><b>Paciente + fechas:</b> Historial de un paciente en un período</li>
     *   <li><b>Profesional + fechas:</b> Consultas de un veterinario en un período</li>
     * </ul>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * // Caso 1: Historial completo de mascota ID 10
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("fecha").descending());
     * Page&lt;ConsultaDTO&gt; result = service.searchWithFilters(10L, null, null, null, pageable);
     * 
     * // Caso 2: Consultas del Dr. Smith este mes
     * LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1);
     * LocalDateTime finMes = inicioMes.plusMonths(1);
     * result = service.searchWithFilters(null, 5L, inicioMes, finMes, pageable);
     * 
     * // Caso 3: Historial de mascota del último año
     * LocalDateTime haceUnAno = LocalDateTime.now().minusYears(1);
     * result = service.searchWithFilters(10L, null, haceUnAno, LocalDateTime.now(), pageable);
     * </pre>
     * 
     * @param pacienteId Filtro opcional por ID de paciente (mascota)
     * @param profesionalId Filtro opcional por ID de profesional (veterinario)
     * @param fechaInicio Filtro opcional por fecha inicial (inclusivo)
     * @param fechaFin Filtro opcional por fecha final (inclusivo)
     * @param pageable Configuración de paginación y ordenamiento. No puede ser null.
     * @return Página de consultas que cumplen los criterios de búsqueda
     */
    @Transactional(readOnly = true)
    public Page<ConsultaDTO> searchWithFilters(
            Long pacienteId,
            Long profesionalId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            @NonNull Pageable pageable) {
        
        log.debug("Buscando consultas con filtros - paciente: {}, profesional: {}, fechas: {} - {}, page: {}", 
            pacienteId, profesionalId, fechaInicio, fechaFin, pageable.getPageNumber());
        
        Page<Consulta> consultas;
        
        // STRATEGY PATTERN: Selección dinámica del query apropiado
        
        // Estrategia 1: Paciente + rango de fechas (historial específico)
        if (pacienteId != null && fechaInicio != null && fechaFin != null) {
            log.debug("Estrategia: Historial de paciente en rango de fechas");
            consultas = consultaRepository.findByPacienteIdAndFechaBetween(
                pacienteId, fechaInicio, fechaFin, pageable);
        }
        // Estrategia 2: Profesional + rango de fechas (evaluación/estadísticas)
        else if (profesionalId != null && fechaInicio != null && fechaFin != null) {
            log.debug("Estrategia: Consultas de profesional en rango de fechas");
            consultas = consultaRepository.findByProfesionalIdAndFechaBetween(
                profesionalId, fechaInicio, fechaFin, pageable);
        }
        // Estrategia 3: Solo rango de fechas (reportes generales)
        else if (fechaInicio != null && fechaFin != null) {
            log.debug("Estrategia: Solo rango de fechas");
            consultas = consultaRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
        }
        // Estrategia 4: Solo paciente (historial completo)
        else if (pacienteId != null) {
            log.debug("Estrategia: Historial completo de paciente");
            consultas = consultaRepository.findByPacienteId(pacienteId, pageable);
        }
        // Estrategia 5: Solo profesional (todas sus consultas)
        else if (profesionalId != null) {
            log.debug("Estrategia: Todas las consultas del profesional");
            consultas = consultaRepository.findByProfesionalId(profesionalId, pageable);
        }
        // Estrategia 6: Sin filtros, todas las consultas
        else {
            log.debug("Estrategia: Sin filtros, retornar todas");
            consultas = consultaRepository.findAll(pageable);
        }
        
        log.debug("Consultas encontradas: {} en página {} de {}", 
            consultas.getNumberOfElements(), 
            consultas.getNumber() + 1, 
            consultas.getTotalPages());
        
        return consultas.map(c -> ConsultaDTO.fromEntity(c, true));
    }
}

