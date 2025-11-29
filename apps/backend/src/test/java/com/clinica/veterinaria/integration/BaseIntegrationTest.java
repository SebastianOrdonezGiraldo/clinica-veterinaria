package com.clinica.veterinaria.integration;

import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.*;
import com.clinica.veterinaria.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase base para tests de integración
 * Proporciona configuración común y utilidades
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected PropietarioRepository propietarioRepository;

    @Autowired
    protected PacienteRepository pacienteRepository;

    @Autowired
    protected CitaRepository citaRepository;

    @Autowired
    protected ConsultaRepository consultaRepository;

    @Autowired
    protected PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtUtil jwtUtil;

    protected String adminToken;
    protected String vetToken;
    protected String recepcionToken;
    protected Usuario adminUser;
    protected Usuario vetUser;
    protected Usuario recepcionUser;

    @BeforeEach
    void baseSetUp() {
        // Limpiar base de datos en el orden correcto (respetando claves foráneas)
        passwordResetTokenRepository.deleteAll();
        consultaRepository.deleteAll();
        citaRepository.deleteAll();
        pacienteRepository.deleteAll();
        propietarioRepository.deleteAll();
        
        // Eliminar usuarios específicos si existen (para evitar conflictos en ejecución paralela)
        usuarioRepository.findByEmail("admin@test.com").ifPresent(usuarioRepository::delete);
        usuarioRepository.findByEmail("vet@test.com").ifPresent(usuarioRepository::delete);
        usuarioRepository.findByEmail("recepcion@test.com").ifPresent(usuarioRepository::delete);
        
        // Limpiar todos los demás usuarios
        usuarioRepository.deleteAll();

        // Crear usuarios de prueba
        adminUser = Usuario.builder()
            .nombre("Admin Test")
            .email("admin@test.com")
            .password(passwordEncoder.encode("admin123"))
            .rol(Usuario.Rol.ADMIN)
            .activo(true)
            .build();
        adminUser = usuarioRepository.save(adminUser);

        vetUser = Usuario.builder()
            .nombre("Vet Test")
            .email("vet@test.com")
            .password(passwordEncoder.encode("vet123"))
            .rol(Usuario.Rol.VET)
            .activo(true)
            .build();
        vetUser = usuarioRepository.save(vetUser);

        recepcionUser = Usuario.builder()
            .nombre("Recepcion Test")
            .email("recepcion@test.com")
            .password(passwordEncoder.encode("recep123"))
            .rol(Usuario.Rol.RECEPCION)
            .activo(true)
            .build();
        recepcionUser = usuarioRepository.save(recepcionUser);

        // Generar tokens JWT
        UserDetails adminDetails = User.builder()
            .username(adminUser.getEmail())
            .password(adminUser.getPassword())
            .authorities("ROLE_" + adminUser.getRol().name())
            .build();
        java.util.Map<String, Object> adminClaims = new java.util.HashMap<>();
        adminClaims.put("rol", adminUser.getRol().name());
        adminClaims.put("userId", adminUser.getId());
        adminToken = jwtUtil.generateToken(adminDetails, adminClaims);

        UserDetails vetDetails = User.builder()
            .username(vetUser.getEmail())
            .password(vetUser.getPassword())
            .authorities("ROLE_" + vetUser.getRol().name())
            .build();
        java.util.Map<String, Object> vetClaims = new java.util.HashMap<>();
        vetClaims.put("rol", vetUser.getRol().name());
        vetClaims.put("userId", vetUser.getId());
        vetToken = jwtUtil.generateToken(vetDetails, vetClaims);

        UserDetails recepcionDetails = User.builder()
            .username(recepcionUser.getEmail())
            .password(recepcionUser.getPassword())
            .authorities("ROLE_" + recepcionUser.getRol().name())
            .build();
        java.util.Map<String, Object> recepcionClaims = new java.util.HashMap<>();
        recepcionClaims.put("rol", recepcionUser.getRol().name());
        recepcionClaims.put("userId", recepcionUser.getId());
        recepcionToken = jwtUtil.generateToken(recepcionDetails, recepcionClaims);
    }

    /**
     * Utilidad para convertir objetos a JSON
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}

