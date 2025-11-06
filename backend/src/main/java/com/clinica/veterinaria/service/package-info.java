/**
 * Paquete de Servicios (Capa de Lógica de Negocio)
 * 
 * Este paquete contiene los servicios que implementan la lógica de negocio
 * de la aplicación, siguiendo el patrón Service Layer.
 * 
 * Responsabilidades de los servicios:
 * 
 * - Implementar la lógica de negocio y reglas de validación
 * - Coordinar operaciones entre múltiples repositorios
 * - Gestionar transacciones con @Transactional
 * - Transformar entidades a DTOs y viceversa
 * - Manejar excepciones de negocio
 * - Logging de operaciones importantes
 * 
 * Características comunes:
 * 
 * - @Service: Componente de Spring para servicios
 * - @RequiredArgsConstructor: Inyección de dependencias por constructor (Lombok)
 * - @Slf4j: Logger de SLF4J (Lombok)
 * - @Transactional: Gestión declarativa de transacciones
 * - @Transactional(readOnly = true): Optimización para consultas
 * 
 * Patrones implementados:
 * 
 * - Service Layer: Capa de servicios entre controladores y repositorios
 * - Dependency Injection: Inyección de repositorios y otros servicios
 * - DTO Pattern: Transformación de entidades a DTOs
 * - Transaction Management: Gestión automática de transacciones
 * - Exception Handling: Manejo centralizado de excepciones
 * 
 * Servicios disponibles:
 * 
 * - UsuarioService: Gestión de usuarios y autenticación
 * - PropietarioService: Gestión de propietarios
 * - PacienteService: Gestión de pacientes (mascotas)
 * - CitaService: Gestión de citas médicas
 * - ConsultaService: Gestión de consultas (historia clínica)
 * - (Otros servicios según necesidades)
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
package com.clinica.veterinaria.service;

