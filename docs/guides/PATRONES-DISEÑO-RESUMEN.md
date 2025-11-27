# 🎨 Patrones de Diseño - Resumen Visual

## 📋 11 Patrones Implementados

| # | Patrón | Ubicación | Propósito |
|---|--------|-----------|-----------|
| 1️⃣ | **Singleton** | `apps/frontend/src/core/logging/loggerService.ts` | Una sola instancia del logger en todo el frontend |
| 2️⃣ | **Interceptor/Filter** | `apps/backend/.../logging/CorrelationIdFilter.java`<br>`apps/backend/.../logging/RequestResponseLoggingInterceptor.java`<br>`apps/frontend/src/core/api/axios.ts` | Interceptar requests para añadir logging sin modificar código de negocio |
| 3️⃣ | **Chain of Responsibility** | Cadena de filtros de Spring | Pasar requests por cadena de handlers |
| 4️⃣ | **Strategy** | `apps/backend/src/main/resources/logback-spring.xml` | Diferentes estrategias de logging por entorno (dev/test/prod) |
| 5️⃣ | **Facade** | `apps/backend/.../logging/AuditLogger.java` | API simplificada para auditoría, oculta complejidad |
| 6️⃣ | **Template Method** | `apps/backend/.../security/JwtAuthenticationFilter.java` | Define esqueleto de algoritmo en clase base |
| 7️⃣ | **Dependency Injection** | Todos los servicios (`@RequiredArgsConstructor`) | Inversión de control, desacoplamiento |
| 8️⃣ | **Decorator** | `apps/frontend/src/core/api/axios.ts` (interceptors) | Añade funcionalidad (logging) sin modificar objeto |
| 9️⃣ | **Observer** | `apps/frontend/src/shared/components/common/ErrorBoundary.tsx` | Observa y reacciona a errores de React |
| 🔟 | **Factory** | `LoggerFactory.getLogger()` (SLF4J) | Crea loggers sin especificar clase concreta |
| 1️⃣1️⃣ | **MDC Pattern** | `CorrelationIdFilter`, `RequestResponseLoggingInterceptor` | Propaga contexto automáticamente en todos los logs |

---

## 🎯 Mapa Visual de Patrones

```
┌─────────────────────────────────────────────────────────────────────┐
│                            FRONTEND                                  │
│                                                                      │
│  [Singleton]              [Observer]              [Decorator]       │
│  LoggerService    ──→    ErrorBoundary    ──→    Axios Interceptors│
│       │                        │                        │            │
│       └────────────────────────┴────────────────────────┘            │
│                               │                                      │
│                    Correlation ID (MDC)                              │
│                               │                                      │
└───────────────────────────────┼──────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            BACKEND                                   │
│                                                                      │
│  [Chain of Responsibility] ──→ Filtros en orden                     │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ [Interceptor/Filter]    [Template Method]    [MDC Pattern] │    │
│  │ CorrelationIdFilter  →  JwtAuthFilter  →  Interceptor      │    │
│  └────────────────────────────────────────────────────────────┘    │
│                               │                                      │
│                               ▼                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ [Dependency Injection]                        [Facade]     │    │
│  │ AuthService  →  PacienteService  →  AuditLogger            │    │
│  └────────────────────────────────────────────────────────────┘    │
│                               │                                      │
│                               ▼                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │ [Strategy]                              [Factory]          │    │
│  │ Logback (dev/test/prod)  →  LoggerFactory                 │    │
│  └────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 Ejemplos Rápidos de Cada Patrón

### 1️⃣ Singleton
```typescript
// apps/frontend/src/core/logging/loggerService.ts
class LoggerService { /* ... */ }
export const loggerService = new LoggerService(); // ← Única instancia
```

### 2️⃣ Interceptor/Filter
```java
// apps/backend/.../logging/CorrelationIdFilter.java
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        // Interceptar ANTES
        MDC.put("correlationId", generateId());
        chain.doFilter(request, response); // ← Continuar
        // Interceptar DESPUÉS
        MDC.clear();
    }
}
```

### 3️⃣ Chain of Responsibility
```java
Request → CorrelationIdFilter → JwtAuthFilter → RequestInterceptor → Controller
```

### 4️⃣ Strategy
```xml
<!-- logback-spring.xml -->
<springProfile name="dev">
    <root level="INFO">
        <appender-ref ref="CONSOLE"/> <!-- Estrategia: Texto plano -->
    </root>
</springProfile>

<springProfile name="prod">
    <root level="INFO">
        <appender-ref ref="CONSOLE_JSON"/> <!-- Estrategia: JSON -->
    </root>
</springProfile>
```

### 5️⃣ Facade
```java
// apps/backend/.../logging/AuditLogger.java
@Service
public class AuditLogger {
    // Oculta complejidad de MDC, sanitización, formato
    public void logCreate(String entity, Object id, Object data) {
        MDC.put("action", "CREATE");
        auditLogger.info("✓ CREATED {} with ID {}", entity, id);
        MDC.remove("action");
    }
}

// Uso simple:
auditLogger.logCreate("Paciente", 123, data); // ← API simple
```

### 6️⃣ Template Method
```java
// JwtAuthenticationFilter.java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) { // ← Template Method
        // Paso 1: Extraer token
        // Paso 2: Validar
        // Paso 3: Autenticar
        // Paso 4: Continuar
    }
}
```

### 7️⃣ Dependency Injection
```java
@Service
@RequiredArgsConstructor // ← Genera constructor con dependencias
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuditLogger auditLogger; // ← Inyectado
}
```

### 8️⃣ Decorator
```typescript
// apps/frontend/src/core/api/axios.ts
axiosInstance.interceptors.request.use(
  (config) => {
    // Decorar request con logging
    config.headers['X-Correlation-ID'] = generateId();
    loggerService.logApiRequest(config);
    return config; // ← Request decorado
  }
);
```

### 9️⃣ Observer
```typescript
// ErrorBoundary.tsx
export class ErrorBoundary extends Component {
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Observar error y reaccionar:
    loggerService.error('Error caught', error); // Reacción 1
    this.props.onError?.(error, errorInfo);     // Reacción 2
    this.setState({ hasError: true });          // Reacción 3
  }
}
```

### 🔟 Factory
```java
private static final Logger auditLogger = 
    LoggerFactory.getLogger("com.clinica.veterinaria.audit"); // ← Factory
```

### 1️⃣1️⃣ MDC Pattern
```java
// CorrelationIdFilter.java
MDC.put("correlationId", "abc-123");
MDC.put("username", "admin");

// En CUALQUIER parte del código después:
log.info("Procesando request"); 
// → [correlationId=abc-123] [username=admin] Procesando request
// ¡Sin pasar parámetros! Propagación automática
```

---

## 📊 Patrones por Categoría

### Patrones Creacionales
| Patrón | Implementación |
|--------|----------------|
| **Singleton** | `loggerService` (Frontend) |
| **Factory** | `LoggerFactory.getLogger()` (Backend) |
| **Dependency Injection** | `@RequiredArgsConstructor`, `@Autowired` |

### Patrones Estructurales
| Patrón | Implementación |
|--------|----------------|
| **Facade** | `AuditLogger` - API simplificada |
| **Decorator** | Axios Interceptors - Añadir funcionalidad |

### Patrones Comportamiento
| Patrón | Implementación |
|--------|----------------|
| **Interceptor/Filter** | `CorrelationIdFilter`, `RequestResponseLoggingInterceptor` |
| **Chain of Responsibility** | Cadena de filtros de Spring |
| **Strategy** | Logback con perfiles (dev/test/prod) |
| **Template Method** | `OncePerRequestFilter`, `HandlerInterceptor` |
| **Observer** | `ErrorBoundary` - Observa errores de React |

### Patrones Específicos
| Patrón | Implementación |
|--------|----------------|
| **MDC (Mapped Diagnostic Context)** | Propagación automática de contexto en logs |

---

## 🎯 Beneficios de Usar Estos Patrones

| Beneficio | Patrones que lo Proporcionan |
|-----------|------------------------------|
| **Código Limpio** | Facade, DI, Singleton |
| **Reutilización** | Template Method, Strategy, Interceptor |
| **Desacoplamiento** | DI, Facade, Observer |
| **Extensibilidad** | Chain of Responsibility, Strategy, Decorator |
| **Mantenibilidad** | Facade, DI, MDC |
| **Testabilidad** | DI, Facade, Strategy |
| **Consistencia** | Interceptor, Template Method, Facade |
| **Escalabilidad** | Chain of Responsibility, MDC, Strategy |

---

## 📚 Dónde Encontrar Más Información

Para documentación detallada de cada patrón con diagramas completos:
📖 **`docs/architecture/LOGGING-DESIGN-PATTERNS.md`** (46 KB)

---

## ✅ Checklist de Patrones

- [x] **Singleton** - Logger único en frontend
- [x] **Interceptor/Filter** - Logging automático de requests
- [x] **Chain of Responsibility** - Cadena de filtros ordenados
- [x] **Strategy** - Configuración por entorno
- [x] **Facade** - API simplificada de auditoría
- [x] **Template Method** - Estructura reutilizable de filtros
- [x] **Dependency Injection** - Desacoplamiento total
- [x] **Decorator** - Enriquecimiento de requests
- [x] **Observer** - Captura de errores React
- [x] **Factory** - Creación flexible de loggers
- [x] **MDC** - Propagación automática de contexto

---

**Total de Patrones Implementados: 11** ✅  
**Cobertura de Categorías: 100%** (Creacionales, Estructurales, Comportamiento)  
**Nivel de Profesionalismo: Enterprise** 🏢

---

**Autor**: Sistema de Logging Profesional  
**Fecha**: 2024-11-06  
**Versión**: 1.0.0

