# 🎯 Resumen de Patrones - Guía Rápida

## 🔄 Flujo de una Petición Completa

```
📱 FRONTEND                  🖥️  BACKEND
   │                            │
   │  POST /api/pacientes       │
   │  { nombre: "Max", ... }    │
   ├────────────────────────────>│
   │                            │ 1️⃣ CONTROLLER
   │                            │    @RestController
   │                            │    ├─ Valida @Valid
   │                            │    ├─ Maneja HTTP
   │                            │    └─ Retorna DTO
   │                            │         │
   │                            │         ▼
   │                            │ 2️⃣ SERVICE
   │                            │    @Service
   │                            │    ├─ Lógica de negocio
   │                            │    ├─ Valida reglas
   │                            │    ├─ Usa Mapper
   │                            │    └─ Coordina repos
   │                            │         │
   │                            │         ▼
   │                            │ 3️⃣ REPOSITORY
   │                            │    @Repository
   │                            │    ├─ Acceso a datos
   │                            │    └─ Queries JPA
   │                            │         │
   │                            │         ▼
   │                            │ 🗄️ POSTGRESQL
   │                            │    INSERT INTO...
   │                            │         │
   │                            │    ✅ Paciente guardado
   │                            │         │
   │                            │         ▼
   │  { id: 1, nombre: "Max" }  │ 4️⃣ MAPPER
   │ <────────────────────────────    Entity → DTO
   │  Status: 201 Created       │
   │                            │
```

## 📋 Checklist de Implementación por Capa

### 1. ENTIDADES (Entity) - `@Entity`
```java
✅ Anotaciones JPA (@Entity, @Table, @Id, @GeneratedValue)
✅ Relaciones (@OneToMany, @ManyToOne, @ManyToMany)
✅ Lombok (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
✅ Validaciones básicas (@NotNull, @Size)
✅ Timestamps (createdAt, updatedAt)
✅ Soft delete (activo: boolean)
```

### 2. DTOs - Request y Response
```java
// Request DTO (entrada)
✅ Validaciones (@NotBlank, @Email, @Min, @Max)
✅ Solo campos necesarios para crear/actualizar
✅ Sin lógica de negocio

// Response DTO (salida)
✅ Todos los campos que el frontend necesita
✅ DTOs anidados cuando sea necesario
✅ Sin datos sensibles (passwords, etc.)
```

### 3. MAPPERS - Conversión Entity ↔ DTO
```java
✅ Método toEntity(RequestDTO) → Entity
✅ Método toResponseDTO(Entity) → ResponseDTO
✅ Método toResponseDTOList(List<Entity>) → List<ResponseDTO>
✅ Manejo de relaciones anidadas
✅ @Component para inyección
```

### 4. REPOSITORIES - `@Repository`
```java
✅ Extends JpaRepository<Entity, Long>
✅ Queries por convención (findBy...)
✅ @Query para queries complejas
✅ Paginación (Pageable)
```

### 5. SERVICES - `@Service`
```java
✅ Lógica de negocio
✅ @Transactional
✅ Inyección por constructor (final fields)
✅ Usa mappers
✅ Usa repositorios
✅ Lanza excepciones custom
✅ Validaciones complejas
```

### 6. CONTROLLERS - `@RestController`
```java
✅ @RequestMapping("/api/...")
✅ Verbos HTTP correctos
✅ @Valid para validación automática
✅ ResponseEntity con status codes
✅ Documentación @Operation (Swagger)
✅ Manejo de errores delegado
```

### 7. EXCEPTION HANDLERS - `@ControllerAdvice`
```java
✅ @ExceptionHandler para cada tipo
✅ ResponseEntity con ErrorResponse
✅ Códigos HTTP apropiados
✅ Logging de errores
```

## 📊 Ejemplo Código Real

### Entity
```java
@Entity
@Table(name = "pacientes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String nombre;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Propietario propietario;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### Request DTO
```java
@Data
@Builder
public class PacienteRequestDTO {
    @NotBlank(message = "Nombre es requerido")
    @Size(max = 100)
    private String nombre;
    
    @NotNull(message = "Propietario es requerido")
    private Long propietarioId;
    
    @NotNull(message = "Especie es requerida")
    private String especie;
}
```

### Response DTO
```java
@Data
@Builder
public class PacienteResponseDTO {
    private Long id;
    private String nombre;
    private String especie;
    private PropietarioDTO propietario; // DTO anidado
    private LocalDateTime createdAt;
}
```

### Mapper
```java
@Component
@RequiredArgsConstructor
public class PacienteMapper {
    
    private final PropietarioMapper propietarioMapper;
    
    public PacienteResponseDTO toResponseDTO(Paciente entity) {
        return PacienteResponseDTO.builder()
            .id(entity.getId())
            .nombre(entity.getNombre())
            .especie(entity.getEspecie())
            .propietario(propietarioMapper.toDTO(entity.getPropietario()))
            .createdAt(entity.getCreatedAt())
            .build();
    }
    
    public Paciente toEntity(PacienteRequestDTO dto, Propietario propietario) {
        return Paciente.builder()
            .nombre(dto.getNombre())
            .especie(dto.getEspecie())
            .propietario(propietario)
            .build();
    }
}
```

### Repository
```java
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    List<Paciente> findByPropietarioId(Long propietarioId);
    
    @Query("SELECT p FROM Paciente p WHERE p.especie = :especie AND p.activo = true")
    List<Paciente> findActiveByEspecie(@Param("especie") String especie);
    
    Page<Paciente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
```

### Service
```java
@Service
@Transactional
@RequiredArgsConstructor
public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final PacienteMapper pacienteMapper;
    
    public PacienteResponseDTO crearPaciente(PacienteRequestDTO requestDTO) {
        // 1. Validar propietario existe
        Propietario propietario = propietarioRepository
            .findById(requestDTO.getPropietarioId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Propietario no encontrado con id: " + requestDTO.getPropietarioId()
            ));
        
        // 2. Convertir DTO a Entity
        Paciente paciente = pacienteMapper.toEntity(requestDTO, propietario);
        
        // 3. Guardar
        Paciente saved = pacienteRepository.save(paciente);
        
        // 4. Retornar DTO
        return pacienteMapper.toResponseDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> obtenerTodos() {
        return pacienteRepository.findAll()
            .stream()
            .map(pacienteMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPorId(Long id) {
        Paciente paciente = pacienteRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Paciente no encontrado con id: " + id
            ));
        return pacienteMapper.toResponseDTO(paciente);
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "API de gestión de pacientes")
public class PacienteController {
    
    private final PacienteService pacienteService;
    
    @PostMapping
    @Operation(summary = "Crear nuevo paciente")
    public ResponseEntity<PacienteResponseDTO> crear(
            @Valid @RequestBody PacienteRequestDTO requestDTO) {
        PacienteResponseDTO response = pacienteService.crearPaciente(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Obtener todos los pacientes")
    public ResponseEntity<List<PacienteResponseDTO>> obtenerTodos() {
        List<PacienteResponseDTO> pacientes = pacienteService.obtenerTodos();
        return ResponseEntity.ok(pacientes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener paciente por ID")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        PacienteResponseDTO paciente = pacienteService.obtenerPorId(id);
        return ResponseEntity.ok(paciente);
    }
}
```

### Exception Handler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.error("Recurso no encontrado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
            
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Error en validación de datos")
            .validationErrors(errors)
            .build();
            
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
```

## 🎯 Orden de Implementación

```
1. ✅ Entities        (Base de datos)
2. ✅ DTOs            (Contratos de API)
3. ✅ Mappers         (Conversión)
4. ✅ Repositories    (Acceso a datos)
5. ✅ Services        (Lógica de negocio)
6. ✅ Controllers     (API REST)
7. ✅ Exceptions      (Manejo de errores)
8. ✅ Security        (Autenticación)
9. ✅ Tests           (Verificación)
```

## 🚀 Ventajas de Esta Arquitectura

✅ **Código Limpio y Organizado**
✅ **Fácil de Mantener y Escalar**
✅ **Testeable en Cada Capa**
✅ **Sigue Principios SOLID**
✅ **Reutilizable**
✅ **Documentado Automáticamente (Swagger)**
✅ **Manejo Robusto de Errores**
✅ **Performance Optimizado**

---

**¿Listo para empezar a codificar?** 🚀

