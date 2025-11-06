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
 * Entidad Paciente - Representa a las mascotas/pacientes de la clínica
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

    @Size(max = 50, message = "El microchip no puede exceder 50 caracteres")
    @Column(length = 50)
    private String microchip;

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

