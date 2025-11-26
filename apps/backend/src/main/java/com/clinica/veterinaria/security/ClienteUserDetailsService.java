package com.clinica.veterinaria.security;

import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.PropietarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio personalizado para cargar detalles de clientes (propietarios) para autenticación.
 */
@Service
public class ClienteUserDetailsService implements UserDetailsService {

    private final PropietarioRepository propietarioRepository;

    public ClienteUserDetailsService(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Propietario propietario = propietarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado con email: " + email));

        // Verificar si el propietario está activo
        if (!propietario.getActivo()) {
            throw new UsernameNotFoundException("Cliente inactivo: " + email);
        }

        // Verificar que tenga contraseña
        if (propietario.getPassword() == null || propietario.getPassword().trim().isEmpty()) {
            throw new UsernameNotFoundException("Cliente sin contraseña registrada: " + email);
        }

        // Convertir el rol a GrantedAuthority
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CLIENTE");

        // Crear UserDetails de Spring Security
        return User.builder()
            .username(propietario.getEmail())
            .password(propietario.getPassword())
            .authorities(Collections.singletonList(authority))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!propietario.getActivo())
            .build();
    }
}

