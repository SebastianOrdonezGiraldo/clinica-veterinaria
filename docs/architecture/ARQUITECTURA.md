# 🏗️ Arquitectura del Sistema - Clínica Veterinaria

## 📐 Visión General

El sistema sigue una arquitectura en capas (Layered Architecture) con separación clara de responsabilidades.

```
┌─────────────────────────────────────────────┐
│           FRONTEND (Futuro)                 │
│        React / Angular / Vue                │
└─────────────────┬───────────────────────────┘
                  │ HTTP/REST
┌─────────────────┴───────────────────────────┐
│         CAPA DE PRESENTACIÓN                │
│           Controllers REST                  │
│   ┌──────────┬──────────┬──────────┐       │
│   │  Auth    │ Usuarios │ Pacientes│       │
│   │Propiet   │  Citas   │ Consultas│       │
│   └──────────┴──────────┴──────────┘       │
└─────────────────┬───────────────────────────┘
                  │ DTOs
┌─────────────────┴───────────────────────────┐
│        CAPA DE SEGURIDAD                    │
│    ┌────────────────────────────┐          │
│    │  JWT Filter                │          │
│    │  Spring Security           │          │
│    │  RBAC (Role-Based Access)  │          │
│    └────────────────────────────┘          │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────┴───────────────────────────┐
│        CAPA DE LÓGICA DE NEGOCIO            │
│              Services                       │
│   ┌──────────┬──────────┬──────────┐       │
│   │ Usuario  │ Paciente │   Cita   │       │
│   │Propiet   │ Consulta │   Auth   │       │
│   └──────────┴──────────┴──────────┘       │
└─────────────────┬───────────────────────────┘
                  │ Entities
┌─────────────────┴───────────────────────────┐
│       CAPA DE ACCESO A DATOS                │
│          Repositories (JPA)                 │
│   ┌──────────┬──────────┬──────────┐       │
│   │ Usuario  │ Paciente │   Cita   │       │
│   │Propiet   │ Consulta │          │       │
│   └──────────┴──────────┴──────────┘       │
└─────────────────┬───────────────────────────┘
                  │ JDBC
┌─────────────────┴───────────────────────────┐
│          BASE DE DATOS                      │
│           PostgreSQL                        │
└─────────────────────────────────────────────┘
```

## 📦 Capas del Sistema

### 1. Capa de Presentación (Controllers)

**Responsabilidad**: Manejar las peticiones HTTP y devolver respuestas.

```
backend/src/main/java/com/clinica/veterinaria/controller/
├── AuthController.java          # Autenticación
├── UsuarioController.java       # CRUD Usuarios
├── PropietarioController.java   # CRUD Propietarios
├── PacienteController.java      # CRUD Pacientes
├── CitaController.java          # CRUD Citas
└── ConsultaController.java      # CRUD Consultas
```

**Características**:
- Validación de datos con `@Valid`
- Control de acceso con `@PreAuthorize`
- Logging de operaciones
- Manejo de respuestas HTTP

### 2. Capa de Seguridad

**Componentes**:
- `SecurityConfig`: Configuración de Spring Security
- `JwtAuthenticationFilter`: Intercepta requests y valida tokens
- `JwtUtil`: Generación y validación de tokens JWT
- `CustomUserDetailsService`: Carga detalles de usuarios

**Flujo de Autenticación**:
```
1. Cliente → POST /api/auth/login {email, password}
2. AuthService valida credenciales
3. JwtUtil genera token JWT
4. Cliente recibe token
5. Cliente → Request con Header: Authorization: Bearer {token}
6. JwtAuthenticationFilter valida token
7. Si es válido, permite acceso al endpoint
```

### 3. Capa de Lógica de Negocio (Services)

**Responsabilidad**: Implementar la lógica de negocio y reglas.

```
backend/src/main/java/com/clinica/veterinaria/service/
├── AuthService.java         # Lógica de autenticación
├── UsuarioService.java      # Lógica de usuarios
├── PropietarioService.java  # Lógica de propietarios
├── PacienteService.java     # Lógica de pacientes
├── CitaService.java         # Lógica de citas
└── ConsultaService.java     # Lógica de consultas
```

**Características**:
- Transacciones con `@Transactional`
- Validaciones de negocio
- Conversión Entity ↔ DTO
- Logging detallado

### 4. Capa de Acceso a Datos (Repositories)

**Tecnología**: Spring Data JPA

```
backend/src/main/java/com/clinica/veterinaria/repository/
├── UsuarioRepository.java
├── PropietarioRepository.java
├── PacienteRepository.java
├── CitaRepository.java
└── ConsultaRepository.java
```

**Características**:
- Queries automáticas por convención
- Queries personalizadas con `@Query`
- Paginación con `Pageable`
- Proyecciones y DTOs

### 5. Capa de Persistencia

**Base de Datos**: PostgreSQL 15

## 🔄 Patrones de Diseño

### 1. DTO Pattern (Data Transfer Object)

**Propósito**: Separar la representación de datos de las entidades de base de datos.

```java
// Entity (Base de datos)
@Entity
public class Paciente {
    @Id
    private Long id;
    private String nombre;
    @ManyToOne
    private Propietario propietario;
    // ...
}

// DTO (API)
public class PacienteDTO {
    private Long id;
    private String nombre;
    private Long propietarioId;
    // ...
}
```

**Beneficios**:
- No exponer estructura interna
- Control sobre datos enviados
- Evitar lazy loading issues

### 2. Repository Pattern

**Implementación**: Spring Data JPA

```java
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByPropietarioId(Long propietarioId);
    List<Paciente> findByNombreContainingIgnoreCase(String nombre);
}
```

### 3. Service Layer Pattern

**Propósito**: Encapsular lógica de negocio.

```java
@Service
@Transactional
public class PacienteService {
    
    public PacienteDTO create(PacienteDTO dto) {
        // Validaciones
        // Lógica de negocio
        // Persistencia
    }
}
```

### 4. Builder Pattern

**Uso**: Construcción de objetos con Lombok

```java
@Builder
public class Paciente {
    private Long id;
    private String nombre;
    // ...
}

// Uso
Paciente paciente = Paciente.builder()
    .nombre("Max")
    .especie("Perro")
    .build();
```

## 🔐 Modelo de Seguridad

### Control de Acceso Basado en Roles (RBAC)

```
┌──────────────┬─────────┬─────────┬─────────┬──────────────┐
│ Recurso      │  ADMIN  │   VET   │ RECEPC  │ ESTUDIANTE   │
├──────────────┼─────────┼─────────┼─────────┼──────────────┤
│ Usuarios     │   RW    │    R    │    R    │      R       │
│ Propietarios │   RW    │   RW    │   RW    │      R       │
│ Pacientes    │   RW    │   RW    │    R    │      R       │
│ Citas        │   RW    │   RW    │   RW    │      R       │
│ Consultas    │   RW    │   RW    │    R    │      R       │
└──────────────┴─────────┴─────────┴─────────┴──────────────┘

R = Read (Lectura)
W = Write (Escritura/Eliminación)
```

### Anotaciones de Seguridad

```java
// Solo ADMIN puede eliminar usuarios
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id)

// ADMIN, VET y RECEPCION pueden crear citas
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
@PostMapping
public ResponseEntity<CitaDTO> create(@Valid @RequestBody CitaDTO dto)
```

## 📊 Modelo de Datos

### Diagrama Entidad-Relación

```
┌─────────────┐
│   Usuario   │
│─────────────│
│ id (PK)     │
│ nombre      │
│ email       │◄──────────┐
│ password    │           │
│ rol         │           │
│ activo      │           │
└─────────────┘           │
                          │
                          │ N:1 (profesional)
                          │
┌─────────────┐           │
│ Propietario │           │
│─────────────│           │
│ id (PK)     │◄──┐       │
│ nombre      │   │       │
│ email       │   │       │
│ telefono    │   │       │
│ direccion   │   │       │
└─────────────┘   │       │
                  │       │
                  │ 1:N   │
                  │       │
┌─────────────┐   │       │
│  Paciente   │   │       │
│─────────────│   │       │
│ id (PK)     │   │       │
│ nombre      │   │       │
│ especie     │   │       │
│ raza        │   │       │
│ propiet_id  │───┘       │
└─────────────┘           │
      ▲                   │
      │ 1:N               │
      │                   │
┌─────────────┐           │
│    Cita     │           │
│─────────────│           │
│ id (PK)     │           │
│ fecha       │           │
│ motivo      │           │
│ estado      │           │
│ paciente_id │───────────┘
│ propiet_id  │
│ prof_id     │───────────┘
└─────────────┘
      ▲
      │ 1:1
      │
┌─────────────┐
│  Consulta   │
│─────────────│
│ id (PK)     │
│ fecha       │
│ diagnostico │
│ tratamiento │
│ paciente_id │
│ prof_id     │
└─────────────┘
```

## 🔄 Flujos Principales

### Flujo de Creación de Cita

```
1. Cliente → POST /api/citas
   {
     "pacienteId": 1,
     "propietarioId": 1,
     "profesionalId": 2,
     "fecha": "2025-11-10T10:00:00",
     "motivo": "Vacunación"
   }

2. CitaController recibe request
   ↓
3. @Valid valida datos
   ↓
4. @PreAuthorize verifica permisos
   ↓
5. CitaService.create(dto)
   ↓
6. Validar que paciente existe
   ↓
7. Validar que propietario existe
   ↓
8. Validar que profesional existe
   ↓
9. Crear entidad Cita
   ↓
10. CitaRepository.save(cita)
    ↓
11. Convertir a DTO
    ↓
12. Retornar 201 Created + DTO
```

## 🧪 Arquitectura de Testing

### Pirámide de Tests

```
        /\
       /  \      E2E Tests (Futuro)
      /────\
     /      \
    / Integr \   Integration Tests (36)
   /──────────\
  /            \
 /  Unitarios  \  Unit Tests (24)
/────────────────\
```

### Tests Unitarios

- Mockean dependencias
- Prueban lógica de servicios
- Rápidos y aislados

### Tests de Integración

- Usan H2 en memoria
- Prueban controllers completos
- Incluyen seguridad y validaciones

## 📈 Escalabilidad

### Estrategias Implementadas

1. **Conexión a Base de Datos**: Pool de conexiones con HikariCP
2. **Transacciones**: Aislamiento optimista
3. **Caché**: Spring Cache (futuro)
4. **Paginación**: `Pageable` en queries grandes

### Puntos de Mejora Futuros

- [ ] Implementar caché Redis
- [ ] Añadir índices de base de datos
- [ ] Implementar rate limiting
- [ ] Usar message queues para operaciones pesadas
- [ ] Implementar load balancing

## 📝 Convenciones

### Nomenclatura

- **Controllers**: `{Entidad}Controller`
- **Services**: `{Entidad}Service`
- **Repositories**: `{Entidad}Repository`
- **DTOs**: `{Entidad}DTO`
- **Entities**: `{Entidad}`

### Estructura de Paquetes

```
com.clinica.veterinaria
├── config/          # Configuración
├── controller/      # REST Controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA Entities
├── exception/       # Exception Handlers
├── repository/      # JPA Repositories
├── security/        # Seguridad
└── service/         # Business Logic
```

## 🛠️ Herramientas y Frameworks

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.2.1 |
| ORM | Spring Data JPA | 3.2.1 |
| Seguridad | Spring Security | 6.2.1 |
| JWT | jjwt | 0.12.5 |
| Base de Datos | PostgreSQL | 15 |
| Testing | JUnit 5 | 5.10.1 |
| Mocking | Mockito | 5.7.0 |
| Build | Maven | 3.8+ |
| Documentación | Swagger/OpenAPI | 2.3.0 |

---

**Última actualización**: Noviembre 2025
**Versión**: 1.0.0
