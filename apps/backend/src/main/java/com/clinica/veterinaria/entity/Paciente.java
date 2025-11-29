package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa a los pacientes veterinarios (mascotas) atendidos en la clínica.
 * 
 * <p>Esta entidad modela toda la información de identificación y características de las
 * mascotas, incluyendo datos demográficos, físicos y relación con su propietario. Forma
 * el núcleo del sistema ya que todas las citas y consultas están asociadas a un paciente.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> Nombre</li>
 *   <li><b>Características:</b> Especie, raza, sexo (M/F), edad en meses</li>
 *   <li><b>Estado físico:</b> Peso actual en kilogramos</li>
 *   <li><b>Notas:</b> Información adicional, alergias, condiciones especiales</li>
 *   <li><b>Estado:</b> Activo/Inactivo (soft delete para preservar historial)</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Propietario:</b> Muchos a uno - Cada paciente pertenece a un propietario</li>
 *   <li><b>Citas:</b> Uno a muchos - Historial de citas programadas</li>
 *   <li><b>Consultas:</b> Uno a muchos - Historia clínica completa</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Nombre - Para búsquedas por nombre</li>
 *   <li>Especie - Para filtros y estadísticas</li>
 *   <li>Propietario ID - Para listar mascotas de un cliente</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Especie: Requerida, máximo 50 caracteres</li>
 *   <li>Edad: Si se proporciona, debe ser positiva</li>
 *   <li>Peso: Si se proporciona, debe ser positivo</li>
 *   <li>Propietario: Requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Propietario
 * @see Cita
 * @see Consulta
 */
@Entity
@Table(name = "pacientes", indexes = {
    @Index(name = "idx_paciente_nombre", columnList = "nombre"),
    @Index(name = "idx_paciente_especie", columnList = "especie"),
    @Index(name = "idx_paciente_propietario", columnList = "propietario_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La especie es requerida")
    @Size(max = 50, message = "La especie no puede exceder 50 caracteres")
    @Column(nullable = false, length = 50)
    private String especie;

    @Size(max = 100, message = "La raza no puede exceder 100 caracteres")
    @Column(length = 100)
    private String raza;

    @Column(length = 1)
    private String sexo; // M o F

    @Positive(message = "La edad debe ser positiva")
    @Column(name = "edad_meses")
    private Integer edadMeses;

    @Positive(message = "El peso debe ser positivo")
    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(length = 500)
    private String notas;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relación muchos a uno con Propietario
     * Muchas mascotas pueden pertenecer a un propietario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    @NotNull(message = "El propietario es requerido")
    @ToString.Exclude // Evitar carga lazy en toString
    private Propietario propietario;

    /**
     * Relación uno a muchos con Citas
     */
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Cita> citas = new ArrayList<>();

    /**
     * Relación uno a muchos con Consultas (Historia Clínica)
     */
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Consulta> consultas = new ArrayList<>();

    /**
     * Hook antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
    }
}

