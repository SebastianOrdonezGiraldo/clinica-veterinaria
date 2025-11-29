import axios from '@core/api/axios';

/**
 * Servicio para recuperación de contraseñas
 */
export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  password: string;
}

export interface ForgotPasswordResponse {
  message: string;
}

export interface ResetPasswordResponse {
  message: string;
}

export interface ValidateTokenResponse {
  valid: boolean;
  expiresAt?: string;
  expiresInHours?: number;
}

export const passwordResetService = {
  /**
   * Solicita recuperación de contraseña para un usuario del sistema
   */
  async forgotPasswordUsuario(email: string): Promise<ForgotPasswordResponse> {
    const response = await axios.post<ForgotPasswordResponse>(
      '/public/password/forgot-usuario',
      { email }
    );
    return response.data;
  },

  /**
   * Solicita recuperación de contraseña para un cliente/propietario
   */
  async forgotPasswordCliente(email: string): Promise<ForgotPasswordResponse> {
    const response = await axios.post<ForgotPasswordResponse>(
      '/public/password/forgot-cliente',
      { email }
    );
    return response.data;
  },

  /**
   * Resetea la contraseña usando un token de recuperación
   */
  async resetPassword(token: string, password: string): Promise<ResetPasswordResponse> {
    const response = await axios.post<ResetPasswordResponse>(
      '/public/password/reset',
      { token, password }
    );
    return response.data;
  },

  /**
   * Valida si un token de recuperación es válido y obtiene información adicional
   */
  async validateToken(token: string): Promise<ValidateTokenResponse> {
    const response = await axios.get<ValidateTokenResponse>(
      `/public/password/validate-token?token=${encodeURIComponent(token)}`
    );
    return response.data;
  },
};

