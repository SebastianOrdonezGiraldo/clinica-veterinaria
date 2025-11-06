/**
 * Paquete de Configuración de la Aplicación
 * 
 * Este paquete contiene las clases de configuración de Spring Boot
 * y componentes de inicialización de la aplicación.
 * 
 * <h3>Componentes</h3>
 * 
 * <ul>
 *   <li><strong>DataInitializer</strong> - Inicializa datos de prueba al arrancar la aplicación</li>
 * </ul>
 * 
 * <h3>DataInitializer</h3>
 * 
 * <p>Implementa {@link org.springframework.boot.CommandLineRunner} para ejecutarse
 * al iniciar la aplicación y crear datos de prueba si la base de datos está vacía.</p>
 * 
 * <h4>Datos Creados</h4>
 * 
 * <ul>
 *   <li><strong>Usuarios (5)</strong>
 *     <ul>
 *       <li>ADMIN: admin@clinica.com / admin123</li>
 *       <li>VET: maria@clinica.com / vet123</li>
 *       <li>VET: carlos@clinica.com / vet123</li>
 *       <li>RECEPCION: ana@clinica.com / recep123</li>
 *       <li>ESTUDIANTE: juan@clinica.com / est123</li>
 *     </ul>
 *   </li>
 *   <li><strong>Propietarios (3)</strong> - Pedro Martínez, Laura Fernández, Roberto Sánchez</li>
 *   <li><strong>Pacientes (5)</strong> - Max (Perro), Luna (Gato), Rocky (Perro), Miau (Gato), Toby (Conejo)</li>
 *   <li><strong>Citas (3)</strong> - Citas programadas para los próximos días</li>
 *   <li><strong>Consultas (2)</strong> - Historias clínicas de ejemplo</li>
 * </ul>
 * 
 * <h4>Comportamiento</h4>
 * 
 * <p>El inicializador verifica si ya existen usuarios en la base de datos.
 * Si encuentra datos, omite la inicialización para evitar duplicados.</p>
 * 
 * <p>Esto permite:</p>
 * <ul>
 *   <li>Desarrollo rápido con datos de prueba</li>
 *   <li>Testing inmediato de la API</li>
 *   <li>Demostración del sistema sin configuración manual</li>
 * </ul>
 * 
 * <h3>Ejemplo de Salida en Logs</h3>
 * 
 * <pre>
 * Inicializando base de datos con datos de prueba...
 * Usuario ADMIN creado: admin@clinica.com / admin123
 * Usuario VET creado: maria@clinica.com / vet123
 * ...
 * 3 propietarios creados
 * 5 pacientes creados
 * 3 citas creadas
 * 2 consultas creadas
 * ============================================================
 * DATOS DE PRUEBA INICIALIZADOS EXITOSAMENTE
 * ============================================================
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * 
 * @see org.springframework.boot.CommandLineRunner
 * @see org.springframework.context.annotation.Configuration
 */
package com.clinica.veterinaria.config;

