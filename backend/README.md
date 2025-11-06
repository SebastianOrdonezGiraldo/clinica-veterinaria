# 🐾 Clínica Veterinaria - Backend API

API REST desarrollada con Spring Boot para el sistema de gestión de clínicas veterinarias.

## 🚀 Tecnologías

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Autenticación y autorización
- **JWT (JSON Web Tokens)** - Tokens de autenticación
- **PostgreSQL** - Base de datos principal
- **Lombok** - Reducción de código boilerplate
- **SpringDoc OpenAPI** - Documentación automática de API
- **Maven** - Gestión de dependencias

## 📋 Requisitos previos

- **JDK 17** o superior instalado
- **Maven 3.6+** instalado
- **PostgreSQL 12+** instalado y corriendo
- **IDE recomendado**: IntelliJ IDEA, Eclipse o VS Code con extensiones de Java

### Verificar instalación:

```bash
java -version    # Debe mostrar Java 17+
mvn -version     # Debe mostrar Maven 3.6+
psql --version   # Debe mostrar PostgreSQL 12+
```

## 📦 Instalación

### 1. Configurar PostgreSQL

Antes de iniciar la aplicación, debes crear la base de datos:

```bash
# Conectarse a PostgreSQL
psql -U postgres

# Crear la base de datos
CREATE DATABASE vetclinic;

# Salir
\q
```

**📖 Para más detalles, consulta: [POSTGRESQL-SETUP.md](./POSTGRESQL-SETUP.md)**

### 2. Navegar al directorio del backend

```bash
cd backend
```

### 3. Configurar credenciales (si es necesario)

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña
```

### 4. Compilar el proyecto

```bash
mvn clean install
```

### 5. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

O ejecutar el JAR compilado:

```bash
java -jar target/veterinaria-1.0.0.jar
```

## 🌐 Endpoints Disponibles

Una vez iniciada la aplicación, los siguientes servicios estarán disponibles:

- **API Base**: `http://localhost:8081/api`
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **API Docs JSON**: `http://localhost:8081/api-docs`

### Conexión a PostgreSQL:
- **Host**: `localhost`
- **Puerto**: `5432`
- **Base de datos**: `vetclinic`
- **Usuario**: `postgres`
- **Contraseña**: `postgres` *(configurar en application.properties)*

## 📁 Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/clinica/veterinaria/
│   │   │   ├── config/          # Configuraciones (CORS, Security, etc.)
│   │   │   ├── controller/      # Controladores REST
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # Entidades JPA
│   │   │   ├── repository/      # Repositorios Spring Data
│   │   │   ├── service/         # Lógica de negocio
│   │   │   ├── exception/       # Manejo de excepciones
│   │   │   ├── security/        # Configuración de seguridad JWT
│   │   │   └── VeterinariaApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql         # Datos iniciales (opcional)
│   └── test/                    # Tests unitarios e integración
├── pom.xml                      # Dependencias Maven
└── README.md
```

## 🔐 Autenticación

La API utiliza **JWT (JSON Web Tokens)** para autenticación. 

### Flujo de autenticación:

1. **Login** - `POST /api/auth/login`
   ```json
   {
     "email": "admin@vetclinic.com",
     "password": "demo123"
   }
   ```

2. **Respuesta** - Recibirás un token JWT
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIs...",
     "type": "Bearer",
     "user": { ... }
   }
   ```

3. **Uso** - Incluye el token en las peticiones:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   ```

## 🗄️ Base de Datos

### PostgreSQL

El proyecto usa **PostgreSQL** como base de datos principal.

**Configuración actual:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vetclinic
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

### Gestión de Esquema

- **`ddl-auto=update`**: Hibernate actualiza automáticamente las tablas basándose en las entidades
- Las tablas se crean automáticamente al iniciar la aplicación
- Los datos persisten entre reinicios

### Herramientas recomendadas:
- **pgAdmin 4** - GUI oficial de PostgreSQL
- **DBeaver** - Cliente universal de bases de datos
- **VS Code PostgreSQL Extension** - Para ejecutar queries desde el IDE

## 🛠️ Comandos útiles

```bash
# Compilar sin ejecutar tests
mvn clean install -DskipTests

# Ejecutar solo los tests
mvn test

# Limpiar y compilar
mvn clean package

# Ver dependencias
mvn dependency:tree

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📝 Próximos pasos

1. ✅ Estructura del proyecto creada
2. ⏳ Crear entidades JPA
3. ⏳ Implementar repositorios
4. ⏳ Desarrollar servicios
5. ⏳ Crear controladores REST
6. ⏳ Configurar Spring Security y JWT
7. ⏳ Agregar datos de prueba
8. ⏳ Tests unitarios e integración

## 🐛 Troubleshooting

### Puerto 8081 ya en uso
```bash
# Windows - Encontrar y matar proceso
netstat -ano | findstr :8081
taskkill /PID <numero_pid> /F

# Linux/Mac
lsof -ti:8081 | xargs kill -9
```

### Error de compilación Maven
```bash
mvn clean install -U
```

## 📧 Contacto

Para dudas o sugerencias sobre el backend, consulta con el equipo de desarrollo.

---

Desarrollado con ☕ y ❤️

