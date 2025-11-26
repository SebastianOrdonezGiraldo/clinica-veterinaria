import { useEffect, useState } from 'react';

/**
 * Hook personalizado para debounce de valores
 * Útil para búsquedas y filtros que requieren esperar a que el usuario termine de escribir
 * 
 * @param value Valor a debounce
 * @param delay Tiempo de espera en milisegundos (default: 500ms)
 * @returns Valor debounced
 * 
 * @example
 * const [searchTerm, setSearchTerm] = useState('');
 * const debouncedSearch = useDebounce(searchTerm, 500);
 * 
 * // El valor debounced solo cambiará 500ms después de que el usuario deje de escribir
 */
export function useDebounce<T>(value: T, delay: number = 500): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

