# 📂 Mapa de Archivos - Patrones de Diseño

## 🗺️ Estructura del Proyecto con Patrones

```
clinica-veterinaria/
│
├── apps/
│   │
│   ├── backend/
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/clinica/veterinaria/
│   │       │   │   │
│   │       │   │   ├── logging/                    [LOGGING CORE]
│   │       │   │   │   │
│   │       │   │   │   ├── CorrelationIdFilter.java
│   │       │   │   │   │   └── 🎯 Patrones:
│   │       │   │   │   │       • Interceptor/Filter
│   │       │   │   │   │       • Chain of Responsibility
│   │       │   │   │   │       • MDC Pattern
│   │       │   │   │   │
│   │       │   │   │   ├── RequestResponseLoggingInterceptor.java
│   │       │   │   │   │   └── 🎯 Patrones:
│   │       │   │   │   │       • Interceptor
│   │       │   │   │   │       • Observer
│   │       │   │   │   │       • MDC Pattern
│   │       │   │   │   │
│   │       │   │   │   └── AuditLogger.java
│   │       │   │   │       └── 🎯 Patrones:
│   │       │   │   │           • Facade (principal)
│   │       │   │   │           • Factory (usa LoggerFactory)
│   │       │   │   │           • Singleton (Logger estático)
│   │       │   │   │           • MDC Pattern
│   │       │   │   │
│   │       │   │   ├── config/
│   │       │   │   │   └── WebMvcConfig.java
│   │       │   │   │       └── 🎯 Patrones:
│   │       │   │   │           • Chain of Responsibility (configura orden)
│   │       │   │   │
│   │       │   │   ├── controller/
│   │       │   │   │   ├── LogController.java
│   │       │   │   │   │   └── 🎯 Patrones:
│   │       │   │   │   │       • Dependency Injection
│   │       │   │   │   │
│   │       │   │   │   ├── AuthController.java        [Ya existía]
│   │       │   │   │   ├── PropietarioController.java [Ya existía]
│   │       │   │   │   └── ...
│   │       │   │   │
│   │       │   │   ├── service/
│   │       │   │   │   ├── AuthService.java           [MEJORADO ✨]
│   │       │   │   │   │   └── 🎯 Patrones:
│   │       │   │   │   │       • Dependency Injection (principal)
│   │       │   │   │   │       • Facade (usa AuditLogger)
│   │       │   │   │   │
│   │       │   │   │   ├── PacienteService.java       [MEJORADO ✨]
│   │       │   │   │   │   └── 🎯 Patrones:
│   │       │   │   │   │       • Dependency Injection (principal)
│   │       │   │   │   │       • Facade (usa AuditLogger)
│   │       │   │   │   │
│   │       │   │   │   └── ...
│   │       │   │   │
│   │       │   │   ├── security/
│   │       │   │   │   ├── JwtAuthenticationFilter.java [Ya existía]
│   │       │   │   │   │   └── 🎯 Patrones:
│   │       │   │   │   │       • Template Method (principal)
│   │       │   │   │   │       • Chain of Responsibility
│   │       │   │   │   │       • Interceptor/Filter
│   │       │   │   │   │
│   │       │   │   │   └── ...
│   │       │   │   │
│   │       │   │   └── repository/                    [Sin cambios]
│   │       │   │       └── *.java (interfaces - no necesitan patrones)
│   │       │   │
│   │       │   └── resources/
│   │       │       │
│   │       │       ├── logback-spring.xml             [NUEVO ✨]
│   │       │       │   └── 🎯 Patrones:
│   │       │       │       • Strategy (principal - dev/test/prod)
│   │       │       │       • Factory (Appenders)
│   │       │       │
│   │       │       └── application.properties         [MEJORADO ✨]
│   │       │           └── Configuración de logging mejorada
│   │       │
│   │       └── pom.xml                                [MEJORADO ✨]
│   │           └── Dependencias de logging añadidas
│   │
│   └── frontend/
│       └── src/
│           │
│           ├── core/
│           │   │
│           │   ├── logging/
│           │   │   └── loggerService.ts               [NUEVO ✨]
│           │   │       └── 🎯 Patrones:
│           │   │           • Singleton (principal)
│           │   │           • Facade
│           │   │           • Observer
│           │   │
│           │   └── api/
│           │       └── axios.ts                       [MEJORADO ✨]
│           │           └── 🎯 Patrones:
│           │               • Decorator (principal)
│           │               • Interceptor
│           │
│           └── shared/
│               └── components/
│                   └── common/
│                       └── ErrorBoundary.tsx          [NUEVO ✨]
│                           └── 🎯 Patrones:
│                               • Observer (principal)
│
└── docs/
    └── architecture/
        └── LOGGING-DESIGN-PATTERNS.md                 [NUEVO ✨]
            └── 📚 Documentación completa de patrones
```

---

## 🎨 Leyenda de Patrones por Archivo

### Backend - Logging Core

#### 📄 `CorrelationIdFilter.java`
```java
Líneas clave:
  23: @Component @Order(1)     → Chain of Responsibility
  33: public void doFilter()   → Interceptor/Filter
  53: MDC.put("correlationId") → MDC Pattern
```

**Patrones:**
- 🎯 **Interceptor/Filter** (Principal) - Intercepta requests
- 🔗 **Chain of Responsibility** - Orden de ejecución
- 📊 **MDC Pattern** - Propaga Correlation ID

---

#### 📄 `RequestResponseLoggingInterceptor.java`
```java
Líneas clave:
  32: @Component               → Spring Component
  51: preHandle()              → Interceptor (antes del request)
  67: afterCompletion()        → Interceptor (después del request)
  87: MDC.put("requestUri")    → MDC Pattern
```

**Patrones:**
- 🎯 **Interceptor** (Principal) - Logging de requests/responses
- 👁️ **Observer** - Observa duration y slow requests
- 📊 **MDC Pattern** - Añade contexto a logs

---

#### 📄 `AuditLogger.java`
```java
Líneas clave:
  27: @Service                                    → Spring Service
  30: LoggerFactory.getLogger("audit")            → Factory
  36: public void logCreate()                     → Facade (API simple)
  41:   MDC.put("action", "CREATE")               → MDC Pattern
  43:   auditLogger.info("✓ CREATED...")          → Logging
  47:   MDC.remove("action")                      → MDC Cleanup
```

**Patrones:**
- 🎯 **Facade** (Principal) - API simplificada
- 🏭 **Factory** - LoggerFactory
- 🔄 **Singleton** - Logger estático
- 📊 **MDC Pattern** - Contexto de auditoría

---

### Backend - Security

#### 📄 `JwtAuthenticationFilter.java`
```java
Líneas clave:
  23: extends OncePerRequestFilter              → Template Method
  32: protected void doFilterInternal()         → Template Method hook
  45:   jwt = authorizationHeader.substring(7)  → Paso 1: Extraer token
  47:   username = jwtUtil.extractUsername()    → Paso 2: Validar
  63:   SecurityContext.setAuthentication()     → Paso 3: Autenticar
```

**Patrones:**
- 🎯 **Template Method** (Principal) - Esqueleto de algoritmo
- 🎯 **Interceptor/Filter** - Intercepta requests
- 🔗 **Chain of Responsibility** - Parte de cadena de filtros

---

### Backend - Configuration

#### 📄 `logback-spring.xml`
```xml
Líneas clave:
  178: <springProfile name="dev">     → Strategy: Configuración dev
  186: <springProfile name="test">    → Strategy: Configuración test
  194: <springProfile name="prod">    → Strategy: Configuración prod
  
  13: <appender name="CONSOLE">       → Factory: Console Appender
  24: <appender name="CONSOLE_JSON">  → Factory: JSON Appender
  38: <appender name="FILE">          → Factory: File Appender
```

**Patrones:**
- 🎯 **Strategy** (Principal) - Diferentes estrategias por perfil
- 🏭 **Factory** - Creación de appenders

---

### Frontend - Core

#### 📄 `loggerService.ts`
```typescript
Líneas clave:
  24: class LoggerService {            → Clase principal
  25:   private logs: LogEntry[]       → State privado
  
  44:   debug() / info() / warn()      → Facade (API simple)
  
  298: export const loggerService      → Singleton (única instancia)
       = new LoggerService();
  
  302: (window as any).logger          → Global access
       = loggerService;
```

**Patrones:**
- 🎯 **Singleton** (Principal) - Única instancia
- 🎨 **Facade** - API simple para logging
- 👁️ **Observer** - Observa eventos para logging

---

#### 📄 `axios.ts`
```typescript
Líneas clave:
  10: const axiosInstance = axios.create() → Base object
  
  18: interceptors.request.use(          → Decorator 1: Request
  20:   (config) => {
  22:     config.headers['X-Correlation-ID'] = id;
  32:     config.metadata = { startTime };
  38:     loggerService.logApiRequest();
  
  56: interceptors.response.use(         → Decorator 2: Response
  57:   (response) => {
  64:     loggerService.logApiResponse();
  
  84:   (error) => {                     → Decorator 3: Error
  93:     loggerService.logApiError();
```

**Patrones:**
- 🎯 **Decorator** (Principal) - Añade funcionalidad a Axios
- 🎯 **Interceptor** - Intercepta requests/responses
- 📊 **MDC** (conceptual) - Correlation ID

---

### Frontend - Components

#### 📄 `ErrorBoundary.tsx`
```typescript
Líneas clave:
  22: export class ErrorBoundary        → React Component
       extends Component
  
  33:   componentDidCatch(error) {      → Observer: Observa errores
  36:     loggerService.error(...);     → Reacción 1: Log
  45:     this.props.onError?.(...);    → Reacción 2: Callback
  48:     this.setState({ hasError });  → Reacción 3: UI
```

**Patrones:**
- 🎯 **Observer** (Principal) - Observa y reacciona a errores

---

## 📊 Tabla Resumen: Archivo → Patrones

| Archivo | Patrón Principal | Patrones Secundarios | Líneas Clave |
|---------|------------------|---------------------|--------------|
| `CorrelationIdFilter.java` | Interceptor/Filter | Chain of Responsibility, MDC | 23, 33, 53 |
| `RequestResponseLoggingInterceptor.java` | Interceptor | Observer, MDC | 51, 67, 87 |
| `AuditLogger.java` | Facade | Factory, Singleton, MDC | 30, 36, 41 |
| `JwtAuthenticationFilter.java` | Template Method | Interceptor, Chain | 23, 32, 45 |
| `WebMvcConfig.java` | Chain of Responsibility | - | 20 |
| `logback-spring.xml` | Strategy | Factory | 178, 186, 194 |
| `loggerService.ts` | Singleton | Facade, Observer | 24, 44, 298 |
| `axios.ts` | Decorator | Interceptor | 18, 56, 84 |
| `ErrorBoundary.tsx` | Observer | - | 33 |
| Todos los Services | Dependency Injection | - | @RequiredArgsConstructor |

---

## 🔍 Cómo Buscar un Patrón Específico

### Quiero ver el patrón **Singleton**:
```bash
# Frontend
code apps/frontend/src/core/logging/loggerService.ts
# Ir a línea 298: export const loggerService = new LoggerService();
```

### Quiero ver el patrón **Facade**:
```bash
# Backend
code apps/backend/src/main/java/com/clinica/veterinaria/logging/AuditLogger.java
# Ver métodos como logCreate(), logDelete(), etc.
```

### Quiero ver el patrón **Interceptor**:
```bash
# Backend
code apps/backend/src/main/java/com/clinica/veterinaria/logging/CorrelationIdFilter.java
code apps/backend/src/main/java/com/clinica/veterinaria/logging/RequestResponseLoggingInterceptor.java

# Frontend
code apps/frontend/src/core/api/axios.ts
# Ir a línea 18 y 56: interceptors.request.use / response.use
```

### Quiero ver el patrón **Strategy**:
```bash
# Backend
code apps/backend/src/main/resources/logback-spring.xml
# Ir a líneas 178-200: <springProfile name="dev|test|prod">
```

### Quiero ver el patrón **MDC**:
```bash
# Buscar en múltiples archivos:
grep -r "MDC.put" apps/backend/src/main/java/com/clinica/veterinaria/logging/
```

---

## 🎯 Archivos NUEVOS vs MODIFICADOS

### ✨ Archivos NUEVOS (Creados desde cero)

```
Backend:
✅ apps/backend/src/main/java/com/clinica/veterinaria/logging/
   ├── CorrelationIdFilter.java
   ├── RequestResponseLoggingInterceptor.java
   └── AuditLogger.java

✅ apps/backend/src/main/java/com/clinica/veterinaria/config/
   └── WebMvcConfig.java

✅ apps/backend/src/main/java/com/clinica/veterinaria/controller/
   └── LogController.java

✅ apps/backend/src/main/resources/
   └── logback-spring.xml

Frontend:
✅ apps/frontend/src/core/logging/
   └── loggerService.ts

✅ apps/frontend/src/shared/components/common/
   └── ErrorBoundary.tsx
```

### 🔄 Archivos MODIFICADOS (Mejorados)

```
Backend:
🔄 apps/backend/pom.xml
   └── Añadidas dependencias de logging

🔄 apps/backend/src/main/resources/application.properties
   └── Configuración de logging mejorada

🔄 apps/backend/src/main/java/com/clinica/veterinaria/service/
   ├── AuthService.java      (agregado AuditLogger)
   └── PacienteService.java  (agregado AuditLogger)

Frontend:
🔄 apps/frontend/src/core/api/axios.ts
   └── Añadidos interceptores de logging
```

### 📝 Archivos SIN CAMBIOS

```
Backend:
✓ Repositories (interfaces - no necesitan cambios)
✓ Entities (no necesitan logging)
✓ DTOs (no necesitan logging)
✓ La mayoría de Controllers (ya tienen @Slf4j)

Frontend:
✓ Otros servicios y componentes
✓ Pages (usan el loggerService automáticamente vía axios)
```

---

## 🎓 Cómo Navegar el Código

### Para entender el flujo completo:

1. **Empieza por el request:**
   ```
   Frontend (axios.ts) 
   → CorrelationIdFilter.java 
   → JwtAuthenticationFilter.java 
   → RequestResponseLoggingInterceptor.java 
   → Controller 
   → Service (usa AuditLogger) 
   → Repository
   ```

2. **Para ver logging de auditoría:**
   ```
   AuthService.java o PacienteService.java 
   → AuditLogger.java 
   → logback-spring.xml 
   → logs/audit.log
   ```

3. **Para ver cómo funciona MDC:**
   ```
   CorrelationIdFilter.java (pone en MDC) 
   → RequestResponseLoggingInterceptor.java (añade más al MDC) 
   → Cualquier log() en la aplicación (automáticamente incluye MDC)
   ```

---

**📚 Documentación Completa:**
- `docs/architecture/LOGGING-DESIGN-PATTERNS.md` - Explicación detallada
- `PATRONES-DISEÑO-RESUMEN.md` - Resumen visual
- `LOGGING-IMPLEMENTATION-SUMMARY.md` - Implementación completa

---

**Autor**: Sistema de Logging Profesional  
**Fecha**: 2024-11-06  
**Versión**: 1.0.0

