/**
 * Paquete raíz de la aplicación Clínica Veterinaria
 * 
 * <h2>Sistema de Gestión para Clínica Veterinaria</h2>
 * 
 * <p>Esta aplicación proporciona una API REST completa para la gestión integral
 * de una clínica veterinaria, incluyendo:</p>
 * 
 * <ul>
 *   <li>Gestión de usuarios y autenticación con JWT</li>
 *   <li>Registro de propietarios y sus mascotas</li>
 *   <li>Agendamiento y seguimiento de citas médicas</li>
 *   <li>Historia clínica completa de pacientes</li>
 *   <li>Prescripciones médicas y medicamentos</li>
 *   <li>Sistema de roles y permisos (ADMIN, VET, RECEPCION, ESTUDIANTE)</li>
 * </ul>
 * 
 * <h3>Arquitectura</h3>
 * 
 * <p>El proyecto sigue una arquitectura en capas (Layered Architecture):</p>
 * 
 * <pre>
 * ┌─────────────────────────────────────┐
 * │   CAPA DE PRESENTACIÓN              │
 * │   (Controllers)                     │
 * └──────────────┬──────────────────────┘
 *                │
 * ┌──────────────▼──────────────────────┐
 * │   CAPA DE LÓGICA DE NEGOCIO         │
 * │   (Services)                        │
 * └──────────────┬──────────────────────┘
 *                │
 * ┌──────────────▼──────────────────────┐
 * │   CAPA DE ACCESO A DATOS            │
 * │   (Repositories)                    │
 * └──────────────┬──────────────────────┘
 *                │
 * ┌──────────────▼──────────────────────┐
 * │   CAPA DE PERSISTENCIA              │
 * │   (Entities + PostgreSQL)           │
 * └─────────────────────────────────────┘
 * </pre>
 * 
 * <h3>Tecnologías</h3>
 * 
 * <ul>
 *   <li><strong>Java 17</strong> - Lenguaje de programación</li>
 *   <li><strong>Spring Boot 3.x</strong> - Framework principal</li>
 *   <li><strong>Spring Data JPA</strong> - Persistencia de datos</li>
 *   <li><strong>Spring Security</strong> - Seguridad y autenticación</li>
 *   <li><strong>JWT (jjwt)</strong> - Tokens de autenticación</li>
 *   <li><strong>PostgreSQL</strong> - Base de datos relacional</li>
 *   <li><strong>Lombok</strong> - Reducción de código boilerplate</li>
 *   <li><strong>Maven</strong> - Gestión de dependencias y build</li>
 *   <li><strong>SpringDoc OpenAPI</strong> - Documentación API (Swagger)</li>
 * </ul>
 * 
 * <h3>Patrones de Diseño</h3>
 * 
 * <ul>
 *   <li><strong>Repository Pattern</strong> - Acceso a datos</li>
 *   <li><strong>Service Layer Pattern</strong> - Lógica de negocio</li>
 *   <li><strong>DTO Pattern</strong> - Transferencia de datos</li>
 *   <li><strong>Builder Pattern</strong> - Construcción de objetos</li>
 *   <li><strong>Dependency Injection</strong> - Inversión de control</li>
 *   <li><strong>Factory Method</strong> - Creación de objetos</li>
 * </ul>
 * 
 * <h3>Estructura de Paquetes</h3>
 * 
 * <ul>
 *   <li><strong>entity</strong> - Entidades JPA (modelo de dominio)</li>
 *   <li><strong>repository</strong> - Repositorios Spring Data JPA</li>
 *   <li><strong>dto</strong> - Data Transfer Objects</li>
 *   <li><strong>service</strong> - Servicios con lógica de negocio</li>
 *   <li><strong>controller</strong> - Controladores REST</li>
 *   <li><strong>security</strong> - Configuración de seguridad y JWT</li>
 *   <li><strong>config</strong> - Configuraciones de la aplicación</li>
 * </ul>
 * 
 * <h3>Endpoints Principales</h3>
 * 
 * <ul>
 *   <li><code>POST /api/auth/login</code> - Autenticación</li>
 *   <li><code>GET/POST/PUT/DELETE /api/usuarios</code> - Gestión de usuarios</li>
 *   <li><code>GET/POST/PUT/DELETE /api/propietarios</code> - Gestión de propietarios</li>
 *   <li><code>GET/POST/PUT/DELETE /api/pacientes</code> - Gestión de pacientes</li>
 *   <li><code>GET/POST/PUT/DELETE /api/citas</code> - Gestión de citas</li>
 *   <li><code>GET/POST/PUT/DELETE /api/consultas</code> - Gestión de consultas</li>
 * </ul>
 * 
 * <h3>Seguridad</h3>
 * 
 * <p>El sistema implementa autenticación mediante JWT (JSON Web Tokens) y
 * autorización basada en roles:</p>
 * 
 * <ul>
 *   <li><strong>ADMIN</strong> - Acceso total al sistema</li>
 *   <li><strong>VET</strong> - Gestión de pacientes y consultas médicas</li>
 *   <li><strong>RECEPCION</strong> - Gestión de citas y propietarios</li>
 *   <li><strong>ESTUDIANTE</strong> - Solo lectura (consultas)</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * 
 * @see com.clinica.veterinaria.entity
 * @see com.clinica.veterinaria.repository
 * @see com.clinica.veterinaria.service
 * @see com.clinica.veterinaria.controller
 */
package com.clinica.veterinaria;

