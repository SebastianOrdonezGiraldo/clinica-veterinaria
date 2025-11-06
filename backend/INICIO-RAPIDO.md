# 🚀 Guía de Inicio Rápido - API Clínica Veterinaria

**Autor:** Sebastian Ordoñez  
**Versión:** 1.0.0  
**Fecha:** Noviembre 2025

---

## ✅ Pre-requisitos Instalados

- ✅ Java 17
- ✅ Maven
- ✅ PostgreSQL

## 📋 Pasos para Ejecutar la Aplicación

### 1. Configurar PostgreSQL

```bash
# Crear la base de datos
psql -U postgres
CREATE DATABASE vetclinic;
\q
```

La aplicación está configurada con:
- **Puerto:** `5433`
- **Usuario:** `postgres`
- **Contraseña:** `root`
- **Base de datos:** `vetclinic`

Si tus credenciales son diferentes, edita `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:PUERTO/vetclinic
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
```

### 2. Compilar el Proyecto

```bash
cd backend
mvn clean package
```

### 3. Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

O ejecutar el JAR generado:
```bash
java -jar target/veterinaria-1.0.0.jar
```

### 4. Verificar que Funciona

La aplicación estará disponible en: **http://localhost:8081**

Ver la documentación Swagger: **http://localhost:8081/swagger-ui.html**

## 🔐 Usuarios de Prueba

La aplicación crea automáticamente usuarios de prueba al iniciar:

| Rol | Email | Contraseña | Descripción |
|-----|-------|------------|-------------|
| **ADMIN** | admin@clinica.com | admin123 | Acceso total al sistema |
| **VET** | maria@clinica.com | vet123 | Dra. María García |
| **VET** | carlos@clinica.com | vet123 | Dr. Carlos Rodríguez |
| **RECEPCION** | ana@clinica.com | recep123 | Ana López |
| **ESTUDIANTE** | juan@clinica.com | est123 | Juan Pérez (solo lectura) |

## 📊 Datos de Prueba Incluidos

- ✅ 5 usuarios con diferentes roles
- ✅ 3 propietarios de mascotas
- ✅ 5 pacientes (3 perros, 2 gatos, 1 conejo)
- ✅ 3 citas médicas programadas
- ✅ 2 consultas en historia clínica

## 🔑 Cómo Usar la API

### 1. Hacer Login

```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "admin@clinica.com",
  "password": "admin123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "usuario": {
    "id": 1,
    "nombre": "Administrador",
    "email": "admin@clinica.com",
    "rol": "ADMIN"
  }
}
```

### 2. Usar el Token en Requests

Incluir el header `Authorization` en todas las peticiones protegidas:

```bash
GET http://localhost:8081/api/pacientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 📡 Endpoints Principales

### Autenticación (Públicos)
- `POST /api/auth/login` - Iniciar sesión
- `GET /api/auth/validate` - Validar token

### Usuarios (Solo ADMIN)
- `GET /api/usuarios` - Listar usuarios
- `POST /api/usuarios` - Crear usuario
- `PUT /api/usuarios/{id}` - Actualizar usuario
- `DELETE /api/usuarios/{id}` - Eliminar usuario
- `GET /api/usuarios/veterinarios` - Listar veterinarios activos

### Propietarios
- `GET /api/propietarios` - Listar propietarios
- `GET /api/propietarios/{id}` - Obtener por ID
- `POST /api/propietarios` - Crear (ADMIN, RECEPCION)
- `PUT /api/propietarios/{id}` - Actualizar (ADMIN, RECEPCION)
- `DELETE /api/propietarios/{id}` - Eliminar (Solo ADMIN)

### Pacientes
- `GET /api/pacientes` - Listar pacientes
- `GET /api/pacientes/{id}` - Obtener por ID
- `GET /api/pacientes/propietario/{id}` - Por propietario
- `POST /api/pacientes` - Crear (ADMIN, RECEPCION, VET)
- `PUT /api/pacientes/{id}` - Actualizar (ADMIN, RECEPCION, VET)
- `DELETE /api/pacientes/{id}` - Eliminar (Solo ADMIN)

### Citas
- `GET /api/citas` - Listar citas
- `GET /api/citas/{id}` - Obtener por ID
- `GET /api/citas/paciente/{id}` - Por paciente
- `POST /api/citas` - Crear (ADMIN, RECEPCION, VET)
- `PUT /api/citas/{id}` - Actualizar (ADMIN, RECEPCION, VET)
- `PATCH /api/citas/{id}/estado` - Cambiar estado
- `DELETE /api/citas/{id}` - Eliminar (ADMIN, RECEPCION)

### Consultas (Historia Clínica)
- `GET /api/consultas` - Listar consultas (ADMIN, VET)
- `GET /api/consultas/{id}` - Obtener por ID
- `GET /api/consultas/paciente/{id}` - Historia de paciente
- `POST /api/consultas` - Crear (ADMIN, VET)
- `PUT /api/consultas/{id}` - Actualizar (ADMIN, VET)
- `DELETE /api/consultas/{id}` - Eliminar (Solo ADMIN)

## 🛠️ Comandos Útiles

### Limpiar y recompilar:
```bash
mvn clean compile
```

### Ejecutar tests:
```bash
mvn test
```

### Generar JAR:
```bash
mvn clean package
```

### Ver logs en tiempo real:
```bash
tail -f logs/spring.log
```

### Resetear base de datos:
```bash
# En PostgreSQL
DROP DATABASE vetclinic;
CREATE DATABASE vetclinic;
# Reiniciar la aplicación para recrear tablas y datos
```

## 🔧 Perfiles de Ejecución

### Desarrollo (por defecto):
```bash
mvn spring-boot:run
```

### Desarrollo explícito:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Producción:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 📝 Notas Importantes

1. **DDL Auto:** La aplicación está configurada con `ddl-auto=update`, lo que significa que creará las tablas automáticamente la primera vez.

2. **Datos Iniciales:** Los datos de prueba solo se insertan si la base de datos está vacía. Si reinicias la aplicación sin borrar la BD, no se volverán a crear.

3. **CORS:** El frontend puede conectarse desde `http://localhost:5173` o `http://localhost:3000`. Si usas otro puerto, edita `SecurityConfig.java`.

4. **JWT Secret:** En producción, cambia el secret en `application-prod.properties`.

5. **Puerto:** La API corre en el puerto `8081` para evitar conflictos con el frontend (que suele usar 3000 o 5173).

## 🆘 Solución de Problemas

### Error: Puerto 8081 ya en uso
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8081
kill -9 <PID>
```

### Error: No puede conectar a PostgreSQL
- Verifica que PostgreSQL esté corriendo
- Verifica usuario/contraseña en `application.properties`
- Verifica que la base de datos `vetclinic` exista

### Error: Lombok no funciona
```bash
mvn clean compile
```

### Ver logs detallados:
Edita `application.properties`:
```properties
logging.level.com.clinica.veterinaria=DEBUG
```

## 📚 Documentación Adicional

- `README.md` - Documentación general del proyecto
- `POSTGRESQL-SETUP.md` - Guía detallada de configuración de PostgreSQL
- `ARQUITECTURA.md` - Decisiones arquitectónicas y patrones de diseño
- `PATRONES-RESUMEN.md` - Resumen visual de patrones implementados

## 🎉 ¡Listo!

Tu API está funcionando correctamente. Ahora puedes:

1. Probar los endpoints con Postman o curl
2. Ver la documentación interactiva en Swagger
3. Conectar tu frontend React a la API
4. Agregar nuevas funcionalidades según necesites

**¡Disfruta desarrollando!** 🚀

