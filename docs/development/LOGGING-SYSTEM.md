# 📊 Sistema de Logging Profesional - Documentación Completa

## 🎯 Visión General

Este proyecto implementa un **sistema de logging profesional end-to-end** con trazabilidad completa desde el frontend hasta el backend, incluyendo:

- ✅ Logging estructurado en formato JSON
- ✅ Correlation IDs para trazabilidad de requests
- ✅ Logging de requests/responses HTTP
- ✅ Auditoría de eventos de negocio
- ✅ Logging de performance y queries lentas
- ✅ Logging centralizado del frontend
- ✅ Error Boundary en React
- ✅ Métricas y health checks

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Error        │  │ Logger       │  │ Axios        │      │
│  │ Boundary     │  │ Service      │  │ Interceptors │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                 │                  │               │
│         └─────────────────┴──────────────────┘               │
│                           │                                  │
│                  Correlation ID                              │
│                  X-Correlation-ID                            │
│                           │                                  │
└───────────────────────────┼──────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       BACKEND                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Correlation  │  │ Request/     │  │ Audit        │      │
│  │ ID Filter    │  │ Response     │  │ Logger       │      │
│  │              │  │ Interceptor  │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                 │                  │               │
│         └─────────────────┴──────────────────┘               │
│                           │                                  │
│                  ┌────────┴────────┐                         │
│                  │   SLF4J + MDC   │                         │
│                  └────────┬────────┘                         │
│                           │                                  │
│                  ┌────────┴────────┐                         │
│                  │     Logback     │                         │
│                  └────────┬────────┘                         │
│                           │                                  │
│         ┌─────────────────┼─────────────────┐               │
│         │                 │                 │               │
│    ┌────▼────┐      ┌────▼────┐      ┌────▼────┐          │
│    │ Console │      │ Files   │      │ Actuator│          │
│    │ (JSON)  │      │ (JSON)  │      │ Metrics │          │
│    └─────────┘      └─────────┘      └─────────┘          │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔧 Componentes del Backend

### 1. **Logback Configuration** (`logback-spring.xml`)

Configuración avanzada de Logback con múltiples appenders:

#### **Appenders Configurados:**

| Appender | Propósito | Formato | Archivo |
|----------|-----------|---------|---------|
| `CONSOLE` | Desarrollo - Logs legibles | Texto plano con colores | - |
| `CONSOLE_JSON` | Producción - Logs estructurados | JSON | - |
| `FILE` | Logs generales de la aplicación | JSON | `logs/application.log` |
| `ERROR_FILE` | Solo errores (ERROR level) | JSON | `logs/error.log` |
| `AUDIT_FILE` | Auditoría de eventos de negocio | JSON | `logs/audit.log` |
| `PERFORMANCE_FILE` | Métricas de performance | JSON | `logs/performance.log` |

#### **Características:**

- 🔄 **Rolling Policy**: Archivos rotan diariamente o al alcanzar 10MB
- 📦 **Compresión**: Archivos antiguos se comprimen en `.gz`
- 🗑️ **Retención**: 
  - Logs generales: 30 días
  - Logs de error: 90 días
  - Logs de auditoría: 180 días
  - Logs de performance: 30 días
- ⚡ **Async**: Appenders asíncronos para mejor rendimiento
- 🏷️ **Perfiles**: Configuración diferente para `dev`, `test`, `prod`

#### **Ejemplo de Log JSON:**

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger": "com.clinica.veterinaria.service.AuthService",
  "message": "✓ Login exitoso para usuario: admin@clinica.com",
  "application": "clinica-veterinaria-api",
  "correlationId": "1705315845123-abc123xyz",
  "userId": "admin@clinica.com",
  "username": "admin@clinica.com",
  "requestUri": "/api/auth/login",
  "requestMethod": "POST",
  "clientIp": "192.168.1.100",
  "userAgent": "Mozilla/5.0..."
}
```

---

### 2. **Correlation ID Filter** (`CorrelationIdFilter.java`)

Filtro que añade un ID único a cada request para trazabilidad end-to-end.

#### **Funcionamiento:**

1. Intercepta todos los requests HTTP
2. Busca header `X-Correlation-ID` del frontend
3. Si no existe, genera uno nuevo usando UUID
4. Añade el ID al MDC de SLF4J
5. Incluye el ID en la respuesta HTTP
6. Limpia el MDC al terminar

#### **Uso en Logs:**

```java
log.info("Procesando request"); 
// → [correlationId=abc-123] Procesando request
```

#### **Ventajas:**

- ✅ Trazar requests desde frontend hasta backend
- ✅ Correlacionar logs entre diferentes componentes
- ✅ Facilitar debugging en producción
- ✅ Identificar problemas en flujos complejos

---

### 3. **Request/Response Interceptor** (`RequestResponseLoggingInterceptor.java`)

Interceptor de Spring MVC que registra información detallada de cada request HTTP.

#### **Información Registrada:**

**En cada REQUEST:**
- Método HTTP (GET, POST, etc.)
- URI de la petición
- IP del cliente
- Usuario autenticado
- Query parameters
- Headers importantes (solo en DEBUG)
- Correlation ID

**En cada RESPONSE:**
- Código de estado HTTP
- Duración de la petición
- Detección de requests lentos (>1s)

#### **Ejemplo de Logs:**

```
→ Incoming POST /api/pacientes from 192.168.1.100 | User: admin@clinica.com | Correlation-ID: abc-123
← Response POST /api/pacientes | Status: 201 | Duration: 234ms

⚠️ SLOW REQUEST: GET /api/reportes/completo took 3456ms (threshold: 1000ms)
```

#### **Configuración:**

El interceptor está registrado en `WebMvcConfig.java` y se aplica a todos los endpoints excepto:
- `/swagger-ui/**`
- `/api-docs/**`
- `/actuator/**`
- `/error`

---

### 4. **Audit Logger** (`AuditLogger.java`)

Servicio especializado para auditoría de eventos de negocio.

#### **Métodos Disponibles:**

| Método | Uso | Ejemplo |
|--------|-----|---------|
| `logCreate()` | Creación de entidades | Nuevo paciente registrado |
| `logUpdate()` | Actualización de entidades | Datos de paciente modificados |
| `logDelete()` | Eliminación de entidades | Paciente desactivado |
| `logAccess()` | Acceso a información sensible | Consulta de historial médico |
| `logLoginSuccess()` | Login exitoso | Usuario autenticado |
| `logLoginFailure()` | Login fallido | Intento de acceso no autorizado |
| `logLogout()` | Cierre de sesión | Usuario cerró sesión |
| `logPermissionChange()` | Cambios de permisos | Rol de usuario modificado |
| `logDataExport()` | Exportación de datos | Reporte generado |
| `logStatusChange()` | Cambios de estado | Cita cancelada |
| `logCustomEvent()` | Eventos personalizados | Cualquier evento importante |
| `logSecurityEvent()` | Eventos de seguridad | Intento de acceso no autorizado |

#### **Ejemplo de Uso:**

```java
@Service
@RequiredArgsConstructor
public class PacienteService {
    private final AuditLogger auditLogger;
    
    public PacienteDTO create(PacienteDTO dto) {
        Paciente paciente = // ... crear paciente
        
        auditLogger.logCreate("Paciente", paciente.getId(), 
            String.format("Nombre: %s, Especie: %s", 
                paciente.getNombre(), paciente.getEspecie()));
        
        return dto;
    }
}
```

#### **Características:**

- 🔐 **Sanitización**: Automáticamente oculta información sensible (passwords, tokens)
- 👤 **Usuario**: Detecta automáticamente el usuario actual
- 📝 **Formato**: Logs estructurados con metadata rica
- 🎨 **Emojis**: Usa emojis para fácil identificación visual
- 📂 **Archivo dedicado**: `logs/audit.log` separado de otros logs

#### **Ejemplo de Log de Auditoría:**

```
✓ CREATED Paciente with ID 123 | User: admin@clinica.com | Data: Nombre: Rex, Especie: Perro
✎ UPDATED Paciente with ID 123 | User: vet@clinica.com | Old: Peso: 10.5kg | New: Peso: 11.2kg
⚠ DELETED Paciente with ID 123 | User: admin@clinica.com | Timestamp: 2024-01-15 10:30:45
```

---

### 5. **Logging de Base de Datos**

Configuración en `application.properties` para logging de Hibernate y queries:

```properties
# Logging de SQL y parámetros
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.hibernate.stat=DEBUG

# Estadísticas de Hibernate
spring.jpa.properties.hibernate.generate_statistics=true

# Logging de queries lentas (más de 100ms)
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=100

# Batch processing para mejor rendimiento
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

#### **Ejemplo de Logs de SQL:**

```sql
Hibernate: 
    select
        paciente0_.id as id1_2_,
        paciente0_.nombre as nombre2_2_,
        ...
    from
        pacientes paciente0_
    where
        paciente0_.propietario_id=?
binding parameter [1] as [BIGINT] - [5]
```

---

### 6. **Spring Boot Actuator**

Endpoints de métricas y health checks configurados:

```properties
management.endpoints.web.exposure.include=health,info,metrics,loggers,httptrace,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.web.server.request.autotime.enabled=true
```

#### **Endpoints Disponibles:**

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de salud de la aplicación |
| `/actuator/metrics` | Métricas de la aplicación |
| `/actuator/loggers` | Ver y cambiar niveles de log en runtime |
| `/actuator/httptrace` | Últimas peticiones HTTP |
| `/actuator/prometheus` | Métricas en formato Prometheus |

#### **Ejemplo:**

```bash
curl http://localhost:8080/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

---

## 🎨 Componentes del Frontend

### 1. **Logger Service** (`loggerService.ts`)

Servicio centralizado de logging para el frontend.

#### **Niveles de Log:**

- `DEBUG`: Solo en desarrollo
- `INFO`: Información general
- `WARN`: Advertencias
- `ERROR`: Errores

#### **Métodos Disponibles:**

```typescript
loggerService.debug('Mensaje de debug', { context: 'value' });
loggerService.info('Mensaje informativo', { context: 'value' });
loggerService.warn('Advertencia', { context: 'value' });
loggerService.error('Error', errorObject, { context: 'value' });

// Métodos especializados
loggerService.logApiRequest('POST', '/api/pacientes', correlationId, params);
loggerService.logApiResponse('POST', '/api/pacientes', 201, 234, correlationId);
loggerService.logApiError('POST', '/api/pacientes', 500, errorData, 234, correlationId);
loggerService.logUserEvent('button_clicked', { button: 'save' });
loggerService.logNavigation('/pacientes', '/pacientes/123');
loggerService.logAuth('login', 'user@example.com');
loggerService.logPerformance('page_load', 1234, 'ms');
```

#### **Características:**

- 🎨 **Logs con colores** en consola del navegador
- 📊 **Logs estructurados** con metadata
- 💾 **Buffer en memoria** de últimos 100 logs
- 💿 **Almacenamiento local** de últimos 50 logs
- 🔐 **Sanitización** automática de datos sensibles
- 📤 **Envío al backend** de errores y warnings
- 🐛 **Debugging** fácil con `window.logger`

#### **Ejemplo de Uso:**

```typescript
import { loggerService } from '@/core/logging/loggerService';

function savePaciente(data: PacienteDTO) {
  try {
    loggerService.info('Guardando paciente', { nombre: data.nombre });
    const result = await pacienteService.create(data);
    loggerService.info('Paciente guardado exitosamente', { id: result.id });
    return result;
  } catch (error) {
    loggerService.error('Error al guardar paciente', error, { data });
    throw error;
  }
}
```

#### **Debugging en Consola:**

```javascript
// Ver logs recientes
window.logger.getRecentLogs(20)

// Exportar todos los logs
window.logger.exportLogs()

// Limpiar logs
window.logger.clearLogs()
```

---

### 2. **Axios Interceptors** (`axios.ts`)

Interceptores de Axios para logging automático de todas las peticiones API.

#### **Request Interceptor:**

- Genera Correlation ID único para cada request
- Añade header `X-Correlation-ID`
- Añade token JWT si existe
- Registra timestamp para medir duración
- Log de request saliente

#### **Response Interceptor:**

- Calcula duración de la petición
- Log de response exitoso
- Detecta requests lentos (>3s)
- Log detallado de errores
- Manejo de errores por código de estado

#### **Ejemplo de Logs:**

```
🔍 [10:30:45] DEBUG → API Request: POST /api/pacientes
  {correlationId: "1705315845123-abc123", params: {...}}

ℹ️ [10:30:46] INFO ← API Response: POST /api/pacientes [201] 234ms
  {status: 201, duration: 234, correlationId: "1705315845123-abc123"}

⚠️ [10:30:50] WARN Slow API call detected: GET /api/reportes took 3456ms
  {duration: 3456, url: "/api/reportes", correlationId: "..."}
```

---

### 3. **Error Boundary** (`ErrorBoundary.tsx`)

Componente de React para capturar errores no manejados.

#### **Uso:**

```tsx
import { ErrorBoundary } from '@/shared/components/common/ErrorBoundary';

function App() {
  return (
    <ErrorBoundary>
      <YourApp />
    </ErrorBoundary>
  );
}
```

#### **Características:**

- ✅ Captura errores en render de componentes
- ✅ Muestra UI de fallback amigable
- ✅ Log automático del error
- ✅ Muestra detalles en desarrollo
- ✅ Botones para recuperación (reintentar, ir al inicio, recargar)
- ✅ Detecta múltiples errores consecutivos

#### **UI de Error:**

![Error Boundary Example](https://via.placeholder.com/600x300?text=Error+Boundary+UI)

---

### 4. **Endpoint de Logs** (`LogController.java`)

Endpoint en el backend para recibir logs del frontend.

#### **Endpoint:**

```
POST /api/logs/frontend
Content-Type: application/json
X-Correlation-ID: abc-123
```

#### **Body:**

```json
{
  "level": "ERROR",
  "message": "Error al cargar pacientes",
  "timestamp": "2024-01-15T10:30:45.123Z",
  "context": {
    "component": "PacientesList",
    "action": "fetchPacientes"
  },
  "error": {
    "name": "TypeError",
    "message": "Cannot read property 'id' of undefined",
    "stack": "..."
  },
  "url": "http://localhost:5173/pacientes",
  "userId": "admin@clinica.com",
  "correlationId": "1705315845123-abc123"
}
```

#### **Response:**

```json
{
  "status": "success",
  "message": "Log received",
  "timestamp": "2024-01-15 10:30:45"
}
```

---

## 📖 Guía de Uso

### Para Desarrolladores

#### **1. Agregar Logging en un Servicio:**

```java
@Service
@RequiredArgsConstructor
@Slf4j  // ← Anotación de Lombok
public class MiServicio {
    
    private final AuditLogger auditLogger;
    
    public void miMetodo() {
        log.info("→ Iniciando operación");
        
        try {
            // ... lógica
            
            log.info("✓ Operación exitosa");
            auditLogger.logCustomEvent("OPERACION_EXITOSA", "Detalles...");
            
        } catch (Exception e) {
            log.error("✗ Error en operación", e);
            throw e;
        }
    }
}
```

#### **2. Agregar Logging en el Frontend:**

```typescript
import { loggerService } from '@/core/logging/loggerService';

async function fetchData() {
  try {
    loggerService.info('Cargando datos');
    const data = await api.get('/endpoint');
    loggerService.info('Datos cargados', { count: data.length });
    return data;
  } catch (error) {
    loggerService.error('Error al cargar datos', error);
    throw error;
  }
}
```

#### **3. Debugging con Correlation ID:**

Cuando un usuario reporta un error:

1. Pide el Correlation ID (se muestra en la UI de error)
2. Busca en los logs del backend: `grep "correlationId=abc-123" logs/application.log`
3. Busca en los logs del frontend: `localStorage` → filtrar por Correlation ID
4. Traza todo el flujo de la petición

---

### Para Operaciones

#### **1. Monitorear Logs en Producción:**

```bash
# Ver logs en tiempo real
tail -f logs/application.log | jq .

# Ver solo errores
tail -f logs/error.log | jq '.message'

# Ver auditoría
tail -f logs/audit.log | jq 'select(.action == "LOGIN_FAILURE")'

# Ver requests lentos
tail -f logs/performance.log | jq 'select(.duration > 1000)'
```

#### **2. Análisis de Logs:**

```bash
# Contar errores por tipo
cat logs/error.log | jq -r '.logger' | sort | uniq -c | sort -rn

# Usuarios con más login failures
cat logs/audit.log | jq -r 'select(.action == "LOGIN_FAILURE") | .username' | sort | uniq -c | sort -rn

# Requests más lentos del día
cat logs/performance.log | jq -r '. | "\(.duration) \(.requestUri)"' | sort -rn | head -20
```

#### **3. Métricas con Actuator:**

```bash
# Ver métricas HTTP
curl http://localhost:8080/actuator/metrics/http.server.requests | jq .

# Ver uso de memoria
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq .

# Cambiar nivel de log en runtime
curl -X POST http://localhost:8080/actuator/loggers/com.clinica.veterinaria \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 🔍 Casos de Uso

### Caso 1: Usuario reporta error en login

**Problema**: Usuario no puede hacer login, dice que ve un error.

**Solución:**

1. Pedir Correlation ID al usuario (aparece en pantalla de error)
2. Buscar en logs de auditoría:
   ```bash
   cat logs/audit.log | jq 'select(.correlationId == "abc-123")'
   ```
3. Ver en logs del backend:
   ```bash
   cat logs/application.log | jq 'select(.correlationId == "abc-123")'
   ```
4. Identificar causa: ¿Usuario inactivo? ¿Credenciales inválidas? ¿Error de BD?

---

### Caso 2: Performance degradado

**Problema**: La aplicación está lenta.

**Solución:**

1. Ver requests lentos:
   ```bash
   cat logs/performance.log | jq 'select(.duration > 1000) | {uri: .requestUri, duration: .duration}' | head -20
   ```
2. Identificar endpoint problemático
3. Ver queries SQL lentas:
   ```bash
   cat logs/application.log | jq 'select(.logger == "org.hibernate.SQL" and .duration > 100)'
   ```
4. Optimizar query o añadir índice

---

### Caso 3: Auditoría de seguridad

**Problema**: Necesitas saber quién eliminó un paciente.

**Solución:**

1. Buscar en logs de auditoría:
   ```bash
   cat logs/audit.log | jq 'select(.action == "DELETE" and .entity == "Paciente" and .entityId == "123")'
   ```
2. Ver resultado:
   ```json
   {
     "action": "DELETE",
     "entity": "Paciente",
     "entityId": "123",
     "username": "admin@clinica.com",
     "timestamp": "2024-01-15T10:30:45.123Z",
     "correlationId": "abc-123"
   }
   ```
3. Identificado: `admin@clinica.com` eliminó el paciente el 15/01/2024 a las 10:30

---

## 🎓 Mejores Prácticas

### ✅ DO

- **Usa niveles apropiados**:
  - `DEBUG`: Información detallada solo para desarrollo
  - `INFO`: Operaciones importantes y flujo normal
  - `WARN`: Situaciones anormales pero manejables
  - `ERROR`: Errores que requieren atención

- **Incluye contexto**:
  ```java
  log.info("Paciente creado con ID: {} por usuario: {}", id, username);
  ```

- **Usa emojis para identificación rápida**:
  ```java
  log.info("✓ Operación exitosa");
  log.error("✗ Error en operación");
  log.warn("⚠️ Advertencia");
  ```

- **Audita eventos importantes**:
  ```java
  auditLogger.logCreate("Paciente", id, data);
  ```

### ❌ DON'T

- **No loguees información sensible**:
  ```java
  log.info("Password: {}", password); // ❌ NUNCA
  ```

- **No uses System.out.println**:
  ```java
  System.out.println("Debug"); // ❌ Usar log.debug()
  ```

- **No loguees en loops sin control**:
  ```java
  for (int i = 0; i < 10000; i++) {
    log.info("Processing {}", i); // ❌ Demasiados logs
  }
  ```

- **No captures excepciones sin logear**:
  ```java
  try {
    // ...
  } catch (Exception e) {
    // ❌ Excepción silenciada
  }
  ```

---

## 📊 Estructura de Archivos de Log

```
logs/
├── application.log           # Logs generales
├── error.log                 # Solo errores
├── audit.log                 # Auditoría
├── performance.log           # Performance
└── archive/                  # Archivos antiguos comprimidos
    ├── application-2024-01-14.0.log.gz
    ├── error-2024-01-14.0.log.gz
    └── ...
```

---

## 🚀 Próximas Mejoras

- [ ] Integración con ELK Stack (Elasticsearch, Logstash, Kibana)
- [ ] Dashboard de métricas en tiempo real
- [ ] Alertas automáticas por errores críticos
- [ ] Integración con Sentry o similar
- [ ] Análisis predictivo de errores
- [ ] Rate limiting basado en logs

---

## 📚 Referencias

- [SLF4J Documentation](http://www.slf4j.org/)
- [Logback Documentation](http://logback.qos.ch/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [MDC (Mapped Diagnostic Context)](http://logback.qos.ch/manual/mdc.html)

---

## 🆘 Soporte

Si tienes problemas con el sistema de logging:

1. Verifica que Logback esté configurado correctamente
2. Revisa permisos de escritura en carpeta `logs/`
3. Verifica nivel de log en `application.properties`
4. Consulta esta documentación
5. Contacta al equipo de desarrollo

---

**Última actualización**: 2024-11-06
**Autor**: Sistema de Logging Profesional
**Versión**: 1.0.0

