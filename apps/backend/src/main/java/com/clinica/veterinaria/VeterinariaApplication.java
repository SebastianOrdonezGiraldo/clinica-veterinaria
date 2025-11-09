package com.clinica.veterinaria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(VeterinariaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VeterinariaApplication.class, args);
        logger.info("\n========================================");
        logger.info("🐾 Clínica Veterinaria API Iniciada");
        logger.info("========================================");
        logger.info("📍 Puerto: http://localhost:8081");
        logger.info("📚 Swagger UI: http://localhost:8081/swagger-ui.html");
        logger.info("💾 H2 Console: http://localhost:8081/h2-console");
        logger.info("========================================\n");
    }
}

