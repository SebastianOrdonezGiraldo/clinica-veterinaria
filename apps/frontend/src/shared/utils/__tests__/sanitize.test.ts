/**
 * Tests unitarios para funciones de sanitización
 * 
 * Verifica que las funciones de sanitización funcionen correctamente
 * y prevengan ataques XSS.
 */

import { describe, it, expect } from 'vitest';
import { sanitizeHTML, sanitizeText, sanitizeURL, sanitizeObject } from '../sanitize';

// Nota: Estos tests requieren que vitest esté configurado
// Por ahora, son pruebas manuales que se pueden ejecutar en la consola del navegador

describe('sanitizeText', () => {
  it('debe eliminar tags HTML', () => {
    const input = '<script>alert("XSS")</script>Texto seguro';
    const result = sanitizeText(input);
    expect(result).toBe('Texto seguro');
  });

  it('debe manejar strings vacíos', () => {
    expect(sanitizeText('')).toBe('');
    expect(sanitizeText(null as any)).toBe('');
  });

  it('debe preservar texto normal', () => {
    const input = 'Texto normal sin HTML';
    expect(sanitizeText(input)).toBe('Texto normal sin HTML');
  });
});

describe('sanitizeHTML', () => {
  it('debe eliminar scripts', () => {
    const input = '<script>alert("XSS")</script><p>Texto seguro</p>';
    const result = sanitizeHTML(input);
    expect(result).not.toContain('<script>');
    expect(result).toContain('<p>Texto seguro</p>');
  });

  it('debe permitir HTML básico seguro', () => {
    const input = '<p>Texto <strong>importante</strong></p>';
    const result = sanitizeHTML(input);
    expect(result).toContain('<p>');
    expect(result).toContain('<strong>');
  });
});

describe('sanitizeURL', () => {
  it('debe bloquear javascript: URLs', () => {
    const input = 'javascript:alert("XSS")';
    const result = sanitizeURL(input);
    expect(result).toBe('');
  });

  it('debe permitir URLs HTTP/HTTPS válidas', () => {
    const input = 'https://example.com';
    const result = sanitizeURL(input);
    expect(result).toContain('https://');
  });
});

describe('sanitizeObject', () => {
  it('debe sanitizar strings en objetos', () => {
    const input = {
      nombre: '<script>alert("XSS")</script>Juan',
      edad: 30,
      email: 'test@example.com',
    };
    const result = sanitizeObject(input);
    expect(result.nombre).toBe('Juan');
    expect(result.edad).toBe(30);
  });
});

