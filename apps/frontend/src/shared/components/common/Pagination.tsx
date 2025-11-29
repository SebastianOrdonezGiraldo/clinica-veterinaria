import { Button } from '@shared/components/ui/button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

/**
 * Propiedades del componente Pagination.
 *
 * @interface PaginationProps
 */
interface PaginationProps {
  /** Página actual (1-indexed) */
  currentPage: number;
  /** Número total de páginas */
  totalPages: number;
  /** Callback al cambiar de página */
  onPageChange: (page: number) => void;
  /** Elementos por página */
  itemsPerPage: number;
  /** Total de elementos */
  totalItems: number;
}

/**
 * Componente de paginación para listas y tablas.
 *
 * Muestra controles de navegación entre páginas con información del rango
 * de elementos mostrados. Incluye botones anterior/siguiente y números de página.
 *
 * @component
 *
 * @param {PaginationProps} props - Propiedades del componente
 * @param {number} props.currentPage - Página actual (1-indexed)
 * @param {number} props.totalPages - Número total de páginas
 * @param {Function} props.onPageChange - Callback al cambiar de página
 * @param {number} props.itemsPerPage - Elementos por página
 * @param {number} props.totalItems - Total de elementos
 *
 * @returns {JSX.Element} Controles de paginación
 *
 * @example
 * ```tsx
 * <Pagination
 *   currentPage={1}
 *   totalPages={10}
 *   onPageChange={(page) => setCurrentPage(page)}
 *   itemsPerPage={10}
 *   totalItems={100}
 * />
 * ```
 */
export function Pagination({ currentPage, totalPages, onPageChange, itemsPerPage, totalItems }: PaginationProps) {
  const startItem = (currentPage - 1) * itemsPerPage + 1;
  const endItem = Math.min(currentPage * itemsPerPage, totalItems);

  return (
    <div className="flex items-center justify-between px-2 py-4">
      <div className="text-sm text-muted-foreground">
        Mostrando {startItem} - {endItem} de {totalItems} resultados
      </div>
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
        >
          <ChevronLeft className="h-4 w-4" />
          Anterior
        </Button>
        <div className="flex items-center gap-1">
          {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
            let pageNumber;
            if (totalPages <= 5) {
              pageNumber = i + 1;
            } else if (currentPage <= 3) {
              pageNumber = i + 1;
            } else if (currentPage >= totalPages - 2) {
              pageNumber = totalPages - 4 + i;
            } else {
              pageNumber = currentPage - 2 + i;
            }
            
            return (
              <Button
                key={pageNumber}
                variant={currentPage === pageNumber ? 'default' : 'outline'}
                size="sm"
                onClick={() => onPageChange(pageNumber)}
                className="w-8"
              >
                {pageNumber}
              </Button>
            );
          })}
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
        >
          Siguiente
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
