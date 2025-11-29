package com.clinica.veterinaria;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Boot
 * Sistema de Gestión de Clínica Veterinaria
 * 
 * <p><strong>Configuración de variables de entorno:</strong></p>
 * <p>Esta aplicación carga automáticamente variables de entorno desde el archivo .env
 * en la raíz del proyecto (si existe). Las variables del sistema tienen prioridad
 * sobre las del archivo .env.</p>
 * 
 * @author Clínica Veterinaria Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class VeterinariaApplication {

    private static final Logger logger = LoggerFactory.getLogger(VeterinariaApplication.class);

    public static void main(String[] args) {
        // Cargar variables de entorno desde archivo .env si existe
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // Buscar .env en la raíz del proyecto (apps/backend/)
                    .filename(".env") // Nombre del archivo
                    .ignoreIfMissing() // No fallar si no existe
                    .load();
            
            // Establecer las variables en el sistema para que Spring Boot las lea
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                // Solo establecer si no existe ya en el sistema (las vars del sistema tienen prioridad)
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            });
            logger.debug("Variables de entorno cargadas desde archivo .env");
        } catch (Exception e) {
            // Si hay algún error cargando .env, continuar sin él
            // Las variables de entorno del sistema seguirán funcionando
            logger.debug("No se pudo cargar archivo .env (esto es normal si no existe): {}", e.getMessage());
        }
        
        SpringApplication.run(VeterinariaApplication.class, args);
        logger.info("\n========================================");
        logger.info("🐾 Clínica Veterinaria API Iniciada");
        logger.info("========================================");
        logger.info("📍 Puerto: http://localhost:8080");
        logger.info("📚 Swagger UI: http://localhost:8080/swagger-ui/index.html");
        logger.info("📖 API Docs: http://localhost:8080/v3/api-docs");
        logger.info("========================================\n");
    }
}

