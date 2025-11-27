import DOMPurify from 'dompurify';

/**
 * Utilidades para sanitización de inputs y contenido HTML
 * 
 * Previene ataques XSS (Cross-Site Scripting) al limpiar contenido HTML
 * antes de renderizarlo o almacenarlo.
 */

/**
 * Sanitiza una cadena de texto HTML
 * 
 * Elimina scripts, eventos y otros elementos peligrosos mientras
 * preserva el formato HTML básico seguro.
 * 
 * @param dirty - Contenido HTML a sanitizar
 * @param config - Configuración opcional de DOMPurify
 * @returns Contenido HTML sanitizado y seguro
 * 
 * @example
 * ```tsx
 * const userInput = '<script>alert("XSS")</script><p>Texto seguro</p>';
 * const safe = sanitizeHTML(userInput);
 * // Resultado: '<p>Texto seguro</p>'
 * ```
 */
export function sanitizeHTML(
  dirty: string,
  config?: DOMPurify.Config
): string {
  if (!dirty) return '';
  
  // Configuración por defecto: permitir solo HTML básico seguro
  const defaultConfig: DOMPurify.Config = {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6'],
    ALLOWED_ATTR: ['class'],
    KEEP_CONTENT: true,
    ...config,
  };

  return DOMPurify.sanitize(dirty, defaultConfig);
}

/**
 * Sanitiza texto plano (sin HTML)
 * 
 * Escapa caracteres HTML y elimina cualquier tag HTML.
 * Útil para inputs de texto que no deben contener HTML.
 * 
 * @param text - Texto a sanitizar
 * @returns Texto plano sanitizado
 * 
 * @example
 * ```tsx
 * const userInput = '<script>alert("XSS")</script>Texto';
 * const safe = sanitizeText(userInput);
 * // Resultado: 'Texto' (sin tags HTML)
 * ```
 */
export function sanitizeText(text: string): string {
  if (!text) return '';
  
  // Eliminar todos los tags HTML
  return DOMPurify.sanitize(text, {
    ALLOWED_TAGS: [],
    KEEP_CONTENT: true,
  });
}

/**
 * Sanitiza una URL
 * 
 * Valida y sanitiza URLs para prevenir ataques de redirección maliciosa.
 * 
 * @param url - URL a sanitizar
 * @returns URL sanitizada o cadena vacía si es inválida
 * 
 * @example
 * ```tsx
 * const userUrl = 'javascript:alert("XSS")';
 * const safe = sanitizeURL(userUrl);
 * // Resultado: '' (URL bloqueada)
 * ```
 */
export function sanitizeURL(url: string): string {
  if (!url) return '';
  
  try {
    // Intentar crear un objeto URL para validar
    const urlObj = new URL(url, window.location.origin);
    
    // Solo permitir http, https, mailto, tel
    const allowedProtocols = ['http:', 'https:', 'mailto:', 'tel:'];
    if (!allowedProtocols.includes(urlObj.protocol)) {
      return '';
    }
    
    return urlObj.toString();
  } catch {
    // Si no es una URL válida, retornar vacío
    return '';
  }
}

/**
 * Sanitiza un objeto completo recursivamente
 * 
 * Útil para sanitizar datos de formularios o objetos complejos
 * antes de enviarlos al backend.
 * 
 * @param obj - Objeto a sanitizar
 * @param options - Opciones de sanitización
 * @returns Objeto sanitizado
 */
export function sanitizeObject<T extends Record<string, any>>(
  obj: T,
  options?: {
    sanitizeHTML?: boolean;
    sanitizeText?: boolean;
    excludeKeys?: string[];
  }
): T {
  const {
    sanitizeHTML: shouldSanitizeHTML = false,
    sanitizeText: shouldSanitizeText = true,
    excludeKeys = ['id', 'createdAt', 'updatedAt', 'password', 'token'],
  } = options || {};

  const sanitized = { ...obj };

  for (const key in sanitized) {
    if (excludeKeys.includes(key)) {
      continue; // No sanitizar campos sensibles o técnicos
    }

    const value = sanitized[key];

    if (typeof value === 'string') {
      if (shouldSanitizeHTML) {
        sanitized[key] = sanitizeHTML(value) as any;
      } else if (shouldSanitizeText) {
        sanitized[key] = sanitizeText(value) as any;
      }
    } else if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      sanitized[key] = sanitizeObject(value, options) as any;
    } else if (Array.isArray(value)) {
      sanitized[key] = value.map((item) =>
        typeof item === 'string'
          ? shouldSanitizeHTML
            ? sanitizeHTML(item)
            : shouldSanitizeText
            ? sanitizeText(item)
            : item
          : typeof item === 'object' && item !== null
          ? sanitizeObject(item, options)
          : item
      ) as any;
    }
  }

  return sanitized;
}

