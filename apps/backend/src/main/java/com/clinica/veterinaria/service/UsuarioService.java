package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.dto.UsuarioUpdateDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
     * @param id ID del usuario. No puede ser null.
     * @return DTO del usuario sin la contraseña.
     * @throws RuntimeException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findById(Long id) {
        log.debug("Buscando usuario con ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Busca un usuario por su email (usado para autenticación).
     * 
     * @param email Email del usuario. No puede ser null.
     * @return DTO del usuario sin la contraseña.
     * @throws RuntimeException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(String email) {
        log.debug("Buscando usuario con email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
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
     * @param dto Datos del nuevo usuario. No puede ser null. Debe incluir email único
     *            y contraseña en texto plano (será encriptada).
     * @return DTO del usuario creado sin la contraseña, incluyendo ID asignado.
     * @throws RuntimeException si el email ya está registrado.
     */
    public UsuarioDTO create(UsuarioCreateDTO dto) {
        log.info("Creando nuevo usuario: {}", dto.getEmail());
        
        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + dto.getEmail());
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
        log.info("Usuario creado exitosamente con ID: {}", usuario.getId());
        
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
     * @param id ID del usuario a actualizar. No puede ser null.
     * @param dto Nuevos datos del usuario. No puede ser null.
     * @return DTO del usuario actualizado sin la contraseña.
     * @throws RuntimeException si el usuario no existe o el email ya está registrado.
     */
    public UsuarioDTO update(Long id, UsuarioUpdateDTO dto) {
        log.info("Actualizando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Validar email único (si cambió)
        if (!usuario.getEmail().equals(dto.getEmail()) 
            && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + dto.getEmail());
        }

        // Actualizar campos
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());
        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }
        
        // Solo actualizar password si se proporciona
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente con ID: {}", id);
        
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Desactiva un usuario del sistema (Soft Delete).
     * 
     * <p>Los usuarios no se eliminan físicamente para mantener trazabilidad en
     * consultas, citas y auditoría. Un usuario inactivo no puede iniciar sesión.</p>
     * 
     * @param id ID del usuario a desactivar. No puede ser null.
     * @throws RuntimeException si el usuario no existe.
     */
    public void delete(Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        
        log.info("Usuario eliminado exitosamente con ID: {}", id);
    }

    /**
     * Obtiene la lista de veterinarios activos.
     * 
     * <p>Útil para listados al asignar citas o consultas. Solo retorna usuarios
     * con rol VET y estado activo.</p>
     * 
     * @return Lista de veterinarios activos. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findVeterinariosActivos() {
        log.debug("Obteniendo veterinarios activos");
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
     * @param id ID del usuario cuya contraseña se va a resetear. No puede ser null.
     * @param newPassword Nueva contraseña en texto plano. Será hasheada antes de almacenarse.
     * @throws RuntimeException si el usuario no existe.
     */
    public void resetPassword(Long id, String newPassword) {
        log.info("Reseteando contraseña del usuario con ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        log.info("Contraseña reseteada exitosamente para usuario con ID: {}", id);
    }
}
