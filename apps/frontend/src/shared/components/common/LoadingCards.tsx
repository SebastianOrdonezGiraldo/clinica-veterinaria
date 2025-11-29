import { Card, CardContent, CardHeader } from '@shared/components/ui/card';
import { Skeleton } from '@shared/components/ui/skeleton';

/**
 * Propiedades del componente LoadingCards.
 *
 * @interface LoadingCardsProps
 */
interface LoadingCardsProps {
  /** Número de tarjetas skeleton a mostrar (por defecto: 6) */
  count?: number;
}

/**
 * Componente de carga para listas de tarjetas.
 *
 * Muestra un grid de tarjetas skeleton animadas mientras se cargan los datos.
 * Útil para mejorar la experiencia de usuario durante la carga de listas.
 *
 * @component
 *
 * @param {LoadingCardsProps} props - Propiedades del componente
 * @param {number} [props.count=6] - Número de tarjetas skeleton a mostrar
 *
 * @returns {JSX.Element} Grid de tarjetas skeleton
 *
 * @example
 * ```tsx
 * // 6 tarjetas por defecto
 * {isLoading ? <LoadingCards /> : <RealContent />}
 *
 * // 3 tarjetas personalizadas
 * {isLoading ? <LoadingCards count={3} /> : <RealContent />}
 * ```
 */
export function LoadingCards({ count = 6 }: LoadingCardsProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: count }).map((_, i) => (
        <Card key={i}>
          <CardHeader className="pb-3">
            <div className="flex items-center gap-3">
              <Skeleton className="h-12 w-12 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-3/4" />
                <Skeleton className="h-3 w-1/2" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-2">
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-5/6" />
            <Skeleton className="h-3 w-4/6" />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
