# 🏗️ Arquitectura y Patrones de Diseño

## 📐 Arquitectura en Capas (Layered Architecture)

Implementaremos una **arquitectura en capas bien definida** siguiendo las mejores prácticas de Spring Boot:

```
┌─────────────────────────────────────────┐
│         CLIENTE (Frontend)              │
│         React + TypeScript              │
└──────────────┬──────────────────────────┘
               │ HTTP/REST
               ▼
┌─────────────────────────────────────────┐
│     CAPA DE PRESENTACIÓN (Controller)   │◄─── @RestController
│  - Recibe peticiones HTTP                │     @RequestMapping
│  - Valida datos de entrada               │     @Valid
│  - Retorna DTOs                          │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│     CAPA DE SERVICIO (Service)          │◄─── @Service
│  - Lógica de negocio                     │     @Transactional
│  - Validaciones complejas                │
│  - Conversión Entity ↔ DTO               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│     CAPA DE PERSISTENCIA (Repository)   │◄─── @Repository
│  - Acceso a base de datos                │     Spring Data JPA
│  - Queries personalizadas                │     JpaRepository
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         BASE DE DATOS (PostgreSQL)      │
└─────────────────────────────────────────┘
```

## 🎯 Patrones de Diseño Implementados

### 1. **Repository Pattern** ✅
**Propósito**: Abstrae el acceso a datos, desacoplando la lógica de negocio de la persistencia.

**Implementación**: Spring Data JPA
```java
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByPropietarioId(Long propietarioId);
    List<Paciente> findByEspecieAndActivo(String especie, Boolean activo);
}
```

**Beneficios**:
- ✅ Desacoplamiento entre lógica y persistencia
- ✅ Fácil testing con mocks
- ✅ Queries automáticas basadas en nombres de métodos
- ✅ Posibilidad de cambiar implementación sin afectar servicios

---

### 2. **Service Layer Pattern** ✅
**Propósito**: Encapsula la lógica de negocio, coordinando operaciones entre múltiples repositorios.

**Implementación**:
```java
@Service
@Transactional
public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    
    // Constructor injection (mejor práctica)
    public PacienteService(PacienteRepository pacienteRepository, 
                          PropietarioRepository propietarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.propietarioRepository = propietarioRepository;
    }
    
    public PacienteDTO crearPaciente(PacienteRequestDTO request) {
        // Lógica de negocio aquí
    }
}
```

**Beneficios**:
- ✅ Separación de responsabilidades
- ✅ Transaccionalidad manejada por Spring
- ✅ Reusabilidad de lógica de negocio
- ✅ Fácil testing unitario

---

### 3. **Data Transfer Object (DTO) Pattern** ✅
**Propósito**: Transferir datos entre capas sin exponer entidades internas.

**Implementación**:
```java
// Request DTO - Para recibir datos del cliente
public class PacienteRequestDTO {
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
    
    @NotNull(message = "El propietario es requerido")
    private Long propietarioId;
    
    // getters, setters, validations
}

// Response DTO - Para enviar datos al cliente
public class PacienteResponseDTO {
    private Long id;
    private String nombre;
    private String especie;
    private PropietarioDTO propietario; // DTO anidado
    
    // Solo los datos necesarios
}
```

**Beneficios**:
- ✅ Evita exposición de estructura interna
- ✅ Permite personalizar respuestas
- ✅ Validaciones en capa de presentación
- ✅ Evita lazy loading issues

---

### 4. **Builder Pattern** ✅
**Propósito**: Construir objetos complejos de manera legible y flexible.

**Implementación**: Usando Lombok
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    private Long id;
    private String nombre;
    private String especie;
    // ... más campos
}

// Uso
Paciente paciente = Paciente.builder()
    .nombre("Max")
    .especie("Canino")
    .raza("Golden Retriever")
    .build();
```

**Beneficios**:
- ✅ Código más legible
- ✅ Construcción flexible de objetos
- ✅ Inmutabilidad opcional
- ✅ Menos código boilerplate

---

### 5. **Dependency Injection (DI)** ✅
**Propósito**: Inversión de control, desacoplamiento de dependencias.

**Implementación**: Spring Framework
```java
@Service
public class ConsultaService {
    
    // Constructor injection (recomendado)
    private final ConsultaRepository consultaRepository;
    private final PacienteService pacienteService;
    
    public ConsultaService(ConsultaRepository consultaRepository,
                          PacienteService pacienteService) {
        this.consultaRepository = consultaRepository;
        this.pacienteService = pacienteService;
    }
}
```

**Beneficios**:
- ✅ Bajo acoplamiento
- ✅ Fácil testing (inyección de mocks)
- ✅ Gestión automática de ciclo de vida
- ✅ Single Responsibility Principle

---

### 6. **Singleton Pattern** ✅
**Propósito**: Una sola instancia de un objeto en toda la aplicación.

**Implementación**: Spring Beans (por defecto son singleton)
```java
@Service // Singleton por defecto
public class UsuarioService {
    // Una sola instancia en todo el contexto de Spring
}
```

**Beneficios**:
- ✅ Eficiencia de memoria
- ✅ Estado compartido cuando es necesario
- ✅ Manejado automáticamente por Spring

---

### 7. **Strategy Pattern** ✅
**Propósito**: Definir familia de algoritmos intercambiables.

**Implementación**: Para diferentes estrategias de negocio
```java
// Interfaz estrategia
public interface NotificacionStrategy {
    void enviarNotificacion(Usuario usuario, String mensaje);
}

// Implementaciones concretas
@Component
public class EmailNotificacion implements NotificacionStrategy {
    public void enviarNotificacion(Usuario usuario, String mensaje) {
        // Enviar email
    }
}

@Component
public class SMSNotificacion implements NotificacionStrategy {
    public void enviarNotificacion(Usuario usuario, String mensaje) {
        // Enviar SMS
    }
}

// Servicio que usa la estrategia
@Service
public class NotificacionService {
    private final Map<String, NotificacionStrategy> estrategias;
    
    // Spring inyecta todas las implementaciones
    public NotificacionService(List<NotificacionStrategy> estrategias) {
        // Configurar estrategias
    }
}
```

**Beneficios**:
- ✅ Flexibilidad para agregar nuevas estrategias
- ✅ Open/Closed Principle
- ✅ Fácil testing de cada estrategia

---

### 8. **Factory Pattern** ✅
**Propósito**: Crear objetos sin especificar la clase exacta.

**Implementación**: Para crear diferentes tipos de entidades
```java
@Component
public class PrescripcionFactory {
    
    public Prescripcion crearPrescripcion(TipoPrescripcion tipo, Consulta consulta) {
        return switch (tipo) {
            case MEDICAMENTO -> crearPrescripcionMedicamento(consulta);
            case TRATAMIENTO -> crearPrescripcionTratamiento(consulta);
            case DIETA -> crearPrescripcionDieta(consulta);
        };
    }
}
```

**Beneficios**:
- ✅ Centraliza lógica de creación
- ✅ Facilita agregar nuevos tipos
- ✅ Desacopla código cliente de implementaciones concretas

---

### 9. **Mapper Pattern** ✅
**Propósito**: Convertir entre Entity y DTO de manera consistente.

**Implementación**: Clases Mapper dedicadas
```java
@Component
public class PacienteMapper {
    
    public PacienteResponseDTO toResponseDTO(Paciente entity) {
        return PacienteResponseDTO.builder()
            .id(entity.getId())
            .nombre(entity.getNombre())
            .especie(entity.getEspecie())
            // ... mapear campos
            .build();
    }
    
    public Paciente toEntity(PacienteRequestDTO dto) {
        return Paciente.builder()
            .nombre(dto.getNombre())
            .especie(dto.getEspecie())
            // ... mapear campos
            .build();
    }
}
```

**Beneficios**:
- ✅ Separación clara de responsabilidades
- ✅ Reusabilidad
- ✅ Fácil mantenimiento
- ✅ Testing independiente

---

### 10. **Exception Handler Pattern** ✅
**Propósito**: Manejo centralizado de excepciones.

**Implementación**: @ControllerAdvice
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        // Manejo de errores de validación
    }
}
```

**Beneficios**:
- ✅ Respuestas de error consistentes
- ✅ Código limpio en controllers
- ✅ Fácil logging de errores
- ✅ Cliente recibe errores estructurados

---

## 📦 Estructura de Paquetes Organizada

```
com.clinica.veterinaria/
├── config/                  # Configuraciones
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
│
├── controller/              # REST Controllers
│   ├── AuthController.java
│   ├── PacienteController.java
│   └── PropietarioController.java
│
├── dto/                     # Data Transfer Objects
│   ├── request/            # DTOs de entrada
│   │   ├── PacienteRequestDTO.java
│   │   └── LoginRequestDTO.java
│   ├── response/           # DTOs de salida
│   │   ├── PacienteResponseDTO.java
│   │   └── AuthResponseDTO.java
│   └── mapper/             # Mappers Entity ↔ DTO
│       └── PacienteMapper.java
│
├── entity/                  # Entidades JPA
│   ├── Paciente.java
│   ├── Propietario.java
│   └── Usuario.java
│
├── repository/              # Repositorios
│   ├── PacienteRepository.java
│   └── UsuarioRepository.java
│
├── service/                 # Servicios (lógica de negocio)
│   ├── PacienteService.java
│   ├── AuthService.java
│   └── impl/               # Implementaciones si necesario
│       └── PacienteServiceImpl.java
│
├── security/                # Seguridad y JWT
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
│
└── exception/               # Excepciones personalizadas
    ├── ResourceNotFoundException.java
    ├── BusinessException.java
    └── GlobalExceptionHandler.java
```

---

## 🔒 Principios SOLID Aplicados

### S - Single Responsibility Principle
✅ Cada clase tiene una única responsabilidad
- Controllers: Manejar HTTP
- Services: Lógica de negocio
- Repositories: Acceso a datos

### O - Open/Closed Principle
✅ Abierto a extensión, cerrado a modificación
- Uso de interfaces
- Strategy pattern para nuevas funcionalidades

### L - Liskov Substitution Principle
✅ Las subclases pueden reemplazar a sus clases base
- Interfaces bien definidas
- Polimorfismo apropiado

### I - Interface Segregation Principle
✅ Interfaces específicas y pequeñas
- No forzar implementación de métodos innecesarios

### D - Dependency Inversion Principle
✅ Depender de abstracciones, no de concreciones
- Inyección de dependencias
- Uso de interfaces

---

## 🎨 Mejores Prácticas Adicionales

### ✅ Clean Code
- Nombres descriptivos
- Métodos pequeños y enfocados
- Comentarios solo cuando es necesario
- DRY (Don't Repeat Yourself)

### ✅ RESTful Design
- Uso correcto de verbos HTTP
- Nombres de recursos en plural
- Códigos de estado HTTP apropiados
- HATEOAS opcional para navegabilidad

### ✅ Seguridad
- Validación en múltiples capas
- Sanitización de inputs
- JWT para autenticación stateless
- Roles y permisos granulares

### ✅ Performance
- Paginación en listados
- Lazy loading apropiado
- Índices en base de datos
- Caché cuando sea necesario

---

## 📊 Ejemplo Completo de Flujo

```
1. Cliente hace petición
   POST /api/pacientes

2. Controller recibe y valida
   @PostMapping("/pacientes")
   @Valid PacienteRequestDTO

3. Service procesa lógica
   - Valida propietario existe
   - Crea entidad
   - Guarda en DB
   - Retorna DTO

4. Repository persiste
   pacienteRepository.save(paciente)

5. Mapper convierte
   Entity → DTO

6. Controller retorna
   ResponseEntity<PacienteResponseDTO>
   Status: 201 Created
```

---

## 🎯 Conclusión

Esta arquitectura nos proporciona:
- ✅ **Mantenibilidad**: Código organizado y fácil de entender
- ✅ **Escalabilidad**: Fácil agregar nuevas funcionalidades
- ✅ **Testabilidad**: Cada capa se puede probar independientemente
- ✅ **Flexibilidad**: Cambios en una capa no afectan otras
- ✅ **Profesionalismo**: Sigue estándares de la industria

Con estos patrones y arquitectura, tendremos un backend robusto, profesional y fácil de mantener.

