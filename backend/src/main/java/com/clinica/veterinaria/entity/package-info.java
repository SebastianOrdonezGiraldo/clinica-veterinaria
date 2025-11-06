/**
 * Paquete de Entidades JPA
 * 
 * Este paquete contiene todas las entidades del dominio de la clínica veterinaria.
 * Todas las entidades siguen los siguientes patrones:
 * 
 * - @Entity y @Table con índices apropiados
 * - @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor de Lombok
 * - Validaciones Jakarta (@NotBlank, @NotNull, @Email, etc.)
 * - Timestamps automáticos (@CreationTimestamp, @UpdateTimestamp)
 * - Soft delete con campo 'activo'
 * - Relaciones JPA bien definidas con FetchType.LAZY
 * - @ToString.Exclude y @EqualsAndHashCode.Exclude en relaciones para evitar problemas
 * 
 * Entidades principales:
 * - Usuario: Usuarios del sistema con autenticación
 * - Propietario: Dueños de mascotas
 * - Paciente: Mascotas/Pacientes
 * - Cita: Citas médicas agendadas
 * - Consulta: Historias clínicas
 * - Prescripcion: Recetas médicas
 * - ItemPrescripcion: Medicamentos en recetas
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
package com.clinica.veterinaria.entity;

