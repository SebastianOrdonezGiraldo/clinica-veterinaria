package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de login de cliente (propietario).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteLoginRequestDTO {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}

