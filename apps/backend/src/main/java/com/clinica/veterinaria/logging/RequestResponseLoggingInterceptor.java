package com.clinica.veterinaria.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor que registra información detallada de cada request y response HTTP.
 * 
 * Registra:
 * - Método HTTP, URI, parámetros
 * - Headers importantes
 * - Usuario autenticado
 * - Tiempo de procesamiento
 * - Código de respuesta
 * - IP del cliente
 */
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingInterceptor.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("com.clinica.veterinaria.performance");
    
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // 1 segundo
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Registrar tiempo de inicio
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        
        // Añadir información del request al MDC
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("requestMethod", request.getMethod());
        MDC.put("clientIp", getClientIp(request));
        MDC.put("userAgent", request.getHeader("User-Agent"));
        
        // Añadir información del usuario si está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            MDC.put("username", username);
            MDC.put("userId", username); // Puedes mejorar esto obteniendo el ID real del usuario
        }
        
        // Log del request entrante
        logger.info("→ Incoming {} {} from {} | User: {} | Correlation-ID: {}", 
                request.getMethod(),
                request.getRequestURI(),
                getClientIp(request),
                MDC.get("username") != null ? MDC.get("username") : "anonymous",
                MDC.get("correlationId"));
        
        // Log de query parameters si existen
        if (request.getQueryString() != null) {
            logger.debug("  Query params: {}", request.getQueryString());
        }
        
        // Log de headers importantes (solo en DEBUG)
        if (logger.isDebugEnabled()) {
            Map<String, String> headers = getImportantHeaders(request);
            if (!headers.isEmpty()) {
                logger.debug("  Headers: {}", headers);
            }
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) {
        // Este método se ejecuta después del controller pero antes de renderizar la vista
        // No lo usamos por ahora, pero está disponible si se necesita
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) {
        // Calcular duración del request
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long duration = 0;
        
        if (startTime != null) {
            duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", String.valueOf(duration));
        }
        
        // Log del response
        if (ex != null) {
            // Si hubo una excepción
            logger.error("← Response {} {} | Status: 500 | Duration: {}ms | Error: {}", 
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    ex.getMessage());
        } else {
            // Response normal
            int status = response.getStatus();
            String logLevel = getLogLevelForStatus(status);
            
            if ("ERROR".equals(logLevel)) {
                logger.error("← Response {} {} | Status: {} | Duration: {}ms", 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);
            } else if ("WARN".equals(logLevel)) {
                logger.warn("← Response {} {} | Status: {} | Duration: {}ms", 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);
            } else {
                logger.info("← Response {} {} | Status: {} | Duration: {}ms", 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);
            }
        }
        
        // Log de performance para requests lentos
        if (duration > SLOW_REQUEST_THRESHOLD_MS) {
            performanceLogger.warn("⚠️ SLOW REQUEST: {} {} took {}ms (threshold: {}ms) | User: {} | Correlation-ID: {}", 
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    SLOW_REQUEST_THRESHOLD_MS,
                    MDC.get("username") != null ? MDC.get("username") : "anonymous",
                    MDC.get("correlationId"));
        }
        
        // También registrar todas las requests en el log de performance
        performanceLogger.info("Performance: {} {} | {}ms | Status: {}", 
                request.getMethod(),
                request.getRequestURI(),
                duration,
                response.getStatus());
    }
    
    /**
     * Obtiene la IP real del cliente, considerando proxies y load balancers
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs en X-Forwarded-For, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * Obtiene los headers más importantes para debugging
     */
    private Map<String, String> getImportantHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        String[] importantHeaders = {
            "Content-Type", "Accept", "Authorization", "Origin", 
            "Referer", "User-Agent", "X-Correlation-ID"
        };
        
        for (String headerName : importantHeaders) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                // Ofuscar el token de Authorization por seguridad
                if ("Authorization".equals(headerName) && headerValue.startsWith("Bearer ")) {
                    headerValue = "Bearer ***" + headerValue.substring(Math.max(0, headerValue.length() - 8));
                }
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }
    
    /**
     * Determina el nivel de log según el código de estado HTTP
     */
    private String getLogLevelForStatus(int status) {
        if (status >= 500) {
            return "ERROR";
        } else if (status >= 400) {
            return "WARN";
        } else {
            return "INFO";
        }
    }
}

