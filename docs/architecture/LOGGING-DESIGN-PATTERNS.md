# 🏗️ Patrones de Diseño en el Sistema de Logging

## 📋 Índice de Patrones Implementados

1. [Singleton Pattern](#1-singleton-pattern)
2. [Interceptor/Filter Pattern](#2-interceptorfilter-pattern)
3. [Chain of Responsibility](#3-chain-of-responsibility)
4. [Strategy Pattern](#4-strategy-pattern)
5. [Facade Pattern](#5-facade-pattern)
6. [Template Method Pattern](#6-template-method-pattern)
7. [Dependency Injection (DI)](#7-dependency-injection-di)
8. [Decorator Pattern](#8-decorator-pattern)
9. [Observer Pattern](#9-observer-pattern)
10. [Factory Pattern](#10-factory-pattern)
11. [MDC (Mapped Diagnostic Context)](#11-mdc-pattern)

---

## 1. Singleton Pattern

### 📍 Ubicación
**Frontend**: `apps/frontend/src/core/logging/loggerService.ts`

### 🎯 Propósito
Garantizar una única instancia del servicio de logging en toda la aplicación.

### 💻 Implementación

```typescript
class LoggerService {
  private logs: LogEntry[] = [];
  private maxLogsInMemory = 100;
  
  // ... métodos
}

// Exportar instancia singleton
export const loggerService = new LoggerService();

// Exponer en window para debugging
if (typeof window !== 'undefined') {
  (window as any).logger = loggerService;
}
```

### ✅ Beneficios
- Una sola instancia gestiona todos los logs del frontend
- Buffer centralizado de logs
- Configuración consistente
- Fácil acceso desde cualquier parte de la aplicación

### 📊 Diagrama

```
┌─────────────────────────────────────┐
│     LoggerService (Singleton)       │
│  ┌───────────────────────────────┐  │
│  │  private logs: LogEntry[]     │  │
│  │  private maxLogsInMemory      │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  + debug()                    │  │
│  │  + info()                     │  │
│  │  + warn()                     │  │
│  │  + error()                    │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
         ▲          ▲          ▲
         │          │          │
    Component1  Component2  Component3
```

---

## 2. Interceptor/Filter Pattern

### 📍 Ubicaciones

**Backend:**
- `apps/backend/src/main/java/com/clinica/veterinaria/logging/CorrelationIdFilter.java`
- `apps/backend/src/main/java/com/clinica/veterinaria/logging/RequestResponseLoggingInterceptor.java`
- `apps/backend/src/main/java/com/clinica/veterinaria/security/JwtAuthenticationFilter.java`

**Frontend:**
- `apps/frontend/src/core/api/axios.ts` (Axios Interceptors)

### 🎯 Propósito
Interceptar requests/responses para añadir funcionalidad transversal sin modificar el código de negocio.

### 💻 Implementación Backend

#### CorrelationIdFilter
```java
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        // 1. Generar/Obtener Correlation ID
        String correlationId = getOrGenerateCorrelationId(httpRequest);
        
        // 2. Añadir al MDC
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        // 3. Añadir al response
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        // 4. Continuar cadena
        chain.doFilter(request, response);
        
        // 5. Limpiar
        MDC.clear();
    }
}
```

#### RequestResponseLoggingInterceptor
```java
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // Log ANTES del request
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        logger.info("→ Incoming {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Log DESPUÉS del request
        long duration = calculateDuration(request);
        logger.info("← Response {} {} | Status: {} | Duration: {}ms", 
                   request.getMethod(), request.getRequestURI(), 
                   response.getStatus(), duration);
    }
}
```

### 💻 Implementación Frontend

```typescript
// Request Interceptor
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Generar Correlation ID
    const correlationId = generateCorrelationId();
    config.headers['X-Correlation-ID'] = correlationId;
    
    // Log del request
    loggerService.logApiRequest(config.method, config.url, correlationId);
    
    return config;
  }
);

// Response Interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    // Log de response exitoso
    const duration = calculateDuration(response.config);
    loggerService.logApiResponse(method, url, status, duration, correlationId);
    
    return response;
  },
  (error: AxiosError) => {
    // Log de error
    loggerService.logApiError(method, url, status, error, duration, correlationId);
    
    return Promise.reject(error);
  }
);
```

### ✅ Beneficios
- Separación de concerns (logging separado de lógica de negocio)
- Código reutilizable
- Fácil de mantener y testear
- No invasivo (no modifica código existente)

### 📊 Diagrama

```
Request Flow:

Cliente → Filter 1 (CorrelationIdFilter) 
           ↓
       Filter 2 (JwtAuthenticationFilter)
           ↓
       Interceptor (RequestResponseLoggingInterceptor)
           ↓
       Controller
           ↓
       Service
           ↓
       Repository
           ↓
       Database
           ↓
       Response ← (Interceptor logs duration)
```

---

## 3. Chain of Responsibility

### 📍 Ubicación
**Backend**: Cadena de filtros de Spring Security y filtros personalizados

### 🎯 Propósito
Pasar el request por una cadena de handlers, donde cada uno decide si procesa o pasa al siguiente.

### 💻 Implementación

#### Configuración en WebMvcConfig
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/actuator/**");
    }
}
```

#### Orden de Ejecución
```java
@Component
@Order(1)  // ← Define posición en la cadena
public class CorrelationIdFilter implements Filter {
    // ...
}
```

### 📊 Cadena de Filtros

```
Request
  ↓
┌────────────────────────────────────┐
│ 1. CorrelationIdFilter (@Order(1)) │ → Añade Correlation ID
└────────────────────────────────────┘
  ↓
┌────────────────────────────────────┐
│ 2. JwtAuthenticationFilter         │ → Valida token JWT
└────────────────────────────────────┘
  ↓
┌────────────────────────────────────┐
│ 3. RequestResponseLoggingInterceptor│ → Log de request/response
└────────────────────────────────────┘
  ↓
Controller → Service → Repository
```

### ✅ Beneficios
- Desacoplamiento entre handlers
- Fácil añadir/quitar filtros
- Orden configurable
- Cada filtro tiene responsabilidad única

---

## 4. Strategy Pattern

### 📍 Ubicación
**Backend**: `apps/backend/src/main/resources/logback-spring.xml`

### 🎯 Propósito
Diferentes estrategias de logging según el entorno (dev, test, prod).

### 💻 Implementación

```xml
<!-- Estrategia para DESARROLLO -->
<springProfile name="dev">
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>  <!-- Texto plano con colores -->
        <appender-ref ref="ASYNC_FILE"/>
        <appender-ref ref="ASYNC_ERROR_FILE"/>
    </root>
</springProfile>

<!-- Estrategia para PRUEBAS -->
<springProfile name="test">
    <root level="WARN">
        <appender-ref ref="CONSOLE"/>  <!-- Solo consola -->
    </root>
</springProfile>

<!-- Estrategia para PRODUCCIÓN -->
<springProfile name="prod">
    <root level="INFO">
        <appender-ref ref="CONSOLE_JSON"/>  <!-- JSON estructurado -->
        <appender-ref ref="ASYNC_FILE"/>
        <appender-ref ref="ASYNC_ERROR_FILE"/>
    </root>
</springProfile>
```

### 📊 Estrategias de Appenders

```
Strategy: Appender
├── ConsoleAppender (desarrollo)
│   └── PlainTextEncoder con colores
├── ConsoleJsonAppender (producción)
│   └── LogstashEncoder (JSON)
├── FileAppender
│   └── LogstashEncoder → application.log
├── ErrorFileAppender
│   └── LogstashEncoder → error.log
└── AuditFileAppender
    └── LogstashEncoder → audit.log
```

### ✅ Beneficios
- Configuración flexible por entorno
- Fácil cambiar estrategia sin modificar código
- Múltiples destinos de logs simultáneos

---

## 5. Facade Pattern

### 📍 Ubicación
**Backend**: `apps/backend/src/main/java/com/clinica/veterinaria/logging/AuditLogger.java`

### 🎯 Propósito
Proveer una interfaz simplificada para logging de auditoría, ocultando la complejidad del sistema de logging.

### 💻 Implementación

```java
@Service
public class AuditLogger {
    
    private static final Logger auditLogger = 
        LoggerFactory.getLogger("com.clinica.veterinaria.audit");
    
    // Interfaz simplificada para auditoría
    
    public void logCreate(String entity, Object entityId, Object data) {
        // Oculta complejidad de MDC, sanitización, formato
        MDC.put("action", "CREATE");
        MDC.put("entity", entity);
        MDC.put("entityId", String.valueOf(entityId));
        auditLogger.info("✓ CREATED {} with ID {}", entity, entityId);
        // Limpiar MDC
    }
    
    public void logDelete(String entity, Object entityId) {
        // Oculta complejidad
        MDC.put("action", "DELETE");
        auditLogger.warn("⚠ DELETED {} with ID {}", entity, entityId);
        // Limpiar MDC
    }
    
    public void logLoginSuccess(String username, String ipAddress) {
        // Oculta complejidad
        auditLogger.info("🔓 LOGIN SUCCESS | User: {} | IP: {}", 
                        username, ipAddress);
    }
    
    // ... más métodos simplificados
}
```

### 🔧 Uso Simplificado

**Sin Facade (complejo):**
```java
Logger logger = LoggerFactory.getLogger("audit");
MDC.put("action", "CREATE");
MDC.put("entity", "Paciente");
MDC.put("entityId", String.valueOf(id));
String sanitized = sanitizeData(data);
logger.info("Created Paciente with ID {}", id);
MDC.remove("action");
MDC.remove("entity");
MDC.remove("entityId");
```

**Con Facade (simple):**
```java
auditLogger.logCreate("Paciente", id, data);
```

### ✅ Beneficios
- API simple y consistente
- Oculta complejidad del MDC
- Sanitización automática
- Formato consistente
- Fácil de usar

### 📊 Diagrama

```
┌─────────────────────────────────────┐
│         AuditLogger (Facade)        │
│  ┌───────────────────────────────┐  │
│  │ + logCreate()                 │  │
│  │ + logUpdate()                 │  │
│  │ + logDelete()                 │  │
│  │ + logLoginSuccess()           │  │
│  └───────────────────────────────┘  │
│              │                       │
│  ┌───────────▼───────────────────┐  │
│  │ - MDC management              │  │
│  │ - Data sanitization           │  │
│  │ - Format handling             │  │
│  │ - Logger selection            │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

---

## 6. Template Method Pattern

### 📍 Ubicación
**Backend**: `apps/backend/src/main/java/com/clinica/veterinaria/security/JwtAuthenticationFilter.java`

### 🎯 Propósito
Define el esqueleto de un algoritmo, permitiendo que las subclases redefinan ciertos pasos.

### 💻 Implementación

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    // Template Method definido en OncePerRequestFilter
    // doFilter() llama a doFilterInternal() una sola vez por request
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Paso 1: Extraer token
        String jwt = extractJwtFromRequest(request);
        
        // Paso 2: Validar token
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            // Paso 3: Autenticar
            authenticateUser(jwt, request);
        }
        
        // Paso 4: Continuar cadena
        filterChain.doFilter(request, response);
    }
    
    // Métodos auxiliares (pasos específicos)
    private String extractJwtFromRequest(HttpServletRequest request) { }
    private void authenticateUser(String jwt, HttpServletRequest request) { }
}
```

### 📊 Template Method Flow

```
OncePerRequestFilter (Clase abstracta)
│
├── doFilter() [TEMPLATE METHOD]
│   ├── 1. Verificar si ya se ejecutó
│   ├── 2. Llamar a doFilterInternal() [HOOK]
│   └── 3. Marcar como ejecutado
│
└── doFilterInternal() [ABSTRACT - implementado por subclases]
    ├── Paso 1: Extraer token
    ├── Paso 2: Validar token
    ├── Paso 3: Autenticar
    └── Paso 4: Continuar cadena
```

### ✅ Beneficios
- Reutilización de código común
- Estructura consistente
- Fácil extensión
- Previene duplicación

---

## 7. Dependency Injection (DI)

### 📍 Ubicación
**Todos los servicios del backend**

### 🎯 Propósito
Inversión de control para desacoplar dependencias.

### 💻 Implementación

#### Usando @RequiredArgsConstructor (Lombok)

```java
@Service
@RequiredArgsConstructor  // ← Genera constructor con dependencias
@Slf4j
public class AuthService {
    
    // Inyección por constructor (inmutable, testeable)
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogger auditLogger;  // ← Inyectado
    
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Usa dependencias inyectadas
        authenticationManager.authenticate(...);
        auditLogger.logLoginSuccess(...);
    }
}
```

#### Inyección Manual (sin Lombok)

```java
@Service
public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final AuditLogger auditLogger;
    
    // Constructor para DI
    public PacienteService(
            PacienteRepository pacienteRepository,
            PropietarioRepository propietarioRepository,
            AuditLogger auditLogger) {
        this.pacienteRepository = pacienteRepository;
        this.propietarioRepository = propietarioRepository;
        this.auditLogger = auditLogger;
    }
}
```

### ✅ Beneficios
- Desacoplamiento
- Fácil testing (mock dependencies)
- Código más limpio
- Inmutabilidad (final)

### 📊 Diagrama de Dependencias

```
┌─────────────────────────────────────┐
│         AuthService                 │
│  ┌───────────────────────────────┐  │
│  │ Dependencies (injected):      │  │
│  │ - AuthenticationManager       │  │
│  │ - UserDetailsService          │  │
│  │ - JwtUtil                     │  │
│  │ - UsuarioRepository           │  │
│  │ - AuditLogger ←────────────┐  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
                                   │
                                   │ Injected
                                   │
┌──────────────────────────────────▼──┐
│         AuditLogger (Service)       │
│  ┌───────────────────────────────┐  │
│  │ - SLF4J Logger                │  │
│  │ - MDC Management              │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

---

## 8. Decorator Pattern

### 📍 Ubicación
**Frontend**: `apps/frontend/src/core/api/axios.ts` (Axios Interceptors)

### 🎯 Propósito
Añadir funcionalidad (logging) a objetos (requests) dinámicamente sin modificar su estructura.

### 💻 Implementación

```typescript
// Instancia base de Axios
const axiosInstance = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
});

// DECORADOR 1: Request Interceptor (añade logging)
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Decorar config con metadata
    const correlationId = generateCorrelationId();
    config.headers['X-Correlation-ID'] = correlationId;
    
    (config as any).metadata = { 
      startTime: Date.now(),
      correlationId 
    };
    
    // Añadir funcionalidad de logging
    loggerService.logApiRequest(config.method, config.url, correlationId);
    
    return config;  // Request decorado
  }
);

// DECORADOR 2: Response Interceptor (añade logging y manejo de errores)
axiosInstance.interceptors.response.use(
  (response) => {
    // Añadir funcionalidad de logging
    const duration = Date.now() - response.config.metadata.startTime;
    loggerService.logApiResponse(method, url, status, duration, correlationId);
    
    // Añadir detección de requests lentos
    if (duration > 3000) {
      loggerService.warn(`Slow API call: ${duration}ms`);
    }
    
    return response;  // Response decorado
  },
  (error: AxiosError) => {
    // Añadir funcionalidad de manejo de errores
    loggerService.logApiError(...);
    
    if (error.response?.status === 401) {
      // Logout automático
    }
    
    return Promise.reject(error);
  }
);
```

### 📊 Decorador de Request/Response

```
Request Original
      │
      ▼
┌──────────────────────────────┐
│ Decorator 1: Add Metadata    │ → Correlation ID, Timestamp
└──────────────────────────────┘
      │
      ▼
┌──────────────────────────────┐
│ Decorator 2: Add Logging     │ → Log outgoing request
└──────────────────────────────┘
      │
      ▼
   HTTP Call
      │
      ▼
┌──────────────────────────────┐
│ Decorator 3: Log Response    │ → Log duration, status
└──────────────────────────────┘
      │
      ▼
┌──────────────────────────────┐
│ Decorator 4: Error Handling  │ → Log errors, auto-logout
└──────────────────────────────┘
      │
      ▼
Response Decorado
```

### ✅ Beneficios
- Añade funcionalidad sin modificar Axios
- Composición de funcionalidades
- Reutilizable
- Fácil de activar/desactivar

---

## 9. Observer Pattern

### 📍 Ubicación
**Frontend**: `apps/frontend/src/shared/components/common/ErrorBoundary.tsx`

### 🎯 Propósito
Observar errores de React y reaccionar (logging, mostrar UI de fallback).

### 💻 Implementación

```typescript
export class ErrorBoundary extends Component<Props, State> {
  
  // Observer: Escucha errores
  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    
    // Reacción 1: Log del error
    loggerService.error(
      'React Error Boundary caught an error',
      error,
      {
        componentStack: errorInfo.componentStack,
        errorBoundary: true
      }
    );
    
    // Reacción 2: Callback personalizado
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
    
    // Reacción 3: Actualizar UI
    this.setState({
      hasError: true,
      error,
      errorInfo
    });
  }
  
  render(): ReactNode {
    if (this.state.hasError) {
      // Mostrar UI de fallback
      return <ErrorFallbackUI error={this.state.error} />;
    }
    
    return this.props.children;
  }
}
```

### 📊 Observer Flow

```
React Components
      │
      ▼
  [Error occurs]
      │
      ▼
┌──────────────────────────────┐
│    ErrorBoundary (Observer)  │
│                              │
│  componentDidCatch(error) {  │
│    1. Notify Logger          │ → loggerService.error()
│    2. Notify Parent          │ → props.onError()
│    3. Update UI              │ → setState({hasError: true})
│  }                           │
└──────────────────────────────┘
      │
      ├─→ Logger Service → Backend
      ├─→ Parent Component
      └─→ UI Fallback
```

### ✅ Beneficios
- Captura errores automáticamente
- Múltiples reacciones posibles
- Desacoplado de componentes
- Evita que la app crashee

---

## 10. Factory Pattern

### 📍 Ubicación
**Backend**: SLF4J `LoggerFactory`

### 🎯 Propósito
Crear instancias de loggers sin especificar la clase concreta.

### 💻 Implementación

```java
@Service
public class AuditLogger {
    
    // Factory Method: crea logger según el nombre
    private static final Logger auditLogger = 
        LoggerFactory.getLogger("com.clinica.veterinaria.audit");
    
    private static final Logger performanceLogger = 
        LoggerFactory.getLogger("com.clinica.veterinaria.performance");
}
```

### 📊 Factory Diagram

```
LoggerFactory (Factory)
      │
      ├─→ getLogger("audit") → Logger for Audit
      ├─→ getLogger("performance") → Logger for Performance
      ├─→ getLogger(MyClass.class) → Logger for MyClass
      └─→ getLogger("app") → Logger for Application
```

### ✅ Beneficios
- No necesitas saber la implementación concreta
- Fácil cambiar implementación (Logback, Log4j, etc.)
- Configuración centralizada

---

## 11. MDC (Mapped Diagnostic Context) Pattern

### 📍 Ubicación
**Backend**: 
- `CorrelationIdFilter.java`
- `RequestResponseLoggingInterceptor.java`
- `AuditLogger.java`

### 🎯 Propósito
Patrón para propagar contexto (Correlation ID, User ID) a través de toda la cadena de ejecución sin pasarlo explícitamente.

### 💻 Implementación

```java
@Component
public class CorrelationIdFilter implements Filter {
    
    @Override
    public void doFilter(...) {
        try {
            String correlationId = generateCorrelationId();
            
            // Añadir al MDC (Thread-local storage)
            MDC.put("correlationId", correlationId);
            MDC.put("clientIp", getClientIp(request));
            
            // Continuar ejecución
            // TODOS los logs en este thread tendrán el correlationId
            chain.doFilter(request, response);
            
        } finally {
            // IMPORTANTE: Limpiar MDC
            MDC.clear();
        }
    }
}
```

```java
@Component
public class RequestResponseLoggingInterceptor {
    
    @Override
    public boolean preHandle(...) {
        // Añadir más contexto al MDC
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("requestMethod", request.getMethod());
        
        if (authentication != null) {
            MDC.put("username", authentication.getName());
            MDC.put("userId", getUserId());
        }
        
        return true;
    }
}
```

### 🔍 Propagación Automática

```java
// En cualquier parte del código, DESPUÉS del filtro
@Service
public class PacienteService {
    
    public void create(PacienteDTO dto) {
        // Este log automáticamente incluye:
        // - correlationId
        // - clientIp
        // - requestUri
        // - requestMethod
        // - username
        // - userId
        log.info("Creando paciente");  // ← No necesitas pasar el contexto!
    }
}
```

### 📊 MDC Flow

```
Request arrives
      │
      ▼
┌──────────────────────────────┐
│ CorrelationIdFilter          │
│ MDC.put("correlationId", ID) │
│ MDC.put("clientIp", IP)      │
└──────────────────────────────┘
      │
      ▼
┌──────────────────────────────┐
│ RequestResponseInterceptor   │
│ MDC.put("username", user)    │
│ MDC.put("requestUri", uri)   │
└──────────────────────────────┘
      │
      ▼
┌──────────────────────────────┐
│ Controller                   │
│ log.info("Processing")       │ → [correlationId=abc] [username=admin] Processing
└──────────────────────────────┘
      │
      ▼
┌──────────────────────────────┐
│ Service                      │
│ log.info("Creating entity")  │ → [correlationId=abc] [username=admin] Creating entity
└──────────────────────────────┘
      │
      ▼
┌──────────────────────────────┐
│ Repository (Hibernate)       │
│ SQL log                      │ → [correlationId=abc] [username=admin] SELECT ...
└──────────────────────────────┘
```

### ✅ Beneficios
- Propagación automática de contexto
- No necesitas pasar parámetros extra
- Thread-safe
- Todos los logs tienen el mismo contexto

---

## 📊 Resumen de Patrones por Capa

### Backend

| Capa | Patrones | Archivos |
|------|----------|----------|
| **Filters** | Interceptor, Chain of Responsibility, Template Method | `CorrelationIdFilter.java`, `JwtAuthenticationFilter.java` |
| **Interceptors** | Interceptor, Observer | `RequestResponseLoggingInterceptor.java` |
| **Services** | Facade, DI, Singleton | `AuditLogger.java`, `AuthService.java`, `PacienteService.java` |
| **Configuration** | Strategy, Factory | `logback-spring.xml`, `WebMvcConfig.java` |
| **Context** | MDC Pattern | Todos los componentes que usan logging |

### Frontend

| Capa | Patrones | Archivos |
|------|----------|----------|
| **Services** | Singleton, Facade | `loggerService.ts` |
| **HTTP** | Decorator, Interceptor | `axios.ts` |
| **Components** | Observer | `ErrorBoundary.tsx` |

---

## 🎯 Diagrama de Arquitectura Completa

```
┌─────────────────────────────────────────────────────────────────┐
│                           FRONTEND                               │
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │ LoggerService│    │ ErrorBoundary│    │ Axios        │     │
│  │ (Singleton)  │    │ (Observer)   │    │ (Decorator)  │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
│         │                   │                    │              │
└─────────┼───────────────────┼────────────────────┼──────────────┘
          │                   │                    │
          │          Correlation ID (MDC Pattern)  │
          │                   │                    │
┌─────────▼───────────────────▼────────────────────▼──────────────┐
│                           BACKEND                                │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │        Chain of Responsibility (Filters)                 │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
│  │  │Correlation  │→ │JWT Filter   │→ │Interceptor  │     │  │
│  │  │ID Filter    │  │(Template)   │  │(Interceptor)│     │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            ↓                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │               Services (DI, Facade)                       │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
│  │  │AuthService  │  │PacienteServ │  │AuditLogger  │     │  │
│  │  │(DI)         │  │(DI)         │  │(Facade)     │     │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            ↓                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            Logback (Strategy, Factory)                    │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐        │  │
│  │  │Console     │  │File        │  │Audit       │        │  │
│  │  │Appender    │  │Appender    │  │Appender    │        │  │
│  │  └────────────┘  └────────────┘  └────────────┘        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎓 Conclusiones

### Patrones Clave Implementados:
1. ✅ **Singleton** - Logger centralizado
2. ✅ **Interceptor/Filter** - Logging transversal
3. ✅ **Chain of Responsibility** - Cadena de filtros
4. ✅ **Strategy** - Diferentes configuraciones por entorno
5. ✅ **Facade** - API simplificada de auditoría
6. ✅ **Template Method** - Filtros reutilizables
7. ✅ **Dependency Injection** - Desacoplamiento
8. ✅ **Decorator** - Enriquecimiento de requests
9. ✅ **Observer** - Captura de errores
10. ✅ **Factory** - Creación de loggers
11. ✅ **MDC** - Propagación de contexto

### Beneficios de Esta Arquitectura:
- 🎯 **Código limpio y mantenible**
- 🔄 **Fácil de extender**
- 🧪 **Testeable**
- 📊 **Escalable**
- 🔒 **Seguro**
- 📚 **Bien documentado**

---

**Fecha**: 2024-11-06  
**Versión**: 1.0.0

