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

/**
 * Data Transfer Object (DTO) para crear o actualizar usuarios del sistema.
 * 
 * <p>Este DTO se utiliza exclusivamente para operaciones de creación y actualización
 * de usuarios, ya que incluye el campo de contraseña que no está presente en {@link UsuarioDTO}.</p>
 * 
 * <p><strong>Diferencia con UsuarioDTO:</strong></p>
 * <ul>
 *   <li><b>UsuarioCreateDTO:</b> Incluye password (para crear/actualizar)</li>
 *   <li><b>UsuarioDTO:</b> NO incluye password (para respuestas seguras)</li>
 * </ul>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Datos personales:</b> Nombre, email</li>
 *   <li><b>Autenticación:</b> Password (en texto plano, será hasheado antes de almacenar)</li>
 *   <li><b>Autorización:</b> Rol (ADMIN, VET, RECEPCION, ESTUDIANTE)</li>
 *   <li><b>Estado:</b> activo (opcional, por defecto true)</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Email: Requerido, formato válido, máximo 100 caracteres, único</li>
 *   <li>Password: Requerido al crear, opcional al actualizar, mínimo 6 caracteres</li>
 *   <li>Rol: Requerido</li>
 * </ul>
 * 
 * <p><strong>Nota de seguridad:</strong> La contraseña se recibe en texto plano pero se
 * hashea automáticamente con BCrypt antes de almacenarse en la base de datos.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Usuario
 * @see UsuarioDTO
 * @see UsuarioService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "El rol es requerido")
    private Usuario.Rol rol;

    private Boolean activo;
}

