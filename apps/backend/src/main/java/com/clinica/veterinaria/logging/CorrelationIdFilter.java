package com.clinica.veterinaria.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro Servlet que añade un Correlation ID único a cada petición HTTP.
 * 
 * <p>Este filtro proporciona trazabilidad end-to-end de las peticiones, permitiendo
 * rastrear una petición desde el frontend hasta el backend y a través de todos los logs.
 * Es especialmente útil para debugging en producción y análisis de problemas.</p>
 * 
 * <p><strong>Funcionalidad:</strong></p>
 * <ul>
 *   <li>Genera un Correlation ID único (UUID) si no viene en el header</li>
 *   <li>Reutiliza el Correlation ID si viene del frontend (header X-Correlation-ID)</li>
 *   <li>Añade el ID al MDC para que aparezca en todos los logs</li>
 *   <li>Incluye el ID en el header de respuesta para que el frontend pueda usarlo</li>
 *   <li>Limpia el MDC después de procesar la petición (previene memory leaks)</li>
 * </ul>
 * 
 * <p><strong>Orden de ejecución:</strong> Orden 1 (se ejecuta antes que otros filtros)</p>
 * 
 * <p><strong>Header HTTP:</strong> X-Correlation-ID</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see RequestResponseLoggingInterceptor
 * @see AuditLogger
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Intentar obtener el Correlation ID del header de la petición (viene del frontend)
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            
            // Si no existe, generar uno nuevo
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = generateCorrelationId();
            }
            
            // Añadir al MDC para que aparezca en todos los logs
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            
            // Añadir también información del usuario si está autenticado
            // (esto se hará en el filtro JWT después de la autenticación)
            
            // Añadir el Correlation ID a la respuesta para que el frontend pueda usarlo
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Continuar con la cadena de filtros
            chain.doFilter(request, response);
            
        } finally {
            // IMPORTANTE: Limpiar el MDC después de procesar la request
            // para evitar memory leaks en aplicaciones con thread pools
            MDC.clear();
        }
    }
    
    /**
     * Genera un Correlation ID único usando UUID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}

