package com.clinica.veterinaria.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // Configuración de seguridad JWT
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
            .info(new Info()
                .title("API Clínica Veterinaria")
                .version("1.0.0")
                .description("""
                    Sistema completo de gestión para clínicas veterinarias.
                    
                    ## Características
                    - 👥 Gestión de usuarios con roles (ADMIN, VET, RECEPCION, ESTUDIANTE)
                    - 🐾 Registro de pacientes (mascotas)
                    - 👨‍👩‍👧 Administración de propietarios
                    - 📅 Sistema de citas médicas
                    - 🏥 Registro de consultas y tratamientos
                    
                    ## Autenticación
                    Esta API utiliza JWT (JSON Web Tokens) para autenticación.
                    
                    ### Pasos para autenticarse:
                    1. Usar el endpoint `/api/auth/login` con email y contraseña
                    2. Copiar el token JWT de la respuesta
                    3. Click en "Authorize" (🔒) en la parte superior
                    4. Ingresar: `Bearer {tu_token_jwt}`
                    5. Ahora puedes usar todos los endpoints protegidos
                    
                    ## Usuarios de Prueba
                    - ADMIN: admin@clinica.com / admin123
                    - VET: maria@clinica.com / vet123
                    - RECEPCION: ana@clinica.com / recep123
                    """)
                .contact(new Contact()
                    .name("Sebastian Ordoñez")
                    .email("soporte@clinica-veterinaria.com")
                    .url("https://github.com/tu-usuario/clinica-veterinaria"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Servidor de Desarrollo"),
                new Server()
                    .url("https://api.clinica-veterinaria.com")
                    .description("Servidor de Producción")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Ingresa el token JWT obtenido del endpoint /api/auth/login")));
    }
}

