package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.entity.Usuario.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * Proporciona métodos de acceso a datos para usuarios del sistema
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
}

