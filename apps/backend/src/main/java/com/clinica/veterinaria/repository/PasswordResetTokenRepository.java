package com.clinica.veterinaria.repository;

import com.clinica.veterinaria.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio JPA para tokens de recuperación de contraseña.
 * 
 * <p>Proporciona métodos para gestionar tokens de recuperación de contraseña,
 * incluyendo búsqueda por token, email y limpieza de tokens expirados.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token por su valor único.
     * 
     * @param token Valor del token a buscar
     * @return Token encontrado o Optional vacío
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Busca tokens activos (no usados y no expirados) por email y tipo de usuario.
     * 
     * @param email Email del usuario
     * @param userType Tipo de usuario ("USUARIO" o "PROPIETARIO")
     * @return Token válido encontrado o Optional vacío
     */
    @Query("SELECT t FROM PasswordResetToken t " +
           "WHERE t.email = :email " +
           "AND t.userType = :userType " +
           "AND t.usado = false " +
           "AND t.expiresAt > :now " +
           "ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findValidTokenByEmailAndUserType(
        @Param("email") String email,
        @Param("userType") String userType,
        @Param("now") LocalDateTime now
    );

    /**
     * Marca un token como usado.
     * 
     * @param tokenId ID del token a marcar como usado
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usado = true WHERE t.id = :tokenId")
    void markAsUsed(@Param("tokenId") Long tokenId);

    /**
     * Elimina tokens expirados (limpieza automática).
     * 
     * @param now Fecha y hora actual
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now OR t.usado = true")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}

