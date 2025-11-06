/**
 * Paquete de Seguridad y Autenticación
 * 
 * Este paquete contiene todas las clases relacionadas con la seguridad
 * de la aplicación, incluyendo autenticación JWT y configuración de Spring Security.
 * 
 * <h3>Componentes Principales</h3>
 * 
 * <ul>
 *   <li><strong>JwtUtil</strong> - Utilidad para generar y validar tokens JWT</li>
 *   <li><strong>JwtAuthenticationFilter</strong> - Filtro para interceptar y validar requests</li>
 *   <li><strong>CustomUserDetailsService</strong> - Carga de usuarios para autenticación</li>
 *   <li><strong>SecurityConfig</strong> - Configuración de Spring Security</li>
 * </ul>
 * 
 * <h3>Flujo de Autenticación</h3>
 * 
 * <pre>
 * 1. Cliente envía POST /api/auth/login con email/password
 * 2. AuthService valida credenciales con Spring Security
 * 3. Si es válido, JwtUtil genera un token JWT
 * 4. Token se devuelve al cliente en la respuesta
 * 5. Cliente incluye token en header Authorization: Bearer {token}
 * 6. JwtAuthenticationFilter intercepta requests y valida el token
 * 7. Si es válido, permite el acceso al recurso solicitado
 * </pre>
 * 
 * <h3>Estructura del Token JWT</h3>
 * 
 * <p>El token contiene:</p>
 * <ul>
 *   <li><strong>subject</strong> - Email del usuario</li>
 *   <li><strong>rol</strong> - Rol del usuario (ADMIN, VET, etc.)</li>
 *   <li><strong>userId</strong> - ID del usuario</li>
 *   <li><strong>iat</strong> - Fecha de emisión</li>
 *   <li><strong>exp</strong> - Fecha de expiración (10 horas)</li>
 * </ul>
 * 
 * <h3>Configuración de Seguridad</h3>
 * 
 * <ul>
 *   <li>Autenticación: JWT stateless (sin sesiones)</li>
 *   <li>Encriptación de contraseñas: BCrypt</li>
 *   <li>CORS: Configurado para frontend en localhost:5173</li>
 *   <li>CSRF: Deshabilitado (API REST stateless)</li>
 *   <li>Endpoints públicos: /api/auth/*, /swagger-ui/*, /api-docs/*</li>
 * </ul>
 * 
 * <h3>Roles y Permisos</h3>
 * 
 * <table border="1">
 *   <tr>
 *     <th>Rol</th>
 *     <th>Permisos</th>
 *   </tr>
 *   <tr>
 *     <td>ADMIN</td>
 *     <td>Acceso total (CRUD usuarios, todas las operaciones)</td>
 *   </tr>
 *   <tr>
 *     <td>VET</td>
 *     <td>Gestión de pacientes, consultas, prescripciones</td>
 *   </tr>
 *   <tr>
 *     <td>RECEPCION</td>
 *     <td>Gestión de propietarios, pacientes, citas</td>
 *   </tr>
 *   <tr>
 *     <td>ESTUDIANTE</td>
 *     <td>Solo lectura (consultas)</td>
 *   </tr>
 * </table>
 * 
 * <h3>Ejemplo de Uso</h3>
 * 
 * <pre>{@code
 * // Login
 * POST /api/auth/login
 * {
 *   "email": "admin@clinica.com",
 *   "password": "admin123"
 * }
 * 
 * // Respuesta
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "type": "Bearer",
 *   "usuario": { ... }
 * }
 * 
 * // Request autenticado
 * GET /api/pacientes
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * }</pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * 
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see io.jsonwebtoken.Jwts
 */
package com.clinica.veterinaria.security;

