import { useEffect, useState } from 'react';

/**
 * Hook personalizado para debounce de valores.
 * 
 * Retrasa la actualización de un valor hasta que haya pasado un tiempo determinado
 * sin cambios. Útil para optimizar búsquedas, filtros y otras operaciones que
 * no necesitan ejecutarse en cada cambio de input.
 * 
 * **Características:**
 * - Evita llamadas excesivas a APIs durante la escritura
 * - Mejora el rendimiento al reducir operaciones innecesarias
 * - Limpia automáticamente el timeout al desmontar el componente
 * 
 * @template T Tipo del valor a debounce
 * @param value Valor que se desea debounce
 * @param delay Tiempo de espera en milisegundos antes de actualizar el valor (default: 500ms)
 * @returns Valor debounced que solo se actualiza después del delay especificado
 * 
 * @example
 * ```tsx
 * function SearchComponent() {
 *   const [searchTerm, setSearchTerm] = useState('');
 *   const debouncedSearch = useDebounce(searchTerm, 500);
 *   
 *   // La búsqueda solo se ejecuta 500ms después de que el usuario deje de escribir
 *   useEffect(() => {
 *     if (debouncedSearch) {
 *       performSearch(debouncedSearch);
 *     }
 *   }, [debouncedSearch]);
 *   
 *   return (
 *     <input 
 *       value={searchTerm}
 *       onChange={(e) => setSearchTerm(e.target.value)}
 *     />
 *   );
 * }
 * ```
 * 
 * @see https://react.dev/reference/react/useEffect
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

