package com.clinica.veterinaria.config;

import com.clinica.veterinaria.logging.RequestResponseLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring Web MVC para interceptores personalizados.
 * 
 * <p>Esta clase registra interceptores HTTP personalizados que se ejecutan antes y después
 * de procesar las peticiones. Actualmente registra el interceptor de logging de requests/responses.</p>
 * 
 * <p><strong>Interceptores registrados:</strong></p>
 * <ul>
 *   <li><b>RequestResponseLoggingInterceptor:</b> Registra todas las peticiones HTTP y sus respuestas
 *       con información detallada (método, URL, headers, body, tiempo de respuesta)</li>
 * </ul>
 * 
 * <p><strong>Rutas excluidas del logging:</strong></p>
 * <ul>
 *   <li>/swagger-ui/** - Documentación de API</li>
 *   <li>/api-docs/** - Especificación OpenAPI</li>
 *   <li>/actuator/** - Endpoints de monitoreo</li>
 *   <li>/error - Páginas de error</li>
 *   <li>/favicon.ico - Icono del navegador</li>
 *   <li>/static/**, /public/** - Recursos estáticos</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see RequestResponseLoggingInterceptor
 * @see WebMvcConfigurer
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final RequestResponseLoggingInterceptor loggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/actuator/**",
                        "/error",
                        "/favicon.ico",
                        "/static/**",
                        "/public/**"
                );
    }
}

