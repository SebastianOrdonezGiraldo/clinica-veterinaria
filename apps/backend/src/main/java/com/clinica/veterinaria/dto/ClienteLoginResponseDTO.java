package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de login de cliente (propietario).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteLoginResponseDTO {
    
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private PropietarioDTO propietario;
}

