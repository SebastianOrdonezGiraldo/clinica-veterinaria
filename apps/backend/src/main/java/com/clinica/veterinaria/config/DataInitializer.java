package com.clinica.veterinaria.config;

import com.clinica.veterinaria.entity.*;
import com.clinica.veterinaria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inicializador de datos de prueba para desarrollo y demostración.
 * 
 * <p>Este componente se ejecuta automáticamente al iniciar la aplicación (si no está
 * en perfil de test) y crea datos de ejemplo para facilitar el desarrollo y las pruebas
 * manuales. Incluye usuarios con diferentes roles, propietarios, pacientes, citas y consultas.</p>
 * 
 * <p><strong>Datos creados:</strong></p>
 * <ul>
 *   <li><b>Usuarios:</b> ADMIN, VET, RECEPCION con credenciales de prueba</li>
 *   <li><b>Propietarios:</b> Clientes de ejemplo con información de contacto</li>
 *   <li><b>Pacientes:</b> Mascotas asociadas a propietarios</li>
 *   <li><b>Citas:</b> Citas médicas programadas</li>
 *   <li><b>Consultas:</b> Registros de atención médica</li>
 * </ul>
 * 
 * <p><strong>Credenciales de prueba:</strong></p>
 * <ul>
 *   <li>ADMIN: admin@clinica.com / admin123</li>
 *   <li>VET: maria@clinica.com / vet123</li>
 *   <li>RECEPCION: ana@clinica.com / recep123</li>
 * </ul>
 * 
 * <p><strong>Comportamiento:</strong></p>
 * <ul>
 *   <li>Solo se ejecuta si la base de datos está vacía</li>
 *   <li>No se ejecuta en perfil de test (para evitar interferir con tests)</li>
 *   <li>Las contraseñas se hashean con BCrypt antes de almacenar</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see CommandLineRunner
 */
@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @SuppressWarnings({"null", "java:S2068"}) // null: builders nunca retornan null, S2068: contraseñas de prueba hardcodeadas
    public CommandLineRunner initDatabase() {
        return args -> {
            // Verificar si ya existen datos
            if (usuarioRepository.count() > 0) {
                log.info("La base de datos ya contiene datos. Omitiendo inicialización.");
                return;
            }

            log.info("Inicializando base de datos con datos de prueba...");

            // ===== USUARIOS =====
            Usuario admin = Usuario.builder()
                .nombre("Administrador")
                .email("admin@clinica.com")
                .password(passwordEncoder.encode("admin123")) // NOSONAR: contraseña de prueba para desarrollo
                .rol(Usuario.Rol.ADMIN)
                .activo(true)
                .build();
            usuarioRepository.save(admin);
            log.info("Usuario ADMIN creado: admin@clinica.com / admin123");

            Usuario vet1 = Usuario.builder()
                .nombre("Dra. María García")
                .email("maria@clinica.com")
                .password(passwordEncoder.encode("vet123")) // NOSONAR: contraseña de prueba para desarrollo
                .rol(Usuario.Rol.VET)
                .activo(true)
                .build();
            usuarioRepository.save(vet1);
            log.info("Usuario VET creado: maria@clinica.com / vet123");

            Usuario vet2 = Usuario.builder()
                .nombre("Dr. Carlos Rodríguez")
                .email("carlos@clinica.com")
                .password(passwordEncoder.encode("vet123")) // NOSONAR: contraseña de prueba para desarrollo
                .rol(Usuario.Rol.VET)
                .activo(true)
                .build();
            usuarioRepository.save(vet2);
            log.info("Usuario VET creado: carlos@clinica.com / vet123");

            Usuario recepcion = Usuario.builder()
                .nombre("Ana López")
                .email("ana@clinica.com")
                .password(passwordEncoder.encode("recep123")) // NOSONAR: contraseña de prueba para desarrollo
                .rol(Usuario.Rol.RECEPCION)
                .activo(true)
                .build();
            usuarioRepository.save(recepcion);
            log.info("Usuario RECEPCION creado: ana@clinica.com / recep123");

            Usuario estudiante = Usuario.builder()
                .nombre("Juan Pérez")
                .email("juan@clinica.com")
                .password(passwordEncoder.encode("est123")) // NOSONAR: contraseña de prueba para desarrollo
                .rol(Usuario.Rol.ESTUDIANTE)
                .activo(true)
                .build();
            usuarioRepository.save(estudiante);
            log.info("Usuario ESTUDIANTE creado: juan@clinica.com / est123");

            // ===== PROPIETARIOS =====
            Propietario prop1 = Propietario.builder()
                .nombre("Pedro Martínez")
                .documento("12345678")
                .email("pedro@email.com")
                .telefono("555-0101")
                .direccion("Calle Principal 123")
                .activo(true)
                .build();
            propietarioRepository.save(prop1);

            Propietario prop2 = Propietario.builder()
                .nombre("Laura Fernández")
                .documento("87654321")
                .email("laura@email.com")
                .telefono("555-0102")
                .direccion("Avenida Central 456")
                .activo(true)
                .build();
            propietarioRepository.save(prop2);

            Propietario prop3 = Propietario.builder()
                .nombre("Roberto Sánchez")
                .documento("11223344")
                .email("roberto@email.com")
                .telefono("555-0103")
                .direccion("Boulevard Norte 789")
                .activo(true)
                .build();
            propietarioRepository.save(prop3);

            log.info("3 propietarios creados");

            // ===== PACIENTES =====
            Paciente pac1 = Paciente.builder()
                .nombre("Max")
                .especie("Perro")
                .raza("Labrador")
                .sexo("M")
                .edadMeses(36)
                .pesoKg(new BigDecimal("30.5"))
                .microchip("123456789012345")
                .notas("Muy juguetón")
                .propietario(prop1)
                .activo(true)
                .build();
            pacienteRepository.save(pac1);

            Paciente pac2 = Paciente.builder()
                .nombre("Luna")
                .especie("Gato")
                .raza("Siamés")
                .sexo("F")
                .edadMeses(24)
                .pesoKg(new BigDecimal("4.2"))
                .microchip("987654321098765")
                .notas("Tímida con extraños")
                .propietario(prop1)
                .activo(true)
                .build();
            pacienteRepository.save(pac2);

            Paciente pac3 = Paciente.builder()
                .nombre("Rocky")
                .especie("Perro")
                .raza("Pastor Alemán")
                .sexo("M")
                .edadMeses(48)
                .pesoKg(new BigDecimal("38.0"))
                .notas("Obediente")
                .propietario(prop2)
                .activo(true)
                .build();
            pacienteRepository.save(pac3);

            Paciente pac4 = Paciente.builder()
                .nombre("Miau")
                .especie("Gato")
                .raza("Persa")
                .sexo("F")
                .edadMeses(12)
                .pesoKg(new BigDecimal("3.8"))
                .notas("Muy cariñosa")
                .propietario(prop2)
                .activo(true)
                .build();
            pacienteRepository.save(pac4);

            Paciente pac5 = Paciente.builder()
                .nombre("Toby")
                .especie("Conejo")
                .raza("Mini Lop")
                .sexo("M")
                .edadMeses(6)
                .pesoKg(new BigDecimal("1.5"))
                .notas("Muy activo")
                .propietario(prop3)
                .activo(true)
                .build();
            pacienteRepository.save(pac5);

            log.info("5 pacientes creados");

            // ===== CITAS =====
            Cita cita1 = Cita.builder()
                .fecha(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .motivo("Vacunación anual")
                .estado(Cita.EstadoCita.PENDIENTE)
                .paciente(pac1)
                .propietario(prop1)
                .profesional(vet1)
                .build();
            citaRepository.save(cita1);

            Cita cita2 = Cita.builder()
                .fecha(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .motivo("Control general")
                .estado(Cita.EstadoCita.CONFIRMADA)
                .paciente(pac3)
                .propietario(prop2)
                .profesional(vet2)
                .build();
            citaRepository.save(cita2);

            Cita cita3 = Cita.builder()
                .fecha(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0))
                .motivo("Consulta por tos")
                .estado(Cita.EstadoCita.PENDIENTE)
                .paciente(pac2)
                .propietario(prop1)
                .profesional(vet1)
                .build();
            citaRepository.save(cita3);

            log.info("3 citas creadas");

            // ===== CONSULTAS =====
            Consulta cons1 = Consulta.builder()
                .fecha(LocalDateTime.now().minusDays(7))
                .frecuenciaCardiaca(120)
                .frecuenciaRespiratoria(30)
                .temperatura(new BigDecimal("38.5"))
                .pesoKg(new BigDecimal("30.2"))
                .examenFisico("Paciente alerta, hidratado. Mucosas rosadas. Sin alteraciones aparentes.")
                .diagnostico("Control de rutina. Buen estado general.")
                .tratamiento("Vacunas al día. Desparasitación recomendada en 3 meses.")
                .observaciones("Propietario reporta buen apetito y actividad normal.")
                .paciente(pac1)
                .profesional(vet1)
                .build();
            consultaRepository.save(cons1);

            Consulta cons2 = Consulta.builder()
                .fecha(LocalDateTime.now().minusDays(3))
                .frecuenciaCardiaca(180)
                .frecuenciaRespiratoria(40)
                .temperatura(new BigDecimal("38.2"))
                .pesoKg(new BigDecimal("4.1"))
                .examenFisico("Paciente tranquila. Mucosas rosadas. Pelaje brillante.")
                .diagnostico("Control post-esterilización. Evolución favorable.")
                .tratamiento("Continuar con antibiótico por 3 días más.")
                .observaciones("Herida quirúrgica en buen estado. Retirar puntos en 7 días.")
                .paciente(pac2)
                .profesional(vet2)
                .build();
            consultaRepository.save(cons2);

            log.info("2 consultas creadas");

            log.info("=".repeat(60));
            log.info("DATOS DE PRUEBA INICIALIZADOS EXITOSAMENTE");
            log.info("=".repeat(60));
            log.info("Credenciales de acceso:");
            log.info("  ADMIN:       admin@clinica.com     / admin123");
            log.info("  VET 1:       maria@clinica.com     / vet123");
            log.info("  VET 2:       carlos@clinica.com    / vet123");
            log.info("  RECEPCION:   ana@clinica.com       / recep123");
            log.info("  ESTUDIANTE:  juan@clinica.com      / est123");
            log.info("=".repeat(60));
            log.info("Estadísticas:");
            log.info("  - {} usuarios", usuarioRepository.count());
            log.info("  - {} propietarios", propietarioRepository.count());
            log.info("  - {} pacientes", pacienteRepository.count());
            log.info("  - {} citas", citaRepository.count());
            log.info("  - {} consultas", consultaRepository.count());
            log.info("=".repeat(60));
        };
    }
}

