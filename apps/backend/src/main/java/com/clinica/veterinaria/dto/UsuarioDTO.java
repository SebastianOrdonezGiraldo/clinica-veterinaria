package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Usuario}.
 * 
 * <p>Este DTO se utiliza para transferir datos de usuarios del sistema entre el cliente
 * y el servidor. <strong>IMPORTANTE:</strong> Este DTO NO incluye la contraseña por razones
 * de seguridad. Para crear o actualizar usuarios con contraseña, use {@link UsuarioCreateDTO}.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Datos personales:</b> ID, nombre, email</li>
 *   <li><b>Autorización:</b> Rol (ADMIN, VET, RECEPCION, ESTUDIANTE)</li>
 *   <li><b>Estado:</b> activo (true/false)</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong> La contraseña nunca se incluye en este DTO, incluso si
 * la entidad la contiene. Esto previene exposición accidental de credenciales en logs
 * o respuestas HTTP.</p>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Email: Requerido, formato válido, máximo 100 caracteres</li>
 *   <li>Rol: Requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Usuario
 * @see UsuarioCreateDTO
 * @see UsuarioService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotNull(message = "El rol es requerido")
    private Usuario.Rol rol;

    private Boolean activo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad {@link Usuario} a su DTO equivalente.
     * 
     * <p><strong>Seguridad:</strong> Este método excluye explícitamente la contraseña
     * del DTO retornado, garantizando que nunca se exponga en respuestas HTTP o logs.</p>
     * 
     * @param usuario Entidad Usuario a convertir. No puede ser null.
     * @return DTO con los datos del usuario sin la contraseña.
     */
    public static UsuarioDTO fromEntity(Usuario usuario) {
        return UsuarioDTO.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .rol(usuario.getRol())
            .activo(usuario.getActivo())
            .createdAt(usuario.getCreatedAt())
            .updatedAt(usuario.getUpdatedAt())
            .build();
    }
}

