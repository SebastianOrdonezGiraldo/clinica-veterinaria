package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.EstablecerPasswordDTO;
import com.clinica.veterinaria.service.PropietarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/clientes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClientePublicController {

    private final PropietarioService propietarioService;

    /**
     * Establece una contraseña para un propietario existente que no tiene contraseña.
     * 
     * <p>Este endpoint permite que un cliente que fue creado sin contraseña
     * (por ejemplo, al agendar una cita) pueda establecer una contraseña después
     * para acceder al portal del cliente.</p>
     */
    @PostMapping("/establecer-password")
    public ResponseEntity<Void> establecerPassword(@Valid @RequestBody EstablecerPasswordDTO request) {
        log.info("POST /api/public/clientes/establecer-password - email: {}", request.getEmail());
        propietarioService.establecerPassword(request.getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }
}

