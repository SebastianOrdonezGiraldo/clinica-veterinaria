package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.VacunacionDTO;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.entity.Vacuna;
import com.clinica.veterinaria.entity.Vacunacion;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.repository.VacunaRepository;
import com.clinica.veterinaria.repository.VacunacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar registros de vacunaciones aplicadas a pacientes.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VacunacionService {

    private final VacunacionRepository vacunacionRepository;
    private final PacienteRepository pacienteRepository;
    private final VacunaRepository vacunaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IAuditLogger auditLogger;

    @Transactional(readOnly = true)
    public Page<VacunacionDTO> findAll(@NonNull Pageable pageable) {
        log.debug("Obteniendo vacunaciones con paginación");
        return vacunacionRepository.findAll(pageable)
            .map(v -> VacunacionDTO.fromEntity(v, true));
    }

    @Transactional(readOnly = true)
    public VacunacionDTO findById(@NonNull Long id) {
        log.debug("Buscando vacunación con ID: {}", id);
        Vacunacion vacunacion = vacunacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vacunacion", "id", id));
        return VacunacionDTO.fromEntity(vacunacion, true);
    }

    @Transactional(readOnly = true)
    public List<VacunacionDTO> findByPaciente(@NonNull Long pacienteId) {
        log.debug("Buscando vacunaciones del paciente con ID: {}", pacienteId);
        return vacunacionRepository.findByPacienteId(pacienteId).stream()
            .map(v -> VacunacionDTO.fromEntity(v, true))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VacunacionDTO> findByPaciente(@NonNull Long pacienteId, @NonNull Pageable pageable) {
        log.debug("Buscando vacunaciones del paciente con ID: {} (paginado)", pacienteId);
        return vacunacionRepository.findByPacienteIdOrderByFechaAplicacionDesc(pacienteId, pageable)
            .map(v -> VacunacionDTO.fromEntity(v, true));
    }

    @Transactional(readOnly = true)
    public List<VacunacionDTO> findProximasAVencer(int dias) {
        log.debug("Buscando vacunaciones próximas a vencer (próximos {} días)", dias);
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);
        return vacunacionRepository.findProximasAVencer(fechaInicio, fechaFin).stream()
            .map(v -> VacunacionDTO.fromEntity(v, true))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VacunacionDTO> findVencidas() {
        log.debug("Buscando vacunaciones vencidas");
        return vacunacionRepository.findVencidas(LocalDate.now()).stream()
            .map(v -> VacunacionDTO.fromEntity(v, true))
            .collect(Collectors.toList());
    }

    @CacheEvict(value = {"vacunaciones", "pacientes"}, allEntries = true)
    public VacunacionDTO create(@NonNull VacunacionDTO dto) {
        log.info("→ Registrando nueva vacunación - Paciente: {}, Vacuna: {}", dto.getPacienteId(), dto.getVacunaId());

        // Validar paciente
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
            .orElseThrow(() -> {
                log.error("✗ Paciente no encontrado con ID: {}", dto.getPacienteId());
                return new ResourceNotFoundException("Paciente", "id", dto.getPacienteId());
            });

        // Validar vacuna
        Vacuna vacuna = vacunaRepository.findById(dto.getVacunaId())
            .orElseThrow(() -> {
                log.error("✗ Vacuna no encontrada con ID: {}", dto.getVacunaId());
                return new ResourceNotFoundException("Vacuna", "id", dto.getVacunaId());
            });

        if (!vacuna.getActivo()) {
            throw new InvalidDataException("vacunaId", dto.getVacunaId(),
                "La vacuna seleccionada no está activa");
        }

        // Validar profesional
        Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
            .orElseThrow(() -> {
                log.error("✗ Profesional no encontrado con ID: {}", dto.getProfesionalId());
                return new ResourceNotFoundException("Usuario", "id", dto.getProfesionalId());
            });

        // Validar número de dosis
        if (dto.getNumeroDosis() == null || dto.getNumeroDosis() <= 0) {
            throw new InvalidDataException("numeroDosis", dto.getNumeroDosis(),
                "El número de dosis debe ser mayor a 0");
        }

        if (dto.getNumeroDosis() > vacuna.getNumeroDosis()) {
            throw new InvalidDataException("numeroDosis", dto.getNumeroDosis(),
                String.format("El número de dosis no puede exceder %d (máximo para esta vacuna)", vacuna.getNumeroDosis()));
        }

        // Validar fecha de aplicación
        if (dto.getFechaAplicacion() == null) {
            throw new InvalidDataException("fechaAplicacion", null, "La fecha de aplicación es requerida");
        }

        if (dto.getFechaAplicacion().isAfter(LocalDate.now())) {
            throw new InvalidDataException("fechaAplicacion", dto.getFechaAplicacion(),
                "La fecha de aplicación no puede ser futura");
        }

        // Calcular próxima dosis si aplica
        LocalDate proximaDosis = null;
        if (dto.getNumeroDosis() < vacuna.getNumeroDosis() && vacuna.getIntervaloDias() != null) {
            proximaDosis = dto.getFechaAplicacion().plusDays(vacuna.getIntervaloDias());
        }

        Vacunacion vacunacion = Vacunacion.builder()
            .paciente(paciente)
            .vacuna(vacuna)
            .profesional(profesional)
            .fechaAplicacion(dto.getFechaAplicacion())
            .numeroDosis(dto.getNumeroDosis())
            .proximaDosis(proximaDosis)
            .lote(dto.getLote())
            .observaciones(dto.getObservaciones())
            .build();

        vacunacion = vacunacionRepository.save(vacunacion);
        log.info("✓ Vacunación registrada exitosamente con ID: {} | Paciente: {} | Vacuna: {} | Dosis: {}",
            vacunacion.getId(), paciente.getNombre(), vacuna.getNombre(), dto.getNumeroDosis());

        auditLogger.logCreate("Vacunacion", vacunacion.getId(),
            String.format("Paciente: %s, Vacuna: %s, Dosis: %d/%d, Fecha: %s",
                paciente.getNombre(), vacuna.getNombre(), dto.getNumeroDosis(), vacuna.getNumeroDosis(),
                dto.getFechaAplicacion()));

        return VacunacionDTO.fromEntity(vacunacion, true);
    }

    @CacheEvict(value = {"vacunaciones", "pacientes"}, allEntries = true)
    public VacunacionDTO update(@NonNull Long id, @NonNull VacunacionDTO dto) {
        log.info("→ Actualizando vacunación con ID: {}", id);

        Vacunacion vacunacion = vacunacionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Vacunación no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Vacunacion", "id", id);
            });

        String oldData = String.format("Paciente: %s, Vacuna: %s, Dosis: %d, Fecha: %s",
            vacunacion.getPaciente().getNombre(), vacunacion.getVacuna().getNombre(),
            vacunacion.getNumeroDosis(), vacunacion.getFechaAplicacion());

        // Validar vacuna si cambió
        if (!vacunacion.getVacuna().getId().equals(dto.getVacunaId())) {
            Vacuna vacuna = vacunaRepository.findById(dto.getVacunaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacuna", "id", dto.getVacunaId()));
            vacunacion.setVacuna(vacuna);
        }

        // Validar profesional si cambió
        if (!vacunacion.getProfesional().getId().equals(dto.getProfesionalId())) {
            Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", dto.getProfesionalId()));
            vacunacion.setProfesional(profesional);
        }

        // Validar número de dosis
        if (dto.getNumeroDosis() != null) {
            if (dto.getNumeroDosis() <= 0) {
                throw new InvalidDataException("numeroDosis", dto.getNumeroDosis(),
                    "El número de dosis debe ser mayor a 0");
            }
            if (dto.getNumeroDosis() > vacunacion.getVacuna().getNumeroDosis()) {
                throw new InvalidDataException("numeroDosis", dto.getNumeroDosis(),
                    String.format("El número de dosis no puede exceder %d", vacunacion.getVacuna().getNumeroDosis()));
            }
            vacunacion.setNumeroDosis(dto.getNumeroDosis());
        }

        // Actualizar fecha de aplicación
        if (dto.getFechaAplicacion() != null) {
            if (dto.getFechaAplicacion().isAfter(LocalDate.now())) {
                throw new InvalidDataException("fechaAplicacion", dto.getFechaAplicacion(),
                    "La fecha de aplicación no puede ser futura");
            }
            vacunacion.setFechaAplicacion(dto.getFechaAplicacion());

            // Recalcular próxima dosis
            if (vacunacion.getNumeroDosis() < vacunacion.getVacuna().getNumeroDosis()
                && vacunacion.getVacuna().getIntervaloDias() != null) {
                vacunacion.setProximaDosis(dto.getFechaAplicacion().plusDays(vacunacion.getVacuna().getIntervaloDias()));
            } else {
                vacunacion.setProximaDosis(null);
            }
        }

        vacunacion.setLote(dto.getLote());
        vacunacion.setObservaciones(dto.getObservaciones());

        vacunacion = vacunacionRepository.save(vacunacion);

        String newData = String.format("Paciente: %s, Vacuna: %s, Dosis: %d, Fecha: %s",
            vacunacion.getPaciente().getNombre(), vacunacion.getVacuna().getNombre(),
            vacunacion.getNumeroDosis(), vacunacion.getFechaAplicacion());

        log.info("✓ Vacunación actualizada exitosamente con ID: {}", id);
        auditLogger.logUpdate("Vacunacion", id, oldData, newData);

        return VacunacionDTO.fromEntity(vacunacion, true);
    }

    @CacheEvict(value = {"vacunaciones", "pacientes"}, allEntries = true)
    public void delete(@NonNull Long id) {
        log.warn("→ Eliminando vacunación con ID: {}", id);

        Vacunacion vacunacion = vacunacionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Vacunación no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Vacunacion", "id", id);
            });

        String vacunacionInfo = String.format("Paciente: %s, Vacuna: %s, Fecha: %s",
            vacunacion.getPaciente().getNombre(), vacunacion.getVacuna().getNombre(),
            vacunacion.getFechaAplicacion());

        vacunacionRepository.delete(vacunacion);

        log.warn("⚠ Vacunación eliminada: {}", vacunacionInfo);
        auditLogger.logDelete("Vacunacion", id);
    }

    @Transactional(readOnly = true)
    public Optional<VacunacionDTO> findUltimaVacunacion(@NonNull Long pacienteId, @NonNull Long vacunaId) {
        log.debug("Buscando última vacunación - Paciente: {}, Vacuna: {}", pacienteId, vacunaId);
        return vacunacionRepository.findFirstByPacienteIdAndVacunaIdOrderByFechaAplicacionDesc(pacienteId, vacunaId)
            .map(v -> VacunacionDTO.fromEntity(v, true));
    }
}

