package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
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
 * Servicio para gestionar usuarios del sistema
 * Implementa patrón Service con lógica de negocio
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        log.debug("Obteniendo todos los usuarios");
        return usuarioRepository.findAll().stream()
            .map(UsuarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findById(Long id) {
        log.debug("Buscando usuario con ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Obtiene un usuario por email
     */
    @Transactional(readOnly = true)
    public UsuarioDTO findByEmail(String email) {
        log.debug("Buscando usuario con email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Crea un nuevo usuario
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
     * Actualiza un usuario existente
     */
    public UsuarioDTO update(Long id, UsuarioCreateDTO dto) {
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
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente con ID: {}", id);
        
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Elimina un usuario (soft delete)
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
     * Obtiene veterinarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findVeterinariosActivos() {
        log.debug("Obteniendo veterinarios activos");
        return usuarioRepository.findVeterinariosActivos().stream()
            .map(UsuarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Cuenta usuarios por rol
     */
    @Transactional(readOnly = true)
    public long countByRol(Usuario.Rol rol) {
        return usuarioRepository.countByRol(rol);
    }
}

