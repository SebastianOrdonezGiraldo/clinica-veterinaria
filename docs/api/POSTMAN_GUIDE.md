# 📬 Guía de Pruebas con Postman - Clínica Veterinaria API

## 🔧 Configuración Inicial

### URL Base
```
http://localhost:8081
```

---

## 🔐 1. AUTENTICACIÓN (Login)

### 🔓 Login - Obtener Token JWT

**Endpoint:** `POST /api/auth/login`  
**Acceso:** Público (no requiere autenticación)

#### Request Body:
```json
{
  "email": "admin@clinica.com",
  "password": "admin123"
}
```

#### Response (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "email": "admin@clinica.com",
  "rol": "ADMIN",
  "nombre": "Administrador"
}
```

#### 👥 Credenciales Disponibles:

| Rol | Email | Password |
|-----|-------|----------|
| **ADMIN** | admin@clinica.com | admin123 |
| **VET** | maria@clinica.com | vet123 |
| **VET** | carlos@clinica.com | vet123 |
| **RECEPCION** | ana@clinica.com | recep123 |
| **ESTUDIANTE** | juan@clinica.com | est123 |

---

## 🔑 Configurar Token JWT en Postman

Una vez obtengas el token del login:

1. **Opción A - Authorization en cada request:**
   - Ve a la pestaña **Authorization**
   - Type: **Bearer Token**
   - Token: Pega el token que obtuviste del login

2. **Opción B - Variable de colección (Recomendado):**
   - Crea una colección en Postman
   - En Authorization de la colección: **Bearer Token**
   - Token: `{{jwt_token}}`
   - Después del login, guarda el token en una variable:
     ```javascript
     // En Tests del endpoint login:
     var jsonData = pm.response.json();
     pm.collectionVariables.set("jwt_token", jsonData.token);
     ```

---

## 👤 2. USUARIOS

### 📋 Listar Todos los Usuarios
**Endpoint:** `GET /api/usuarios`  
**Roles permitidos:** ADMIN  
**Headers:**
```
Authorization: Bearer {token}
```

---

### 🔍 Obtener Usuario por ID
**Endpoint:** `GET /api/usuarios/{id}`  
**Roles permitidos:** ADMIN  
**Ejemplo:** `GET /api/usuarios/1`

---

### ➕ Crear Usuario
**Endpoint:** `POST /api/usuarios`  
**Roles permitidos:** ADMIN  
**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body:**
```json
{
  "nombre": "Dr. Luis Torres",
  "email": "luis@clinica.com",
  "password": "password123",
  "rol": "VET"
}
```

**Roles disponibles:** `ADMIN`, `VET`, `RECEPCION`, `ESTUDIANTE`

---

### ✏️ Actualizar Usuario
**Endpoint:** `PUT /api/usuarios/{id}`  
**Roles permitidos:** ADMIN  
**Ejemplo:** `PUT /api/usuarios/1`

**Body:**
```json
{
  "nombre": "Dr. Luis Torres Actualizado",
  "email": "luis@clinica.com",
  "rol": "VET",
  "activo": true
}
```

---

### 🗑️ Eliminar Usuario (Soft Delete)
**Endpoint:** `DELETE /api/usuarios/{id}`  
**Roles permitidos:** ADMIN  
**Ejemplo:** `DELETE /api/usuarios/5`

---

## 🏠 3. PROPIETARIOS

### 📋 Listar Todos los Propietarios
**Endpoint:** `GET /api/propietarios`  
**Roles permitidos:** ADMIN, VET, RECEPCION

---

### 🔍 Obtener Propietario por ID
**Endpoint:** `GET /api/propietarios/{id}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/propietarios/1`

---

### ➕ Crear Propietario
**Endpoint:** `POST /api/propietarios`  
**Roles permitidos:** ADMIN, RECEPCION

**Body:**
```json
{
  "nombre": "Carlos Ramírez",
  "documento": "12345678",
  "email": "carlos@email.com",
  "telefono": "555-1234",
  "direccion": "Calle 123, Ciudad"
}
```

---

### ✏️ Actualizar Propietario
**Endpoint:** `PUT /api/propietarios/{id}`  
**Roles permitidos:** ADMIN, RECEPCION

**Body:**
```json
{
  "nombre": "Carlos Ramírez Actualizado",
  "documento": "12345678",
  "email": "carlos.nuevo@email.com",
  "telefono": "555-9999",
  "direccion": "Nueva Dirección 456",
  "activo": true
}
```

---

### 🔎 Buscar Propietarios
**Endpoint:** `GET /api/propietarios/buscar?q={texto}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/propietarios/buscar?q=Pedro`

---

### 🗑️ Eliminar Propietario
**Endpoint:** `DELETE /api/propietarios/{id}`  
**Roles permitidos:** ADMIN

---

## 🐾 4. PACIENTES (Mascotas)

### 📋 Listar Todos los Pacientes
**Endpoint:** `GET /api/pacientes`  
**Roles permitidos:** ADMIN, VET, RECEPCION

---

### 🔍 Obtener Paciente por ID
**Endpoint:** `GET /api/pacientes/{id}`  
**Roles permitidos:** ADMIN, VET, RECEPCION, ESTUDIANTE  
**Ejemplo:** `GET /api/pacientes/1`

---

### ➕ Crear Paciente
**Endpoint:** `POST /api/pacientes`  
**Roles permitidos:** ADMIN, VET, RECEPCION

**Body:**
```json
{
  "nombre": "Firulais",
  "especie": "Perro",
  "raza": "Golden Retriever",
  "sexo": "M",
  "edadMeses": 24,
  "pesoKg": 28.5,
  "microchip": "123456789012345",
  "notas": "Muy amigable",
  "propietarioId": 1
}
```

**Notas:**
- `sexo`: "M" o "F"
- `propietarioId`: ID del propietario (debe existir)

---

### ✏️ Actualizar Paciente
**Endpoint:** `PUT /api/pacientes/{id}`  
**Roles permitidos:** ADMIN, VET, RECEPCION

**Body:**
```json
{
  "nombre": "Firulais",
  "especie": "Perro",
  "raza": "Golden Retriever",
  "sexo": "M",
  "edadMeses": 25,
  "pesoKg": 29.0,
  "microchip": "123456789012345",
  "notas": "Muy amigable y juguetón",
  "propietarioId": 1,
  "activo": true
}
```

---

### 🔎 Buscar Pacientes
**Endpoint:** `GET /api/pacientes/buscar?q={texto}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/pacientes/buscar?q=Max`

---

### 🏠 Obtener Pacientes por Propietario
**Endpoint:** `GET /api/pacientes/propietario/{propietarioId}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/pacientes/propietario/1`

---

### 🗑️ Eliminar Paciente
**Endpoint:** `DELETE /api/pacientes/{id}`  
**Roles permitidos:** ADMIN

---

## 📅 5. CITAS

### 📋 Listar Todas las Citas
**Endpoint:** `GET /api/citas`  
**Roles permitidos:** ADMIN, VET, RECEPCION

---

### 🔍 Obtener Cita por ID
**Endpoint:** `GET /api/citas/{id}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/citas/1`

---

### ➕ Crear Cita
**Endpoint:** `POST /api/citas`  
**Roles permitidos:** ADMIN, VET, RECEPCION

**Body:**
```json
{
  "fecha": "2025-11-10T10:30:00",
  "motivo": "Vacunación",
  "estado": "PENDIENTE",
  "observaciones": "Primera vacuna del año",
  "pacienteId": 1,
  "propietarioId": 1,
  "profesionalId": 2
}
```

**Estados disponibles:**
- `PENDIENTE`
- `CONFIRMADA`
- `EN_CURSO`
- `COMPLETADA`
- `CANCELADA`
- `NO_ASISTIO`

---

### ✏️ Actualizar Cita
**Endpoint:** `PUT /api/citas/{id}`  
**Roles permitidos:** ADMIN, VET, RECEPCION

**Body:**
```json
{
  "fecha": "2025-11-10T11:00:00",
  "motivo": "Vacunación y desparasitación",
  "estado": "CONFIRMADA",
  "observaciones": "Cliente confirmó asistencia",
  "pacienteId": 1,
  "propietarioId": 1,
  "profesionalId": 2
}
```

---

### 📊 Filtrar Citas por Estado
**Endpoint:** `GET /api/citas/estado/{estado}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/citas/estado/PENDIENTE`

---

### 📆 Obtener Citas por Rango de Fechas
**Endpoint:** `GET /api/citas/rango?inicio={fecha}&fin={fecha}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/citas/rango?inicio=2025-11-01T00:00:00&fin=2025-11-30T23:59:59`

---

### 🩺 Obtener Citas por Profesional
**Endpoint:** `GET /api/citas/profesional/{profesionalId}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/citas/profesional/2`

---

### 🐾 Obtener Citas por Paciente
**Endpoint:** `GET /api/citas/paciente/{pacienteId}`  
**Roles permitidos:** ADMIN, VET, RECEPCION  
**Ejemplo:** `GET /api/citas/paciente/1`

---

### 🗑️ Eliminar Cita
**Endpoint:** `DELETE /api/citas/{id}`  
**Roles permitidos:** ADMIN

---

## 📋 6. CONSULTAS (Historia Clínica)

### 📋 Listar Todas las Consultas
**Endpoint:** `GET /api/consultas`  
**Roles permitidos:** ADMIN, VET, ESTUDIANTE

---

### 🔍 Obtener Consulta por ID
**Endpoint:** `GET /api/consultas/{id}`  
**Roles permitidos:** ADMIN, VET, ESTUDIANTE  
**Ejemplo:** `GET /api/consultas/1`

---

### ➕ Crear Consulta
**Endpoint:** `POST /api/consultas`  
**Roles permitidos:** ADMIN, VET

**Body:**
```json
{
  "fecha": "2025-11-06T14:30:00",
  "frecuenciaCardiaca": 120,
  "frecuenciaRespiratoria": 28,
  "temperatura": 38.5,
  "pesoKg": 30.2,
  "examenFisico": "Paciente alerta y activo. Mucosas rosadas. Hidratación normal.",
  "diagnostico": "Estado general bueno. Vacunas al día.",
  "tratamiento": "Continuar con alimentación balanceada. Próximo control en 6 meses.",
  "observaciones": "Propietario reporta buen apetito",
  "pacienteId": 1,
  "profesionalId": 2
}
```

---

### ✏️ Actualizar Consulta
**Endpoint:** `PUT /api/consultas/{id}`  
**Roles permitidos:** ADMIN, VET

---

### 🐾 Obtener Consultas por Paciente
**Endpoint:** `GET /api/consultas/paciente/{pacienteId}`  
**Roles permitidos:** ADMIN, VET, ESTUDIANTE  
**Ejemplo:** `GET /api/consultas/paciente/1`

---

### 🩺 Obtener Consultas por Profesional
**Endpoint:** `GET /api/consultas/profesional/{profesionalId}`  
**Roles permitidos:** ADMIN, VET  
**Ejemplo:** `GET /api/consultas/profesional/2`

---

### 🗑️ Eliminar Consulta
**Endpoint:** `DELETE /api/consultas/{id}`  
**Roles permitidos:** ADMIN

---

## 📊 Códigos de Estado HTTP

| Código | Significado | Cuándo aparece |
|--------|-------------|----------------|
| **200** | OK | Operación exitosa (GET, PUT) |
| **201** | Created | Recurso creado (POST) |
| **204** | No Content | Eliminación exitosa (DELETE) |
| **400** | Bad Request | Datos inválidos |
| **401** | Unauthorized | Token inválido o expirado |
| **403** | Forbidden | Sin permisos para esta acción |
| **404** | Not Found | Recurso no encontrado |
| **500** | Internal Error | Error del servidor |

---

## 🎯 Flujo Típico de Prueba

### 1️⃣ **Login**
```
POST /api/auth/login
```
→ Guarda el token JWT

### 2️⃣ **Crear Propietario**
```
POST /api/propietarios
```
→ Guarda el ID del propietario

### 3️⃣ **Crear Paciente**
```
POST /api/pacientes
Body: { ..., "propietarioId": {id_del_paso_2} }
```
→ Guarda el ID del paciente

### 4️⃣ **Crear Cita**
```
POST /api/citas
Body: { 
  "pacienteId": {id_del_paso_3},
  "propietarioId": {id_del_paso_2},
  "profesionalId": 2
}
```

### 5️⃣ **Crear Consulta**
```
POST /api/consultas
Body: { 
  "pacienteId": {id_del_paso_3},
  "profesionalId": 2
}
```

---

## 🔥 Tips para Postman

### 1. Variables de Entorno
Crea variables para:
- `base_url`: `http://localhost:8081`
- `jwt_token`: Se actualiza automáticamente con el script del login

### 2. Script para Guardar Token Automáticamente
En el endpoint de login, pestaña **Tests**:
```javascript
var jsonData = pm.response.json();
if (jsonData.token) {
    pm.collectionVariables.set("jwt_token", jsonData.token);
    console.log("Token guardado: " + jsonData.token);
}
```

### 3. Pre-request Script para Debug
```javascript
console.log("Request URL: " + pm.request.url);
console.log("Token: " + pm.collectionVariables.get("jwt_token"));
```

---

## ⚠️ Errores Comunes

### Error 401 - Unauthorized
- **Causa:** Token no válido o expirado
- **Solución:** Hacer login nuevamente

### Error 403 - Forbidden
- **Causa:** El usuario no tiene permisos para esta acción
- **Solución:** Usar un usuario con el rol adecuado

### Error 404 - Not Found
- **Causa:** El ID del recurso no existe
- **Solución:** Verificar que el ID sea correcto

### Error 400 - Bad Request
- **Causa:** Datos del body incorrectos o incompletos
- **Solución:** Revisar validaciones en el body

---

## 📚 Recursos Adicionales

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs:** http://localhost:8081/api-docs
- **Health Check:** http://localhost:8081/actuator/health

---

## 🎉 ¡Listo para Probar!

Ahora puedes importar esta guía a Postman y comenzar a probar todos los endpoints de la API.

**¿Preguntas?** Revisa los logs del servidor para más detalles sobre errores.

