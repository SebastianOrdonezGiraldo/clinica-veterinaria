# 🚀 Sistema de Logging - Guía de Inicio Rápido

## ⚡ Inicio Rápido en 5 Minutos

### 1️⃣ Compilar el Backend

```bash
cd apps/backend
mvn clean install
```

### 2️⃣ Iniciar el Backend

```bash
mvn spring-boot:run
```

Espera a ver este mensaje:
```
Started ClinicaVeterinariaApplication in X.XXX seconds
```

### 3️⃣ Iniciar el Frontend (en otra terminal)

```bash
cd apps/frontend
npm run dev
```

Espera a ver:
```
VITE ready in XXX ms
➜  Local:   http://localhost:5173/
```

### 4️⃣ Abre el Navegador

1. Ve a http://localhost:5173
2. Abre las **DevTools** (F12)
3. Ve a la pestaña **Console**

---

## 🧪 Probar el Sistema de Logging

### Test 1: Login y Ver Logs

1. **En el navegador:**
   - Haz login con: `admin@clinica.com` / `admin123`
   
2. **En la consola del navegador verás:**
   ```
   🔍 [HH:MM:SS] DEBUG → API Request: POST /api/auth/login
   ℹ️ [HH:MM:SS] INFO ← API Response: POST /api/auth/login [200] 234ms
   ℹ️ [HH:MM:SS] INFO User Event: login
   ```

3. **En la consola del backend verás:**
   ```
   → Incoming POST /api/auth/login from 127.0.0.1 | User: anonymous | Correlation-ID: 1730888445123-abc123xyz
   → Intento de login para usuario: admin@clinica.com desde IP: 127.0.0.1
   ✓ Login exitoso para usuario: admin@clinica.com (ID: 1, Rol: ADMINISTRADOR) desde IP: 127.0.0.1
   🔓 LOGIN SUCCESS | User: admin@clinica.com | IP: 127.0.0.1
   ← Response POST /api/auth/login | Status: 200 | Duration: 234ms
   ```

4. **En el archivo `logs/audit.log` verás:**
   ```json
   {
     "timestamp": "2024-11-06T10:30:45.123Z",
     "level": "INFO",
     "message": "🔓 LOGIN SUCCESS | User: admin@clinica.com | IP: 127.0.0.1",
     "action": "LOGIN_SUCCESS",
     "username": "admin@clinica.com",
     "clientIp": "127.0.0.1",
     "correlationId": "1730888445123-abc123xyz"
   }
   ```

---

### Test 2: Crear un Paciente

1. **En el navegador:**
   - Ve a **Pacientes** → **Nuevo Paciente**
   - Llena el formulario
   - Guarda

2. **En la consola del navegador:**
   ```
   🔍 [HH:MM:SS] DEBUG → API Request: POST /api/pacientes
   ℹ️ [HH:MM:SS] INFO ← API Response: POST /api/pacientes [201] 456ms
   ```

3. **En la consola del backend:**
   ```
   → Incoming POST /api/pacientes from 127.0.0.1 | User: admin@clinica.com
   → Creando nuevo paciente: Rex (Especie: Perro)
   ✓ Paciente creado exitosamente con ID: 123 | Nombre: Rex | Propietario: Juan Pérez
   ✓ CREATED Paciente with ID 123 | User: admin@clinica.com | Data: Nombre: Rex, Especie: Perro
   ← Response POST /api/pacientes | Status: 201 | Duration: 456ms
   ```

4. **En `logs/audit.log`:**
   ```json
   {
     "timestamp": "2024-11-06T10:35:12.456Z",
     "level": "INFO",
     "message": "✓ CREATED Paciente with ID 123",
     "action": "CREATE",
     "entity": "Paciente",
     "entityId": "123",
     "username": "admin@clinica.com"
   }
   ```

---

### Test 3: Probar Error Boundary

1. **En la consola del navegador, ejecuta:**
   ```javascript
   // Esto forzará un error para probar el Error Boundary
   throw new Error("Error de prueba");
   ```

2. **Verás:**
   - UI de error con mensaje amigable
   - Botones para recuperación
   - En desarrollo: detalles del error

3. **En la consola verás:**
   ```
   ❌ [HH:MM:SS] ERROR React Error Boundary caught an error
   {
     error: Error: Error de prueba,
     componentStack: "...",
     errorBoundary: true
   }
   ```

---

### Test 4: Ver Logs Acumulados

#### En el Frontend (Consola del Navegador):

```javascript
// Ver últimos 20 logs
window.logger.getRecentLogs(20)

// Exportar todos los logs
window.logger.exportLogs()

// Limpiar logs
window.logger.clearLogs()
```

#### En el Backend (Terminal):

```bash
# Ver logs en tiempo real (requiere jq)
tail -f logs/application.log | jq .

# Ver solo mensajes
tail -f logs/application.log | jq -r '.message'

# Ver solo errores
tail -f logs/error.log | jq .

# Ver auditoría
tail -f logs/audit.log | jq .

# Buscar por Correlation ID
grep "correlationId=1730888445123-abc123xyz" logs/application.log | jq .
```

---

### Test 5: Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health | jq .

# Ver todas las métricas disponibles
curl http://localhost:8080/actuator/metrics | jq .

# Ver métricas de HTTP requests
curl http://localhost:8080/actuator/metrics/http.server.requests | jq .

# Ver uso de memoria
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq .

# Ver loggers configurados
curl http://localhost:8080/actuator/loggers/com.clinica.veterinaria | jq .

# Cambiar nivel de log en runtime (sin reiniciar)
curl -X POST http://localhost:8080/actuator/loggers/com.clinica.veterinaria \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

### Test 6: Probar Request Lento

Para simular un request lento y ver la alerta:

1. **En el backend, agrega un delay temporal en algún servicio:**
   ```java
   public void miMetodo() {
       try {
           Thread.sleep(2000); // 2 segundos
       } catch (InterruptedException e) {
           // ignore
       }
       // ... resto del código
   }
   ```

2. **Ejecuta la operación y verás:**
   ```
   ⚠️ SLOW REQUEST: GET /api/endpoint took 2000ms (threshold: 1000ms)
   ```

3. **En `logs/performance.log`:**
   ```json
   {
     "message": "⚠️ SLOW REQUEST: GET /api/endpoint took 2000ms",
     "requestUri": "/api/endpoint",
     "requestMethod": "GET",
     "duration": "2000"
   }
   ```

---

## 📁 Estructura de Archivos de Log

Después de ejecutar las pruebas, deberías tener estos archivos:

```
apps/backend/logs/
├── application.log         # Logs generales
├── error.log              # Solo errores (si hubo alguno)
├── audit.log              # Auditoría (login, CRUD)
└── performance.log        # Métricas de performance
```

---

## 🔍 Análisis de Logs

### Buscar por Usuario

```bash
# Login exitosos de un usuario
cat logs/audit.log | jq 'select(.username == "admin@clinica.com" and .action == "LOGIN_SUCCESS")'

# Todas las acciones de un usuario
cat logs/audit.log | jq 'select(.username == "admin@clinica.com")'
```

### Buscar por Entidad

```bash
# Todas las operaciones sobre pacientes
cat logs/audit.log | jq 'select(.entity == "Paciente")'

# Solo creaciones de pacientes
cat logs/audit.log | jq 'select(.entity == "Paciente" and .action == "CREATE")'
```

### Analizar Performance

```bash
# Requests más lentos
cat logs/performance.log | jq -r '. | "\(.duration) \(.requestUri)"' | sort -rn | head -20

# Promedio de duración por endpoint
cat logs/performance.log | jq -r '. | "\(.requestUri) \(.duration)"' | awk '{sum[$1]+=$2; count[$1]++} END {for (uri in sum) print uri, sum[uri]/count[uri]}' | sort -k2 -rn
```

### Contar Errores

```bash
# Contar errores por tipo
cat logs/error.log | jq -r '.logger' | sort | uniq -c | sort -rn

# Errores en las últimas 24 horas
cat logs/error.log | jq 'select(.timestamp > "'$(date -u -d '24 hours ago' '+%Y-%m-%dT%H:%M:%S')'Z")'
```

---

## 🎯 Casos de Uso Comunes

### Caso 1: Usuario reporta que no puede hacer login

1. **Pide al usuario el Correlation ID** (aparece en la pantalla de error)
2. **Busca en logs:**
   ```bash
   grep "correlationId=abc-123" logs/audit.log
   ```
3. **Identifica la causa:**
   - ¿Credenciales inválidas?
   - ¿Usuario inactivo?
   - ¿Error de base de datos?

### Caso 2: La aplicación está lenta

1. **Ver requests lentos:**
   ```bash
   cat logs/performance.log | jq 'select(.duration > 1000)'
   ```
2. **Identificar endpoint problemático**
3. **Ver queries SQL:**
   ```bash
   cat logs/application.log | jq 'select(.logger == "org.hibernate.SQL")'
   ```
4. **Optimizar query o agregar índice**

### Caso 3: Auditoría - ¿Quién eliminó este paciente?

```bash
# Buscar eliminación del paciente ID 123
cat logs/audit.log | jq 'select(.action == "DELETE" and .entity == "Paciente" and .entityId == "123")'
```

**Resultado:**
```json
{
  "action": "DELETE",
  "entity": "Paciente",
  "entityId": "123",
  "username": "admin@clinica.com",
  "timestamp": "2024-11-06T10:45:00.000Z"
}
```

---

## 📚 Recursos Adicionales

- **Documentación Completa**: `docs/development/LOGGING-SYSTEM.md`
- **Resumen de Implementación**: `LOGGING-IMPLEMENTATION-SUMMARY.md`
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator

---

## 🆘 Problemas Comunes

### ❌ Problema: Maven no encuentra las dependencias

**Solución:**
```bash
mvn clean install -U
```

### ❌ Problema: No se crean archivos de log

**Solución:**
```bash
# Crear carpeta de logs
mkdir -p apps/backend/logs
```

### ❌ Problema: `jq: command not found`

**Solución en Windows:**
```powershell
# Instalar jq con Chocolatey
choco install jq

# O descargar de: https://stedolan.github.io/jq/download/
```

**Solución en Mac:**
```bash
brew install jq
```

**Solución en Linux:**
```bash
sudo apt-get install jq
```

### ❌ Problema: Error en frontend "Cannot find module '@/core/logging/loggerService'"

**Solución:**
```bash
cd apps/frontend
npm install
```

---

## ✅ Checklist de Verificación

Después de las pruebas, verifica que:

- [ ] Los logs aparecen en la consola del backend
- [ ] Los logs aparecen en la consola del navegador (F12)
- [ ] Se crearon los archivos en `apps/backend/logs/`
- [ ] El Correlation ID aparece en los logs
- [ ] Los logs de auditoría registran login y operaciones CRUD
- [ ] El Error Boundary captura errores de React
- [ ] Los endpoints de Actuator funcionan
- [ ] Los requests lentos se detectan

---

¡Felicitaciones! El sistema de logging está funcionando correctamente. 🎉

Para más información, consulta la documentación completa en `docs/development/LOGGING-SYSTEM.md`.

