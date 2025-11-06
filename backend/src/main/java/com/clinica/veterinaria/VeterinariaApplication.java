package com.clinica.veterinaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot
 * Sistema de Gestión de Clínica Veterinaria
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
@SpringBootApplication
public class VeterinariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeterinariaApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("🐾 Clínica Veterinaria API Iniciada");
        System.out.println("========================================");
        System.out.println("📍 Puerto: http://localhost:8081");
        System.out.println("📚 Swagger UI: http://localhost:8081/swagger-ui.html");
        System.out.println("💾 H2 Console: http://localhost:8081/h2-console");
        System.out.println("========================================\n");
    }
}

