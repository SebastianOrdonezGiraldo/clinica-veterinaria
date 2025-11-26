import { useState, useCallback } from 'react';

/**
 * Hook personalizado para manejar paginación
 * 
 * @param initialPage Página inicial (default: 0)
 * @param initialSize Tamaño de página inicial (default: 10)
 * @returns Objeto con estado y funciones de paginación
 * 
 * @example
 * const { page, size, setPage, setSize, reset } = usePagination(0, 20);
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

