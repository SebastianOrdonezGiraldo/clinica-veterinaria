/**
 * Paquete de Repositorios Spring Data JPA
 * 
 * Este paquete contiene las interfaces de repositorio que proporcionan
 * acceso a datos para todas las entidades del dominio.
 * 
 * Todos los repositorios siguen estos patrones:
 * 
 * - Extienden JpaRepository<Entity, ID> para operaciones CRUD básicas
 * - Métodos de consulta derivados del nombre (findBy, countBy, etc.)
 * - @Query para consultas personalizadas complejas
 * - Queries optimizadas con JOIN FETCH para evitar N+1
 * - Paginación con Pageable para grandes conjuntos de datos
 * - @Repository para componente de Spring
 * 
 * Beneficios del patrón Repository:
 * - Abstracción del acceso a datos
 * - Consultas tipadas y seguras
 * - Sin necesidad de implementación (Spring genera código)
 * - Testeable mediante mocks
 * - Transacciones gestionadas automáticamente
 * 
 * Repositorios disponibles:
 * - UsuarioRepository: Usuarios del sistema
 * - PropietarioRepository: Dueños de mascotas
 * - PacienteRepository: Mascotas/Pacientes
 * - CitaRepository: Citas médicas
 * - ConsultaRepository: Historias clínicas
 * - PrescripcionRepository: Recetas médicas
 * - ItemPrescripcionRepository: Medicamentos en recetas
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
package com.clinica.veterinaria.repository;

