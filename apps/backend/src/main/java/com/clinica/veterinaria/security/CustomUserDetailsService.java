package com.clinica.veterinaria.security;

import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio personalizado que implementa {@link UserDetailsService} de Spring Security.
 * 
 * <p>Este servicio es responsable de cargar los datos de usuario desde la base de datos
 * para la autenticación. Spring Security utiliza este servicio para validar credenciales
 * y construir el objeto {@link UserDetails} con la información de autorización.</p>
 * 
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 *   <li><b>Carga de usuario:</b> Busca usuario por email en la base de datos</li>
 *   <li><b>Validación de estado:</b> Verifica que el usuario esté activo</li>
 *   <li><b>Conversión de roles:</b> Convierte el enum Rol a GrantedAuthority de Spring Security</li>
 *   <li><b>Construcción de UserDetails:</b> Crea el objeto UserDetails con toda la información necesaria</li>
 * </ul>
 * 
 * <p><strong>Flujo de autenticación:</strong></p>
 * <ol>
 *   <li>Usuario envía credenciales (email/password) en /api/auth/login</li>
 *   <li>Spring Security llama a loadUserByUsername(email)</li>
 *   <li>Este servicio busca el usuario en la base de datos</li>
 *   <li>Verifica que el usuario esté activo</li>
 *   <li>Retorna UserDetails con email, password hasheado y roles</li>
 *   <li>Spring Security valida la contraseña con BCrypt</li>
 * </li>
 * 
 * <p><strong>Manejo de errores:</strong></p>
 * <ul>
 *   <li>Usuario no encontrado: lanza UsernameNotFoundException</li>
 *   <li>Usuario inactivo: lanza UsernameNotFoundException</li>
 * </ul>
 * 
 * <p><strong>Formato de roles:</strong> Los roles se convierten a "ROLE_ROLNAME" (ej: "ROLE_VET")
 * para compatibilidad con Spring Security.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see UserDetailsService
 * @see UserDetails
 * @see UsuarioRepository
 * @see SecurityConfig
 */
@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por email (username)
     * Requerido por Spring Security para autenticación
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        // Convertir el rol del usuario a GrantedAuthority
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        // Crear UserDetails de Spring Security
        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getPassword())
            .authorities(Collections.singletonList(authority))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!usuario.getActivo())
            .build();
    }
}

