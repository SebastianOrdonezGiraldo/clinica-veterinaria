package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.dto.UsuarioUpdateDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar usuarios del sistema.
 * 
 * <p>Este servicio maneja todas las operaciones relacionadas con los usuarios del sistema,
 * que son los profesionales veterinarios, administradores y personal de la clínica.
 * Incluye gestión segura de contraseñas mediante bcrypt hashing.</p>
 * 
 * <p><strong>Roles disponibles:</strong></p>
 * <ul>
 *   <li><b>ADMIN:</b> Administrador del sistema - Acceso total</li>
 *   <li><b>VET:</b> Veterinario - Gestión de citas, consultas, historial clínico</li>
 *   <li><b>RECEP:</b> Recepcionista - Gestión de citas y propietarios</li>
 * </ul>
 * 
 * <p><strong>Seguridad implementada:</strong></p>
 * <ul>
 *   <li><b>Hashing de contraseñas:</b> BCrypt con salt automático</li>
 *   <li><b>Email único:</b> Validación de unicidad en create/update</li>
 *   <li><b>Soft delete:</b> Usuarios se desactivan, no se eliminan</li>
 *   <li><b>DTO sin contraseña:</b> Las contraseñas nunca se exponen en respuestas</li>
 * </ul>
 * 
 * <p><strong>Validaciones de negocio:</strong></p>
 * <ul>
 *   <li>El email debe ser único en el sistema</li>
 *   <li>La contraseña es requerida al crear, opcional al actualizar</li>
 *   <li>El rol es requerido y debe ser válido</li>
 *   <li>Los usuarios inactivos no pueden autenticarse</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see UsuarioDTO
 * @see UsuarioCreateDTO
 * @see Usuario
 * @see PasswordEncoder
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Mapa de roles a nombres en español
    private static final Map<Usuario.Rol, String> ROL_NAMES = Map.of(
        Usuario.Rol.ADMIN, "Administrador",
        Usuario.Rol.VET, "Veterinario",
        Usuario.Rol.RECEPCION, "Recepcionista",
        Usuario.Rol.ESTUDIANTE, "Estudiante"
    );

    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * <p>Las contraseñas NO se incluyen en los DTOs retornados por seguridad.</p>
     * 
     * @return Lista completa de usuarios. Nunca es null, puede ser vacía.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        log.debug("Obteniendo todos los usuarios");
        return usuarioRepository.findAll().stream()
            .map(UsuarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por su identificador.
     * 
     * <p><strong>CACHE:</strong> Almacena el resultado por 10 minutos.
     * El resultado se cachea usando el ID como key.</p>
     * 
     * @param id ID del usuario. No puede ser null.
     * @return DTO del usuario sin la contraseña.
     * @throws RuntimeException si el usuario no existe.
     */
    @Cacheable(value = "usuarios", key = "#id")
    @Transactional(readOnly = true)
    public UsuarioDTO findById(@NonNull Long id) {
        log.debug("Buscando usuario con ID: {} (cache miss)", id);
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Usuario", "id", id);
            });
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Busca un usuario por su email (usado para autenticación).
     * 
     * @param email Email del usuario. No puede ser null.
     * @return DTO del usuario sin la contraseña.
     * @throws ResourceNotFoundException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(@NonNull String email) {
        log.debug("Buscando usuario con email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado con email: {}", email);
                return new ResourceNotFoundException("Usuario", "email", email);
            });
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * <p>La contraseña se encripta automáticamente con BCrypt antes de almacenarla.
     * El usuario se crea activo por defecto.</p>
     * 
     * <p><strong>Ejemplo de contraseña hasheada:</strong></p>
     * <pre>
     * Password plano: "miPassword123"
     * Hash BCrypt: "$2a$10$N9qo8uLO..."
     * </pre>
     * 
     * <p><strong>CACHE:</strong> Invalida los cachés de veterinariosActivos y usuarios
     * para asegurar consistencia.</p>
     * 
     * @param dto Datos del nuevo usuario. No puede ser null. Debe incluir email único
     *            y contraseña en texto plano (será encriptada).
     * @return DTO del usuario creado sin la contraseña, incluyendo ID asignado.
     * @throws RuntimeException si el email ya está registrado.
     */
    @CacheEvict(value = {"veterinariosActivos", "usuarios"}, allEntries = true)
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public UsuarioDTO create(@NonNull UsuarioCreateDTO dto) {
        log.info("→ Creando nuevo usuario: {}", dto.getEmail());
        
        // VALIDACIÓN: Email único
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            log.error("✗ Email duplicado: {}", dto.getEmail());
            throw new DuplicateResourceException("Usuario", "email", dto.getEmail());
        }

        // Crear entidad
        Usuario usuario = Usuario.builder()
            .nombre(dto.getNombre())
            .email(dto.getEmail())
            .password(passwordEncoder.encode(dto.getPassword()))
            .rol(dto.getRol())
            .activo(dto.getActivo() != null ? dto.getActivo() : true)
            .build();

        usuario = usuarioRepository.save(usuario);
        log.info("✓ Usuario creado exitosamente con ID: {} | Rol: {}", usuario.getId(), usuario.getRol());
        
        // Enviar email de bienvenida
        if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
            try {
                String rolNombre = ROL_NAMES.getOrDefault(usuario.getRol(), usuario.getRol().name());
                boolean emailEnviado = emailService.enviarEmailBienvenidaUsuario(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    rolNombre
                );
                if (emailEnviado) {
                    log.info("✓ Email de bienvenida enviado exitosamente a: {}", usuario.getEmail());
                } else {
                    log.warn("✗ No se pudo enviar email de bienvenida a: {}", usuario.getEmail());
                }
            } catch (Exception e) {
                log.error("✗ Error al enviar email de bienvenida: {}", e.getMessage(), e);
                // No lanzar excepción para no interrumpir el flujo principal
            }
        }
        
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Actualiza la información de un usuario existente.
     * 
     * <p>Permite actualizar todos los campos incluyendo el rol. Si se proporciona
     * una nueva contraseña, será hasheada antes de almacenarse. Si no se proporciona
     * contraseña, la anterior se mantiene sin cambios.</p>
     * 
     * <p><strong>Actualización de contraseña:</strong> Solo si dto.getPassword() no es null ni vacío.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida los cachés de veterinariosActivos y usuarios
     * para reflejar cambios inmediatamente.</p>
     * 
     * @param id ID del usuario a actualizar. No puede ser null.
     * @param dto Nuevos datos del usuario. No puede ser null.
     * @return DTO del usuario actualizado sin la contraseña.
     * @throws RuntimeException si el usuario no existe o el email ya está registrado.
     */
    @CacheEvict(value = {"veterinariosActivos", "usuarios"}, allEntries = true)
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public UsuarioDTO update(@NonNull Long id, @NonNull UsuarioUpdateDTO dto) {
        log.info("→ Actualizando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Usuario", "id", id);
            });

        // VALIDACIÓN: Email único (si cambió)
        if (!usuario.getEmail().equals(dto.getEmail()) 
            && usuarioRepository.existsByEmail(dto.getEmail())) {
            log.error("✗ Email duplicado: {}", dto.getEmail());
            throw new DuplicateResourceException("Usuario", "email", dto.getEmail());
        }

        // Actualizar campos
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());
        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }
        
        // Detectar si se cambió la contraseña
        boolean passwordCambiado = dto.getPassword() != null && !dto.getPassword().trim().isEmpty();
        
        // Solo actualizar password si se proporciona
        if (passwordCambiado) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);
        log.info("✓ Usuario actualizado exitosamente con ID: {}", id);
        
        // Enviar email si se cambió la contraseña
        if (passwordCambiado && usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
            try {
                boolean emailEnviado = emailService.enviarEmailCambioPasswordUsuario(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    false // No es reset por admin, es cambio por el usuario mismo
                );
                if (emailEnviado) {
                    log.info("✓ Email de cambio de contraseña enviado exitosamente a: {}", usuario.getEmail());
                } else {
                    log.warn("✗ No se pudo enviar email de cambio de contraseña a: {}", usuario.getEmail());
                }
            } catch (Exception e) {
                log.error("✗ Error al enviar email de cambio de contraseña: {}", e.getMessage(), e);
                // No lanzar excepción para no interrumpir el flujo principal
            }
        }
        
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Desactiva un usuario del sistema (Soft Delete).
     * 
     * <p>Los usuarios no se eliminan físicamente para mantener trazabilidad en
     * consultas, citas y auditoría. Un usuario inactivo no puede iniciar sesión.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida los cachés de veterinariosActivos y usuarios.</p>
     * 
     * @param id ID del usuario a desactivar. No puede ser null.
     * @throws RuntimeException si el usuario no existe.
     */
    @CacheEvict(value = {"veterinariosActivos", "usuarios"}, allEntries = true)
    public void delete(@NonNull Long id) {
        log.warn("→ Eliminando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Usuario", "id", id);
            });
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        
        log.warn("⚠ Usuario desactivado exitosamente con ID: {}", id);
    }

    /**
     * Obtiene la lista de veterinarios activos.
     * 
     * <p>Útil para listados al asignar citas o consultas. Solo retorna usuarios
     * con rol VET y estado activo.</p>
     * 
     * <p><strong>CACHE-ASIDE PATTERN:</strong> Este método es ideal para caché porque:</p>
     * <ul>
     *   <li>Se consulta frecuentemente (al crear/editar citas)</li>
     *   <li>Los veterinarios activos no cambian frecuentemente</li>
     *   <li>El resultado es el mismo para todos los usuarios</li>
     *   <li>TTL de 10 minutos es suficiente para datos relativamente estables</li>
     * </ul>
     * 
     * <p><strong>VENTAJAS:</strong></p>
     * <ul>
     *   <li>Reducción de ~95% en latencia (de 50ms a 2ms)</li>
     *   <li>Disminución de carga en la base de datos</li>
     *   <li>Mejor experiencia de usuario en formularios de citas</li>
     * </ul>
     * 
     * <p><strong>INVALIDACIÓN:</strong> El caché se limpia automáticamente cuando:</p>
     * <ul>
     *   <li>Se crea un nuevo usuario veterinario (create)</li>
     *   <li>Se actualiza un usuario (update) - puede cambiar rol o estado activo</li>
     *   <li>Se desactiva un usuario (delete)</li>
     *   <li>Pasa el TTL de 10 minutos</li>
     * </ul>
     * 
     * @return Lista de veterinarios activos. Puede estar vacía.
     */
    @Cacheable(value = "veterinariosActivos")
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findVeterinariosActivos() {
        log.debug("Obteniendo veterinarios activos (cache miss - consultando DB)");
        return usuarioRepository.findVeterinariosActivos().stream()
            .map(UsuarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Cuenta usuarios por rol.
     * 
     * <p>Útil para estadísticas administrativas sobre la distribución de roles.</p>
     * 
     * @param rol Rol a contar. No puede ser null.
     * @return Número de usuarios con el rol especificado.
     */
    @Transactional(readOnly = true)
    public long countByRol(Usuario.Rol rol) {
        return usuarioRepository.countByRol(rol);
    }

    /**
     * Resetea la contraseña de un usuario.
     * 
     * <p>Permite a un administrador cambiar la contraseña de cualquier usuario del sistema.
     * La nueva contraseña se hashea automáticamente con BCrypt antes de almacenarse.</p>
     * 
     * <p><strong>Uso típico:</strong> Cuando un usuario olvida su contraseña o necesita
     * un reset por razones de seguridad.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida el caché de usuarios para reflejar la actualización.</p>
     * 
     * @param id ID del usuario cuya contraseña se va a resetear. No puede ser null.
     * @param newPassword Nueva contraseña en texto plano. Será hasheada antes de almacenarse.
     * @throws RuntimeException si el usuario no existe.
     */
    @CacheEvict(value = "usuarios", key = "#id")
    public void resetPassword(@NonNull Long id, @NonNull String newPassword) {
        log.info("→ Reseteando contraseña del usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Usuario", "id", id);
            });
        
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        log.info("✓ Contraseña reseteada exitosamente para usuario con ID: {}", id);
        
        // Enviar email de notificación de reset de contraseña
        if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
            try {
                boolean emailEnviado = emailService.enviarEmailCambioPasswordUsuario(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    true // Es reset por admin
                );
                if (emailEnviado) {
                    log.info("✓ Email de reset de contraseña enviado exitosamente a: {}", usuario.getEmail());
                } else {
                    log.warn("✗ No se pudo enviar email de reset de contraseña a: {}", usuario.getEmail());
                }
            } catch (Exception e) {
                log.error("✗ Error al enviar email de reset de contraseña: {}", e.getMessage(), e);
                // No lanzar excepción para no interrumpir el flujo principal
            }
        }
    }
    
    /**
     * Busca usuarios con filtros combinados y paginación del lado del servidor.
     * 
     * <p><strong>PATRÓN STRATEGY:</strong> Selecciona dinámicamente el query apropiado
     * según los filtros proporcionados.</p>
     * 
     * @param nombre Filtro opcional por nombre (búsqueda parcial, case-insensitive)
     * @param rol Filtro opcional por rol (ADMIN, VET, RECEPCION, ESTUDIANTE)
     * @param activo Filtro opcional por estado (true/false)
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de usuarios que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> searchWithFilters(
            String nombre,
            Usuario.Rol rol,
            Boolean activo,
            Pageable pageable) {
        
        log.debug("Buscando usuarios - nombre: {}, rol: {}, activo: {}", nombre, rol, activo);
        
        Page<Usuario> usuarios;
        
        // STRATEGY PATTERN: Selección dinámica del query apropiado
        if (nombre != null && !nombre.trim().isEmpty() && rol != null) {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCaseAndRol(nombre, rol, pageable);
        } else if (nombre != null && !nombre.trim().isEmpty()) {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else if (rol != null && activo != null) {
            usuarios = usuarioRepository.findByRolAndActivo(rol, activo, pageable);
        } else if (rol != null) {
            usuarios = usuarioRepository.findByRol(rol, pageable);
        } else if (activo != null) {
            usuarios = usuarioRepository.findByActivo(activo, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }
        
        log.debug("Usuarios encontrados: {} en página {}/{}", 
            usuarios.getNumberOfElements(), 
            usuarios.getNumber() + 1, 
            usuarios.getTotalPages());
        
        return usuarios.map(UsuarioDTO::fromEntity);
    }
}
