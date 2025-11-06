/**
 * Paquete de DTOs (Data Transfer Objects)
 * 
 * Este paquete contiene objetos de transferencia de datos que se utilizan
 * para comunicación entre la capa de presentación (controladores) y el cliente.
 * 
 * Beneficios de usar DTOs:
 * 
 * - **Separación de capas**: Las entidades JPA no se exponen directamente
 * - **Seguridad**: Campos sensibles (como password) no se envían al cliente
 * - **Flexibilidad**: Diferentes representaciones según el contexto
 * - **Validación**: Validaciones específicas para entrada/salida
 * - **Performance**: Control sobre qué relaciones cargar
 * - **Versionado**: Cambios en DTOs no afectan entidades
 * 
 * DTOs disponibles:
 * 
 * - UsuarioDTO: Representación de usuario (sin password)
 * - UsuarioCreateDTO: Para crear usuarios (con password)
 * - PropietarioDTO: Representación de propietario
 * - PacienteDTO: Representación de paciente
 * - CitaDTO: Representación de cita
 * - ConsultaDTO: Representación de consulta
 * - PrescripcionDTO: Representación de prescripción
 * - ItemPrescripcionDTO: Representación de item de prescripción
 * - LoginRequestDTO: Solicitud de autenticación
 * - LoginResponseDTO: Respuesta de autenticación con token JWT
 * 
 * Todos los DTOs incluyen:
 * - Validaciones Jakarta (@NotBlank, @NotNull, @Email, etc.)
 * - Método estático fromEntity() para conversión desde entidad
 * - Builder pattern con Lombok
 * - Documentación JavaDoc
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
package com.clinica.veterinaria.dto;

