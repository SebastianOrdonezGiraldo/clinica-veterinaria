/**
 * Paquete de Controladores REST (Capa de Presentación)
 * 
 * Este paquete contiene los controladores REST que exponen los endpoints
 * de la API para la aplicación de clínica veterinaria.
 * 
 * Responsabilidades de los controladores:
 * 
 * - Recibir requests HTTP y validar parámetros
 * - Delegar operaciones a la capa de servicios
 * - Convertir respuestas a formato JSON
 * - Manejar códigos de estado HTTP apropiados
 * - Implementar autorización con @PreAuthorize
 * - Logging de requests importantes
 * 
 * Características comunes:
 * 
 * - @RestController: Indica que es un controlador REST
 * - @RequestMapping: Define el path base del controlador
 * - @RequiredArgsConstructor: Inyección de dependencias (Lombok)
 * - @Slf4j: Logger de SLF4J (Lombok)
 * - @CrossOrigin: Permite CORS para desarrollo frontend
 * - @Valid: Validación automática con Jakarta Validation
 * - @PreAuthorize: Control de acceso basado en roles
 * 
 * Métodos HTTP utilizados:
 * 
 * - GET: Obtener recursos (queries, búsquedas)
 * - POST: Crear nuevos recursos
 * - PUT: Actualizar recursos completos
 * - PATCH: Actualizar recursos parcialmente
 * - DELETE: Eliminar recursos
 * 
 * Códigos de estado:
 * 
 * - 200 OK: Operación exitosa
 * - 201 Created: Recurso creado exitosamente
 * - 204 No Content: Operación exitosa sin contenido de respuesta
 * - 400 Bad Request: Error en los datos de entrada
 * - 401 Unauthorized: No autenticado
 * - 403 Forbidden: No autorizado (sin permisos)
 * - 404 Not Found: Recurso no encontrado
 * - 500 Internal Server Error: Error del servidor
 * 
 * Seguridad:
 * 
 * - Todos los endpoints requieren autenticación JWT (excepto /api/auth/*)
 * - Los roles se verifican con @PreAuthorize
 * - ADMIN: Acceso total
 * - VET: Gestión de pacientes, consultas, prescripciones
 * - RECEPCION: Gestión de propietarios, pacientes, citas
 * - ESTUDIANTE: Solo lectura
 * 
 * Controladores disponibles:
 * 
 * - AuthController: Autenticación y login
 * - UsuarioController: Gestión de usuarios del sistema
 * - PropietarioController: Gestión de propietarios
 * - PacienteController: Gestión de pacientes (mascotas)
 * - CitaController: Gestión de citas médicas
 * - ConsultaController: Gestión de consultas (historia clínica)
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
package com.clinica.veterinaria.controller;

