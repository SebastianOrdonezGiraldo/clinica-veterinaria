import { useState, useCallback } from 'react';

/**
 * Hook personalizado para manejar el estado de paginación.
 * 
 * Proporciona un estado centralizado y funciones helper para navegar entre páginas,
 * cambiar el tamaño de página y resetear la paginación.
 * 
 * **Características:**
 * - Estado de página y tamaño de página
 * - Funciones para navegación (siguiente, anterior, ir a página específica)
 * - Función de reset para volver a valores iniciales
 * - Callbacks memoizados para evitar re-renders innecesarios
 * 
 * @param initialPage Página inicial (default: 0). La paginación comienza en 0.
 * @param initialSize Tamaño de página inicial (default: 10). Número de elementos por página.
 * @returns Objeto con estado y funciones de paginación:
 *   - `page`: Página actual (number)
 *   - `size`: Tamaño de página actual (number)
 *   - `setPage`: Función para establecer página específica
 *   - `setSize`: Función para cambiar tamaño de página
 *   - `nextPage`: Función para ir a la siguiente página
 *   - `prevPage`: Función para ir a la página anterior
 *   - `goToPage`: Función para ir a una página específica
 *   - `reset`: Función para resetear a valores iniciales
 * 
 * @example
 * ```tsx
 * function PatientList() {
 *   const { page, size, nextPage, prevPage, setSize } = usePagination(0, 20);
 *   
 *   const { data, isLoading } = useQuery({
 *     queryKey: ['pacientes', { page, size }],
 *     queryFn: () => pacienteService.searchWithFilters({ page, size }),
 *   });
 *   
 *   return (
 *     <div>
 *       <select value={size} onChange={(e) => setSize(Number(e.target.value))}>
 *         <option value={10}>10 por página</option>
 *         <option value={20}>20 por página</option>
 *       </select>
 *       <button onClick={prevPage} disabled={page === 0}>Anterior</button>
 *       <span>Página {page + 1}</span>
 *       <button onClick={nextPage}>Siguiente</button>
 *     </div>
 *   );
 * }
 * ```
 * 
 * @see useCallback
 */
export function usePagination(initialPage: number = 0, initialSize: number = 10) {
  const [page, setPage] = useState(initialPage);
  const [size, setSize] = useState(initialSize);

  const nextPage = useCallback(() => {
    setPage((prev) => prev + 1);
  }, []);

  const prevPage = useCallback(() => {
    setPage((prev) => Math.max(0, prev - 1));
  }, []);

  const goToPage = useCallback((newPage: number) => {
    setPage(Math.max(0, newPage));
  }, []);

  const reset = useCallback(() => {
    setPage(initialPage);
    setSize(initialSize);
  }, [initialPage, initialSize]);

  return {
    page,
    size,
    setPage,
    setSize,
    nextPage,
    prevPage,
    goToPage,
    reset,
  };
}

