package com.clinica.veterinaria.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
    private static final String MDC_USERNAME_KEY = "username";
    private static final String RESPONSE_LOG_FORMAT = "← Response {} {} | Status: {} | Duration: {}ms";
    private static final String RESPONSE_ERROR_LOG_FORMAT = "← Response {} {} | Status: 500 | Duration: {}ms | Error: {}";
    private static final String UNKNOWN_IP = "unknown";
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // Registrar tiempo de inicio
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        
        // Añadir información del request al MDC
        String clientIp = getClientIp(request);
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("requestMethod", request.getMethod());
        MDC.put("clientIp", clientIp);
        MDC.put("userAgent", request.getHeader("User-Agent"));
        
        // Añadir información del usuario si está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            MDC.put(MDC_USERNAME_KEY, username);
            MDC.put("userId", username); // Puedes mejorar esto obteniendo el ID real del usuario
        }
        
        // Log del request entrante
        String username = MDC.get(MDC_USERNAME_KEY);
        String correlationId = MDC.get("correlationId");
        logger.info("→ Incoming {} {} from {} | User: {} | Correlation-ID: {}", 
                request.getMethod(),
                request.getRequestURI(),
                clientIp,
                username != null ? username : "anonymous",
                correlationId);
        
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
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                          @Nullable ModelAndView modelAndView) {
        // Este método se ejecuta después del controller pero antes de renderizar la vista
        // No lo usamos por ahora, pero está disponible si se necesita
    }
    
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
                               @Nullable Exception ex) {
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
            logger.error(RESPONSE_ERROR_LOG_FORMAT, 
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    ex.getMessage());
        } else {
            // Response normal
            int status = response.getStatus();
            String logLevel = getLogLevelForStatus(status);
            
            if ("ERROR".equals(logLevel)) {
                logger.error(RESPONSE_LOG_FORMAT, 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);
            } else if ("WARN".equals(logLevel)) {
                logger.warn(RESPONSE_LOG_FORMAT, 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);
            } else {
                logger.info(RESPONSE_LOG_FORMAT, 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration);
            }
        }
        
        // Log de performance para requests lentos
        if (duration > SLOW_REQUEST_THRESHOLD_MS) {
            String username = MDC.get(MDC_USERNAME_KEY);
            String correlationId = MDC.get("correlationId");
            performanceLogger.warn("⚠️ SLOW REQUEST: {} {} took {}ms (threshold: {}ms) | User: {} | Correlation-ID: {}", 
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    SLOW_REQUEST_THRESHOLD_MS,
                    username != null ? username : "anonymous",
                    correlationId);
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
    @NonNull
    private String getClientIp(@NonNull HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs en X-Forwarded-For, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // Asegurar que siempre retornamos un valor no nulo
        return ip != null ? ip : UNKNOWN_IP;
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

