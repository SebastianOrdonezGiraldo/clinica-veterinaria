package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.entity.Usuario.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Usuario}.
 * 
 * <p>Proporciona métodos de acceso a datos para usuarios del sistema, incluyendo
 * búsquedas por email, rol, nombre y estado. Incluye métodos especializados para
 * autenticación y consultas de veterinarios activos.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Búsqueda por email (único) - usado en autenticación</li>
 *   <li>Verificación de existencia por email</li>
 *   <li>Filtros por rol y estado activo</li>
 *   <li>Búsqueda por nombre (case-insensitive, parcial)</li>
 *   <li>Query personalizada para veterinarios activos</li>
 *   <li>Métodos de conteo por rol</li>
 * </ul>
 * 
 * <p><strong>Nota de seguridad:</strong> El email es único en el sistema y se usa
 * como identificador de autenticación.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Usuario
 * @see UsuarioService
 * @see AuthService
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por rol
     * @param rol Rol a buscar
     * @return Lista de usuarios con ese rol
     */
    List<Usuario> findByRol(Rol rol);

    /**
     * Busca usuarios activos por rol
     * @param rol Rol a buscar
     * @param activo Estado de actividad
     * @return Lista de usuarios activos con ese rol
     */
    List<Usuario> findByRolAndActivo(Rol rol, Boolean activo);

    /**
     * Busca usuarios activos por múltiples roles
     * @param roles Lista de roles a buscar
     * @param activo Estado de actividad
     * @return Lista de usuarios activos con esos roles
     */
    List<Usuario> findByRolInAndActivo(List<Rol> roles, Boolean activo);

    /**
     * Busca usuarios activos
     * @param activo Estado de actividad
     * @return Lista de usuarios activos
     */
    List<Usuario> findByActivo(Boolean activo);

    /**
     * Busca usuarios por nombre (ignorando mayúsculas/minúsculas)
     * @param nombre Nombre o parte del nombre
     * @return Lista de usuarios que coinciden
     */
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Cuenta usuarios por rol
     * @param rol Rol a contar
     * @return Cantidad de usuarios con ese rol
     */
    long countByRol(Rol rol);

    /**
     * Query personalizada: busca veterinarios activos
     * @return Lista de veterinarios activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol = 'VET' AND u.activo = true ORDER BY u.nombre")
    List<Usuario> findVeterinariosActivos();
    
    // Métodos de paginación
    Page<Usuario> findByRol(Rol rol, Pageable pageable);
    
    Page<Usuario> findByActivo(Boolean activo, Pageable pageable);
    
    Page<Usuario> findByRolAndActivo(Rol rol, Boolean activo, Pageable pageable);
    
    Page<Usuario> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    Page<Usuario> findByNombreContainingIgnoreCaseAndRol(String nombre, Rol rol, Pageable pageable);
}

