# 🧪 Guía de Tests - Clínica Veterinaria

## 📋 Resumen

Se han creado tests unitarios y de integración para la aplicación. Los tests están en `src/test/java`.

### ✅ Tests Creados

1. **Tests de Integración (Controladores)**
   - `AuthControllerTest` - Login y autenticación
   - `PropietarioControllerTest` - API REST de propietarios

2. **Tests Unitarios (Servicios)** 
   - `UsuarioServiceTest`
   - `PropietarioServiceTest`  
   - `PacienteServiceTest`

3. **Tests de Seguridad**
   - `JwtUtilTest` - Generación y validación de tokens

4. **Tests de Repositorio**
   - `UsuarioRepositoryTest`

---

## 🔧 Configuración

### Base de Datos H2 para Tests

Los tests usan H2 en memoria (configurado en `application-test.properties`):
- No afecta la base de datos PostgreSQL de desarrollo
- Se crea y destruye automáticamente en cada ejecución
- Más rápido que usar PostgreSQL

### Dependencias Agregadas

```xml
<!-- H2 Database for Tests -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🚀 Ejecutar Tests

### Todos los tests
```bash
mvn test
```

### Un test específico
```bash
mvn test -Dtest=AuthControllerTest
```

### Con reporte de cobertura
```bash
mvn test jacoco:report
```

---

## 📝 Nota sobre los Tests Actuales

Los tests de servicios necesitan ajustes para coincidir con los nombres de métodos reales:

| Método en Test | Método Real en Servicio |
|----------------|------------------------|
| `listarTodos()` | `findAll()` |
| `obtenerPorId()` | `findById()` |
| `crear()` | `save()` |
| `actualizar()` | `update()` |
| `eliminar()` | `deleteById()` |

---

## ✅ Test Funcional: AuthControllerTest

Este test está completamente funcional y prueba:

- ✅ Login exitoso
- ✅ Credenciales incorrectas
- ✅ Usuario no existe
- ✅ Validaciones de email/password vacíos
- ✅ Formato de email inválido
- ✅ Validación de tokens JWT

### Ejecutar solo este test:
```bash
cd backend
mvn test -Dtest=AuthControllerTest
```

---

## 📊 Estructura de un Test Completo

### Ejemplo: Test Unitario de Servicio

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de MiServicio")
class MiServicioTest {

    @Mock
    private MiRepository repository;
    
    @InjectMocks
    private MiServicio servicio;
    
    @Test
    @DisplayName("Debe listar todos los registros")
    void testFindAll() {
        // Arrange
        List<Entidad> lista = Arrays.asList(new Entidad());
        when(repository.findAll()).thenReturn(lista);
        
        // Act
        List<DTO> resultado = servicio.findAll();
        
        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository, times(1)).findAll();
    }
}
```

### Ejemplo: Test de Integración de Controlador

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de MiController")
class MiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String token;
    
    @BeforeEach
    void setUp() {
        // Configurar datos de prueba y obtener token
    }
    
    @Test
    @DisplayName("GET /api/recurso - Debe listar recursos")
    void testListar() throws Exception {
        mockMvc.perform(get("/api/recurso")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

---

## 🔍 Mejores Prácticas

### 1. Nombres Descriptivos
```java
@Test
@DisplayName("Debe lanzar excepción cuando el usuario no existe")
void testObtenerPorId_NoExiste() { ... }
```

### 2. Patrón AAA (Arrange-Act-Assert)
```java
// Arrange - Preparar datos
Usuario usuario = crearUsuarioTest();

// Act - Ejecutar acción
UsuarioDTO resultado = servicio.findById(1L);

// Assert - Verificar resultados
assertNotNull(resultado);
assertEquals("Juan", resultado.getNombre());
```

### 3. Verificar Interacciones con Mockito
```java
verify(repository, times(1)).findById(1L);
verify(repository, never()).delete(any());
```

### 4. Tests de Excepciones
```java
assertThrows(RuntimeException.class, 
    () -> servicio.findById(999L));
```

---

## 🎯 Próximos Pasos

### Para completar la suite de tests:

1. **Ajustar nombres de métodos** en los tests de servicios
2. **Agregar tests para CitaService y ConsultaService**
3. **Agregar tests de repositorios** para todos los repositorios
4. **Configurar JaCoCo** para reportes de cobertura de código
5. **Agregar tests de validación** para DTOs

### Ejemplo de configuración de JaCoCo:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## 📚 Recursos

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

## ✅ Verificación Rápida

Para verificar que al menos un test funciona:

```bash
cd backend
mvn test -Dtest=AuthControllerTest
```

Si este test pasa, la configuración de testing está correcta ✅

