package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.*;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar citas públicas con registro opcional de propietario y paciente.
 * 
 * <p>Este servicio permite a los clientes agendar citas sin estar autenticados. Maneja
 * la creación automática de propietarios y pacientes si no existen, o la reutilización
 * de registros existentes.</p>
 * 
 * <p><strong>Flujo de creación:</strong></p>
 * <ol>
 *   <li>Si se proporcionan IDs existentes, se validan y se usan</li>
 *   <li>Si se proporcionan datos nuevos, se busca primero por email del propietario</li>
 *   <li>Si no existe el propietario, se crea uno nuevo</li>
 *   <li>Si no existe el paciente, se crea uno nuevo asociado al propietario</li>
 *   <li>Se crea la cita con los datos validados</li>
 * </ol>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CitaPublicaService {

    private final CitaService citaService;
    private final PropietarioService propietarioService;
    private final PacienteService pacienteService;
    private final PropietarioRepository propietarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea una cita pública con registro opcional de propietario y paciente.
     * 
     * <p>Este método maneja tres escenarios:</p>
     * <ul>
     *   <li><b>IDs existentes:</b> Si se proporcionan propietarioId y pacienteId, se validan y se usan</li>
     *   <li><b>Datos nuevos:</b> Si se proporcionan datos de propietario y paciente, se crean automáticamente</li>
     *   <li><b>Mixto:</b> Se puede usar un propietario existente y crear un nuevo paciente</li>
     * </ul>
     * 
     * <p><strong>Validaciones:</strong></p>
     * <ul>
     *   <li>Si se proporcionan IDs, deben existir en el sistema</li>
     *   <li>Si se proporcionan datos nuevos, el email del propietario se usa para buscar duplicados</li>
     *   <li>El paciente debe pertenecer al propietario especificado</li>
     *   <li>El profesional debe existir y estar activo</li>
     * </ul>
     * 
     * @param request Datos de la solicitud de cita pública. No puede ser null.
     * @return DTO con los datos de la cita creada.
     * @throws ResourceNotFoundException si algún ID proporcionado no existe.
     * @throws BusinessException si alguna validación de negocio falla.
     */
    public CitaDTO crearCitaPublica(@org.springframework.lang.NonNull CitaPublicaRequestDTO request) {
        log.info("→ Creando cita pública - Profesional ID: {}", request.getProfesionalId());
        
        Long propietarioId;
        Long pacienteId;

        // CASO 1: Se proporcionan IDs existentes
        if (request.getPropietarioId() != null && request.getPacienteId() != null) {
            log.info("Usando IDs existentes - Propietario: {}, Paciente: {}", 
                request.getPropietarioId(), request.getPacienteId());
            
            // Validar que existan
            Propietario propietario = propietarioRepository.findById(request.getPropietarioId())
                .orElseThrow(() -> {
                    log.error("✗ Propietario no encontrado con ID: {}", request.getPropietarioId());
                    return new ResourceNotFoundException("Propietario", "id", request.getPropietarioId());
                });
            
            Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> {
                    log.error("✗ Paciente no encontrado con ID: {}", request.getPacienteId());
                    return new ResourceNotFoundException("Paciente", "id", request.getPacienteId());
                });
            
            // Validar que el paciente pertenezca al propietario
            if (!paciente.getPropietario().getId().equals(propietario.getId())) {
                log.error("✗ El paciente {} no pertenece al propietario {}", 
                    request.getPacienteId(), request.getPropietarioId());
                throw new BusinessException("El paciente no pertenece al propietario especificado");
            }
            
            propietarioId = propietario.getId();
            pacienteId = paciente.getId();
        }
        // CASO 2: Se proporcionan datos nuevos de propietario y paciente
        else if (request.getPropietarioNuevo() != null && request.getPacienteNuevo() != null) {
            log.info("Creando nuevo propietario y paciente - Email: {}", 
                request.getPropietarioNuevo().getEmail());
            
            // Buscar si el propietario ya existe por email
            Propietario propietario = propietarioRepository
                .findByEmail(request.getPropietarioNuevo().getEmail())
                .orElse(null);
            
            if (propietario != null) {
                log.info("Propietario encontrado por email - ID: {}", propietario.getId());
                propietarioId = propietario.getId();
                
                // Si el propietario existe pero no tiene contraseña y se proporciona una, actualizarla
                if ((propietario.getPassword() == null || propietario.getPassword().trim().isEmpty()) 
                    && request.getPropietarioNuevo().getPassword() != null 
                    && !request.getPropietarioNuevo().getPassword().trim().isEmpty()) {
                    propietario.setPassword(passwordEncoder.encode(request.getPropietarioNuevo().getPassword()));
                    propietarioRepository.save(propietario);
                    log.info("✓ Contraseña agregada al propietario existente - ID: {}", propietarioId);
                }
            } else {
                // Crear nuevo propietario con contraseña si se proporciona
                PropietarioDTO propietarioDTO = PropietarioDTO.builder()
                    .nombre(request.getPropietarioNuevo().getNombre())
                    .documento(request.getPropietarioNuevo().getDocumento())
                    .email(request.getPropietarioNuevo().getEmail())
                    .telefono(request.getPropietarioNuevo().getTelefono())
                    .direccion(request.getPropietarioNuevo().getDireccion())
                    .build();
                
                PropietarioDTO creado;
                if (request.getPropietarioNuevo().getPassword() != null 
                    && !request.getPropietarioNuevo().getPassword().trim().isEmpty()) {
                    creado = propietarioService.createWithPassword(propietarioDTO, request.getPropietarioNuevo().getPassword());
                    log.info("✓ Propietario creado con contraseña - ID: {}", creado.getId());
                } else {
                    creado = propietarioService.create(propietarioDTO);
                    log.info("✓ Propietario creado sin contraseña - ID: {}", creado.getId());
                }
                propietarioId = creado.getId();
            }
            
            // Crear nuevo paciente asociado al propietario
            PacienteDTO pacienteDTO = PacienteDTO.builder()
                .nombre(request.getPacienteNuevo().getNombre())
                .especie(request.getPacienteNuevo().getEspecie())
                .raza(request.getPacienteNuevo().getRaza())
                .sexo(request.getPacienteNuevo().getSexo())
                .edadMeses(request.getPacienteNuevo().getEdadMeses())
                .pesoKg(request.getPacienteNuevo().getPesoKg())
                .microchip(request.getPacienteNuevo().getMicrochip())
                .notas(request.getPacienteNuevo().getNotas())
                .propietarioId(propietarioId)
                .build();
            
            PacienteDTO pacienteCreado = pacienteService.create(pacienteDTO);
            pacienteId = pacienteCreado.getId();
            log.info("✓ Paciente creado - ID: {}", pacienteId);
        }
        // CASO 3: Error - falta información
        else {
            log.error("✗ Faltan datos: se requiere propietarioId+pacienteId o propietarioNuevo+pacienteNuevo");
            throw new BusinessException(
                "Debe proporcionar IDs existentes (propietarioId y pacienteId) o datos nuevos (propietarioNuevo y pacienteNuevo)");
        }
        
        // Crear la cita usando el servicio existente
        CitaDTO citaDTO = CitaDTO.builder()
            .fecha(request.getFecha())
            .motivo(request.getMotivo())
            .observaciones(request.getObservaciones())
            .profesionalId(request.getProfesionalId())
            .propietarioId(propietarioId)
            .pacienteId(pacienteId)
            .estado(com.clinica.veterinaria.entity.Cita.EstadoCita.PENDIENTE)
            .build();
        
        CitaDTO citaCreada = citaService.create(citaDTO);
        log.info("✓ Cita pública creada exitosamente - ID: {}", citaCreada.getId());
        
        return citaCreada;
    }
}

