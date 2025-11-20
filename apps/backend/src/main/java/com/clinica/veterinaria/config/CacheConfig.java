package com.clinica.veterinaria.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para el sistema de la clínica veterinaria.
 * 
 * <p>Implementa <strong>Cache-Aside Pattern (Lazy Loading)</strong> usando Caffeine
 * como proveedor de caché de alto rendimiento. Esta configuración define múltiples
 * cachés con diferentes políticas de expiración según la naturaleza de los datos.</p>
 * 
 * <p><strong>PATRÓN: Cache-Aside (Lazy Loading)</strong></p>
 * <ul>
 *   <li>La aplicación consulta primero el caché</li>
 *   <li>Si no existe (cache miss), consulta la base de datos</li>
 *   <li>Almacena el resultado en caché para futuras consultas</li>
 *   <li>El caché se invalida automáticamente en operaciones de escritura</li>
 * </ul>
 * 
 * <p><strong>VENTAJAS DE CAFFEINE:</strong></p>
 * <ul>
 *   <li><b>Alto rendimiento:</b> Más rápido que Guava Cache y EhCache</li>
 *   <li><b>Window TinyLfu:</b> Algoritmo de evicción óptimo (mejor hit rate)</li>
 *   <li><b>Async loading:</b> Soporte para carga asíncrona</li>
 *   <li><b>Métricas integradas:</b> Estadísticas de hit/miss rate</li>
 *   <li><b>Thread-safe:</b> Concurrencia optimizada</li>
 * </ul>
 * 
 * <p><strong>CACHÉS DEFINIDOS:</strong></p>
 * <table border="1">
 *   <tr>
 *     <th>Nombre</th>
 *     <th>TTL</th>
 *     <th>Max Entries</th>
 *     <th>Uso</th>
 *   </tr>
 *   <tr>
 *     <td>veterinariosActivos</td>
 *     <td>10 min</td>
 *     <td>100</td>
 *     <td>Lista de veterinarios para asignar citas (cambia poco)</td>
 *   </tr>
 *   <tr>
 *     <td>propietarios</td>
 *     <td>5 min</td>
 *     <td>500</td>
 *     <td>Búsqueda individual de propietarios</td>
 *   </tr>
 *   <tr>
 *     <td>pacientes</td>
 *     <td>5 min</td>
 *     <td>1000</td>
 *     <td>Búsqueda individual de pacientes (alta frecuencia)</td>
 *   </tr>
 *   <tr>
 *     <td>usuarios</td>
 *     <td>10 min</td>
 *     <td>200</td>
 *     <td>Búsqueda de usuarios (cambia poco)</td>
 *   </tr>
 *   <tr>
 *     <td>consultas</td>
 *     <td>3 min</td>
 *     <td>500</td>
 *     <td>Historiales médicos (se actualizan frecuentemente)</td>
 *   </tr>
 *   <tr>
 *     <td>citas</td>
 *     <td>2 min</td>
 *     <td>300</td>
 *     <td>Agenda de citas (alta volatilidad)</td>
 *   </tr>
 * </table>
 * 
 * <p><strong>ESTRATEGIA DE INVALIDACIÓN:</strong></p>
 * <ul>
 *   <li><b>@CacheEvict:</b> Invalidación manual en operaciones de escritura (create, update, delete)</li>
 *   <li><b>Time-to-live (TTL):</b> Expiración automática después del tiempo configurado</li>
 *   <li><b>Max size:</b> Evicción LFU (Least Frequently Used) cuando se alcanza el límite</li>
 * </ul>
 * 
 * <p><strong>EJEMPLO DE USO EN SERVICIOS:</strong></p>
 * <pre>
 * {@code
 * @Service
 * public class UsuarioService {
 *     
 *     // Cache: almacena resultado por 10 minutos
 *     @Cacheable(value = "veterinariosActivos")
 *     public List<UsuarioDTO> findVeterinariosActivos() {
 *         return usuarioRepository.findVeterinariosActivos()...;
 *     }
 *     
 *     // Invalidación: limpia cache al crear/actualizar/eliminar
 *     @CacheEvict(value = "veterinariosActivos", allEntries = true)
 *     public UsuarioDTO create(UsuarioCreateDTO dto) {
 *         // ... lógica de creación
 *     }
 * }
 * }
 * </pre>
 * 
 * <p><strong>MONITOREO:</strong></p>
 * <ul>
 *   <li>Los logs INFO muestran la configuración de cada caché al iniciar</li>
 *   <li>Caffeine expone métricas de hit/miss rate</li>
 *   <li>Integrado con Spring Boot Actuator para monitoring</li>
 * </ul>
 * 
 * <p><strong>CONSIDERACIONES DE RENDIMIENTO:</strong></p>
 * <ul>
 *   <li><b>Memoria:</b> ~50-100 KB por caché (dependiendo de entries)</li>
 *   <li><b>Hit rate esperado:</b> 70-90% para datos estables</li>
 *   <li><b>Reducción de latencia:</b> 90-95% (de ~50ms DB a ~1ms cache)</li>
 *   <li><b>Throughput:</b> Incremento de 10-20x en lecturas frecuentes</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see Cacheable
 * @see CacheEvict
 * @see Caffeine
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Nombres de cachés utilizados en la aplicación.
     * 
     * <p>Define constantes para evitar errores de tipeo en anotaciones @Cacheable/@CacheEvict.</p>
     */
    public static final String VETERINARIOS_ACTIVOS_CACHE = "veterinariosActivos";
    public static final String PROPIETARIOS_CACHE = "propietarios";
    public static final String PACIENTES_CACHE = "pacientes";
    public static final String USUARIOS_CACHE = "usuarios";
    public static final String CONSULTAS_CACHE = "consultas";
    public static final String CITAS_CACHE = "citas";
    public static final String PRESCRIPCIONES_CACHE = "prescripciones";

    /**
     * Configura el CacheManager con Caffeine usando configuraciones personalizadas por caché.
     * 
     * <p>Cada caché tiene su propia configuración optimizada según:</p>
     * <ul>
     *   <li><b>Frecuencia de acceso:</b> Datos muy consultados → mayor max size</li>
     *   <li><b>Volatilidad:</b> Datos que cambian poco → mayor TTL</li>
     *   <li><b>Tamaño de datos:</b> Registros grandes → menor max size</li>
     * </ul>
     * 
     * @return CacheManager configurado con múltiples cachés Caffeine personalizados
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("🚀 Inicializando Cache Manager con Caffeine - Configuración personalizada");
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager() {
            @Override
            @NonNull
            @SuppressWarnings("null")
            protected com.github.benmanes.caffeine.cache.Cache<Object, Object> createNativeCaffeineCache(@NonNull String name) {
                // Configuración específica por cada caché
                return switch (name) {
                    case VETERINARIOS_ACTIVOS_CACHE -> 
                        // Datos estables, consultados frecuentemente para asignar citas
                        Caffeine.newBuilder()
                            .maximumSize(100)
                            .expireAfterWrite(10, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    case USUARIOS_CACHE -> 
                        // Datos de usuarios, cambian poco
                        Caffeine.newBuilder()
                            .maximumSize(200)
                            .expireAfterWrite(10, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    case PROPIETARIOS_CACHE -> 
                        // Búsquedas individuales frecuentes
                        Caffeine.newBuilder()
                            .maximumSize(500)
                            .expireAfterWrite(5, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    case PACIENTES_CACHE -> 
                        // Alta frecuencia de acceso, datos maestros
                        Caffeine.newBuilder()
                            .maximumSize(1000)
                            .expireAfterWrite(5, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    case CONSULTAS_CACHE -> 
                        // Datos médicos, se actualizan con frecuencia media
                        Caffeine.newBuilder()
                            .maximumSize(500)
                            .expireAfterWrite(3, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    case CITAS_CACHE -> 
                        // Alta volatilidad, agenda cambia constantemente
                        Caffeine.newBuilder()
                            .maximumSize(300)
                            .expireAfterWrite(2, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    case PRESCRIPCIONES_CACHE -> 
                        // Datos médicos, volatilidad media
                        Caffeine.newBuilder()
                            .maximumSize(200)
                            .expireAfterWrite(5, TimeUnit.MINUTES)
                            .recordStats()
                            .build();
                    
                    default -> 
                        // Configuración por defecto para nuevos cachés
                        defaultCaffeineConfig().build();
                };
            }
        };
        
        // Registrar nombres de cachés
        cacheManager.setCacheNames(java.util.List.of(
            VETERINARIOS_ACTIVOS_CACHE,
            PROPIETARIOS_CACHE,
            PACIENTES_CACHE,
            USUARIOS_CACHE,
            CONSULTAS_CACHE,
            CITAS_CACHE,
            PRESCRIPCIONES_CACHE
        ));
        
        log.info("✓ Cache Manager configurado con {} cachés personalizados", 7);
        logCacheConfiguration();
        
        return cacheManager;
    }

    /**
     * Configuración base de Caffeine para cachés sin configuración específica.
     * 
     * <p><strong>Configuración default:</strong></p>
     * <ul>
     *   <li><b>Max size:</b> 500 entries</li>
     *   <li><b>Expire after write:</b> 5 minutos</li>
     *   <li><b>Record stats:</b> true (para monitoreo)</li>
     * </ul>
     * 
     * @return Builder de Caffeine con configuración base
     */
    private Caffeine<Object, Object> defaultCaffeineConfig() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats(); // Habilita métricas de hit/miss rate
    }

    /**
     * Registra en logs la configuración de cada caché al iniciar la aplicación.
     * 
     * <p>Facilita el debugging y permite verificar que las configuraciones
     * son las esperadas en cada ambiente (dev, staging, prod).</p>
     */
    private void logCacheConfiguration() {
        log.info("📋 Configuración de cachés:");
        log.info("  • {} - TTL: 10min, Max: 100 (usuarios veterinarios)", VETERINARIOS_ACTIVOS_CACHE);
        log.info("  • {} - TTL: 5min, Max: 500 (propietarios individuales)", PROPIETARIOS_CACHE);
        log.info("  • {} - TTL: 5min, Max: 1000 (pacientes individuales)", PACIENTES_CACHE);
        log.info("  • {} - TTL: 10min, Max: 200 (usuarios del sistema)", USUARIOS_CACHE);
        log.info("  • {} - TTL: 3min, Max: 500 (consultas médicas)", CONSULTAS_CACHE);
        log.info("  • {} - TTL: 2min, Max: 300 (agenda de citas)", CITAS_CACHE);
        log.info("  • {} - TTL: 5min, Max: 200 (prescripciones)", PRESCRIPCIONES_CACHE);
        log.info("🎯 Cache-Aside Pattern habilitado con Caffeine (Window TinyLfu)");
    }

    // ============================================================================
    // CACHÉS ESPECIALIZADOS (Si se necesitan configuraciones muy específicas)
    // ============================================================================
    
    /**
     * NOTA: La configuración actual usa una configuración base compartida.
     * Si en el futuro necesitas cachés con configuraciones MUY diferentes
     * (ej: TTL de horas, o max size de 10K), puedes crear beans específicos:
     * 
     * <pre>
     * {@code
     * @Bean
     * public Cache veterinariosActivosCache() {
     *     return new CaffeineCache(
     *         VETERINARIOS_ACTIVOS_CACHE,
     *         Caffeine.newBuilder()
     *             .maximumSize(100)
     *             .expireAfterWrite(10, TimeUnit.MINUTES)
     *             .recordStats()
     *             .build()
     *     );
     * }
     * }
     * </pre>
     * 
     * Y luego registrarlos manualmente en un SimpleCacheManager.
     */
}

